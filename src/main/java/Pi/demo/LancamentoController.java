package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.RoundingMode; // Importante para o setScale

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

@RestController
@RequestMapping("/contabilidade")
public class LancamentoController {

    @Autowired
    private LancamentoRepository repository;

    @Autowired
    private CategoriaRepository catRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private ContabilidadeService service;

    // CADASTRO DE EMPRESA
    @PostMapping("/empresa/nova")
    public String cadastrarEmpresa(@RequestParam String nome, 
                                   @RequestParam String doc, 
                                   @RequestParam TipoPessoa tipo, 
                                   @RequestParam RegimeTributario regime) {
        Empresa e = new Empresa();
        e.setNome(nome);
        e.setDocumento(doc);
        e.setTipoPessoa(tipo);
        e.setRegimeTributario(regime);
        empresaRepository.save(e);
        return "Empresa cadastrada com sucesso! ID: " + e.getId();
    }

    @GetMapping("/novo")
    public String salvar(@RequestParam String desc, @RequestParam BigDecimal valor, @RequestParam long categoriaId) {
        Optional<Categoria> cat = catRepository.findById(categoriaId);
        if (cat.isPresent()) {
            Lancamento l = new Lancamento();
            l.setDescricao(desc);
            l.setValor(valor);
            l.setCategoria(cat.get());

            // DEFINE A DATA ANTES DE SALVAR!
            l.setData(LocalDate.now()); 

            repository.save(l); // Agora o 'save' leva a data junto
            return "Salvo: " + desc;
        }
        return "Erro: Categoria " + categoriaId + " não encontrada!";
    }

    // SALDO GERAL
    @GetMapping("/saldo")
    public BigDecimal calcularSaldo() {
        List<Lancamento> todos = repository.findAll();
        BigDecimal saldo = BigDecimal.ZERO;
        for (Lancamento l : todos) {
            if (l.getCategoria() != null && l.getCategoria().getTipo() != null) {
                if (l.getCategoria().getTipo().equalsIgnoreCase("RECEITA")) {
                    saldo = saldo.add(l.getValor());
                } else {
                    saldo = saldo.subtract(l.getValor());
                }
            }
        }
        return saldo;
    }

   @GetMapping("/pdf")
public String gerarPdf(@RequestParam(defaultValue = "1") Long empresaId) {
    @SuppressWarnings("null")
    Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
    if (empresaOpt.isEmpty()) {
        return "Erro: Empresa não encontrada!";
    }
    Empresa empresa = empresaOpt.get();

    try (PdfWriter writer = new PdfWriter("Relatorio_Executivo_" + empresa.getNome() + ".pdf");
         PdfDocument pdf = new PdfDocument(writer);
         Document document = new Document(pdf)) {

        // --- CABEÇALHO ---
        Table header = new Table(2).useAllAvailableWidth();
        header.addCell(new Cell().add(new Paragraph("RELATÓRIO DE GESTÃO CONTÁBIL")
                .setFontSize(16).setBold())
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        
        header.addCell(new Cell().add(new Paragraph("EMITIDO EM: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setTextAlignment(TextAlignment.RIGHT).setFontSize(9))
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        document.add(header);

        // --- DADOS DA ORGANIZAÇÃO (Como você preferiu) ---
        document.add(new Paragraph("\nDADOS DA ORGANIZAÇÃO").setBold().setFontSize(11).setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f)));
        document.add(new Paragraph("Razão Social: " + empresa.getNome() + " | CNPJ/CPF: " + empresa.getDocumento()).setFontSize(10));
        document.add(new Paragraph("Regime Tributário: " + empresa.getRegimeTributario() + " | Tipo: " + empresa.getTipoPessoa()).setFontSize(10));
        document.add(new Paragraph("\n"));

        List<Lancamento> todos = repository.findAll();
        
        // --- LISTAGEM DE LANÇAMENTOS (Estilo "2" - Detalhado) ---
        document.add(new Paragraph("DISCRIMINAÇÃO DOS LANÇAMENTOS").setBold().setFontSize(11));
        
        // Tabela com as colunas: Lançamento | Preço
        float[] columnWidths = {4, 2}; 
        Table itemTable = new Table(columnWidths).useAllAvailableWidth();
        
        // Cabeçalho da Tabela (Sem cores, apenas negrito e bordas cinzas)
        itemTable.addCell(new Cell().add(new Paragraph("Lançamento")).setBold().setBackgroundColor(ColorConstants.WHITE));
        itemTable.addCell(new Cell().add(new Paragraph("Preço")).setBold().setBackgroundColor(ColorConstants.WHITE));

        BigDecimal totalReceitas = BigDecimal.ZERO;
        BigDecimal totalDespesas = BigDecimal.ZERO;

        for (Lancamento l : todos) {
            String prefixo = "";
            if (l.getCategoria() != null) {
                if ("RECEITA".equalsIgnoreCase(l.getCategoria().getTipo())) {
                    prefixo = "(+) ";
                    totalReceitas = totalReceitas.add(l.getValor());
                } else {
                    prefixo = "(-) ";
                    totalDespesas = totalDespesas.add(l.getValor());
                }
            }
            
            itemTable.addCell(new Cell().add(new Paragraph(prefixo + l.getDescricao())).setFontSize(9));
            itemTable.addCell(new Cell().add(new Paragraph("R$ " + l.getValor().setScale(2, RoundingMode.HALF_UP))).setFontSize(9));
        }
        document.add(itemTable);

        // --- RESUMO FINANCEIRO ---
        BigDecimal saldoCaixa = totalReceitas.subtract(totalDespesas);
        document.add(new Paragraph("\nRESUMO FINANCEIRO").setBold().setFontSize(11));
        Table resumoTable = new Table(2).setWidth(250).setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.RIGHT);
        resumoTable.addCell(new Cell().add(new Paragraph("Total Receitas:")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        resumoTable.addCell(new Cell().add(new Paragraph("R$ " + totalReceitas)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        resumoTable.addCell(new Cell().add(new Paragraph("Total Despesas:")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        resumoTable.addCell(new Cell().add(new Paragraph("R$ " + totalDespesas)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
        resumoTable.addCell(new Cell().add(new Paragraph("Saldo em Caixa:")).setBold());
        resumoTable.addCell(new Cell().add(new Paragraph("R$ " + saldoCaixa)).setBold());
        document.add(resumoTable);

        // --- ANÁLISE TRIBUTÁRIA (Como você preferiu) ---
        document.add(new Paragraph("\nANÁLISE TRIBUTÁRIA").setBold().setFontSize(11).setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f)));
        BigDecimal imposto = service.calcularImpostoAutomatico(totalReceitas, saldoCaixa, empresa);
        document.add(new Paragraph("Com base no faturamento de R$ " + totalReceitas + " e no regime " + empresa.getRegimeTributario() + 
                     ", o imposto estimado é de: R$ " + imposto.setScale(2, RoundingMode.HALF_UP)).setBold());

        // --- PARECER TÉCNICO (Como você preferiu) ---
        document.add(new Paragraph("\nPARECER TÉCNICO").setBold().setFontSize(11));
        document.add(new Paragraph("Saúde Financeira: " + service.avaliarSaudeFinanceira(saldoCaixa)).setFontSize(10));
        document.add(new Paragraph("Sugestão de Regime: " + service.sugerirMelhorRegime(totalReceitas, saldoCaixa)).setFontSize(10));

        // Rodapé
        document.add(new Paragraph("\n\n------------------------------------------------------------").setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("Documento gerado para fins de acompanhamento interno - PI 3º Semestre")
                .setFontSize(7).setTextAlignment(TextAlignment.CENTER).setItalic());

        return "Relatório Executivo gerado com sucesso!";

    } catch (Exception e) {
        return "Erro ao gerar PDF: " + e.getMessage();
    }
}

    
    // 5.1 LISTAR TODOS OS LANÇAMENTOS
    @GetMapping("/lista")
    public List<Lancamento> listarTodos() {
        return repository.findAll();
    }

    // 5.2 DELETAR UM LANÇAMENTO
    @SuppressWarnings("null")
    @GetMapping("/deletar")
    public String deletar(@RequestParam Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "Lançamento ID " + id + " deletado com sucesso!";
        }
        return "Erro: Lançamento ID " + id + " não encontrado!";
    }

    // BÔNUS: EDITAR VALOR (Para o seu passo 4.2 do .http)
    @GetMapping("/editar")
    public String editar(@RequestParam Long id, @RequestParam BigDecimal valor) {
        @SuppressWarnings("null")
        Optional<Lancamento> lancamentoOpt = repository.findById(id);
        if (lancamentoOpt.isPresent()) {
            Lancamento l = lancamentoOpt.get();
            l.setValor(valor);
            repository.save(l);
            return "Valor do lançamento " + id + " atualizado para R$ " + valor;
        }
        return "Erro: Lançamento não encontrado!";
    }

    @GetMapping("/dashboard")
public ResponseEntity<RelatorioFinanceiroDTO> getDashboard(@RequestParam(defaultValue = "1") Long empresaId) {
    @SuppressWarnings("null")
    Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
    if (empresaOpt.isEmpty()) {
        return ResponseEntity.notFound().build();
    }
    Empresa empresa = empresaOpt.get();

    List<Lancamento> todos = repository.findAll();
    
    BigDecimal totalReceitas = todos.stream()
            .filter(l -> l.getCategoria() != null && "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
            .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            
    BigDecimal totalDespesas = todos.stream()
            .filter(l -> l.getCategoria() != null && "DESPESA".equalsIgnoreCase(l.getCategoria().getTipo()))
            .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            
    BigDecimal saldoCaixa = totalReceitas.subtract(totalDespesas);
    BigDecimal imposto = service.calcularImpostoAutomatico(totalReceitas, saldoCaixa, empresa);

    RelatorioFinanceiroDTO dto = new RelatorioFinanceiroDTO(
        empresa.getNome(),
        totalReceitas,
        totalDespesas,
        saldoCaixa,
        imposto,
        service.avaliarSaudeFinanceira(saldoCaixa),
        service.sugerirMelhorRegime(totalReceitas, saldoCaixa),
        todos
    );

    return ResponseEntity.ok(dto);
}
} 