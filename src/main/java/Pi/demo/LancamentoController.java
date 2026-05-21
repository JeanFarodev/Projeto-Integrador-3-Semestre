package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.RoundingMode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller; // Mudamos para @Controller para aceitar telas HTML
import org.springframework.ui.Model; // Importante para injetar dados no Thymeleaf
import org.springframework.web.bind.annotation.*;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

@Controller // Alterado para gerenciar as views do Thymeleaf
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

    // =========================================================================
    // NAVEGAÇÃO DAS ABAS (Mapeamento das Telas HTML com Informações Reais)
    // =========================================================================

    // ROTA DA ABA: Dashboard
    @GetMapping("/dashboard")
    public String exibirDashboard(@RequestParam(defaultValue = "1") long empresaId, Model model) {
        Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
        if (empresaOpt.isEmpty()) {
            return "redirect:/contabilidade/lista"; // Redireciona se não achar empresa
        }
        Empresa empresa = empresaOpt.get();

        // Isolamento de dados: pegamos apenas os lançamentos desta empresa
        List<Lancamento> todos = repository.findByEmpresaId(empresaId);
        
        BigDecimal totalReceitas = todos.stream()
                .filter(l -> l.getCategoria() != null && "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalDespesas = todos.stream()
                .filter(l -> l.getCategoria() != null && "DESPESA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal saldoCaixa = totalReceitas.subtract(totalDespesas);
        BigDecimal imposto = service.calcularImpostoAutomatico(totalReceitas, saldoCaixa, empresa);

        // Enviando os dados calculados para serem exibidos nas caixas e textos do HTML
        model.addAttribute("nomeEmpresa", empresa.getNome());
        model.addAttribute("receitas", totalReceitas);
        model.addAttribute("despesas", totalDespesas);
        model.addAttribute("saldo", saldoCaixa);
        model.addAttribute("imposto", imposto);
        model.addAttribute("saude", service.avaliarSaudeFinanceira(saldoCaixa));
        model.addAttribute("sugestaoRegime", service.sugerirMelhorRegime(totalReceitas, saldoCaixa));
        model.addAttribute("empresaId", empresaId);

        return "dashboard"; // Retorna o arquivo src/main/resources/templates/dashboard.html
    }

    // ROTA DA ABA: Movimentações
    @GetMapping("/lista")
    public String listarMovimentacoes(@RequestParam(defaultValue = "1") Long empresaId, Model model) {
        // Traz apenas a lista de lançamentos da empresa logada
        List<Lancamento> lancamentosDaEmpresa = repository.findByEmpresaId(empresaId);
        model.addAttribute("listaLancamentos", lancamentosDaEmpresa);
        model.addAttribute("empresaId", empresaId);
        return "movimentacoes"; // Retorna o arquivo src/main/resources/templates/movimentacoes.html
    }

    // ROTA DA ABA: Fechamento (Saldo Líquido)
    @GetMapping("/saldo")
    public String exibirFechamento(@RequestParam(defaultValue = "1") Long empresaId, Model model) {
        BigDecimal saldoLiquido = calcularSaldo(empresaId);
        model.addAttribute("saldo", saldoLiquido);
        model.addAttribute("empresaId", empresaId);
        return "fechamento"; // Retorna o arquivo src/main/resources/templates/fechamento.html
    }

    // =========================================================================
    // ENDPOINTS DE OPERAÇÕES DO BANCO DE DADOS
    // =========================================================================

    // CADASTRO DE EMPRESA
    @PostMapping("/empresa/nova")
    @ResponseBody // Mantém o retorno como texto puro para o arquivo .http
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

    // CADASTRO DE LANÇAMENTO
    @GetMapping("/novo")
    @ResponseBody
    public String salvar(@RequestParam String desc, 
                         @RequestParam BigDecimal valor, 
                         @RequestParam long categoriaId,
                         @RequestParam long empresaId) { 
        
        Optional<Categoria> cat = catRepository.findById(categoriaId);
        Optional<Empresa> emp = empresaRepository.findById(empresaId);

        if (cat.isPresent() && emp.isPresent()) {
            Lancamento l = new Lancamento();
            l.setDescricao(desc);
            l.setValor(valor);
            l.setCategoria(cat.get());
            l.setEmpresa(emp.get()); 
            l.setData(LocalDate.now()); 

            repository.save(l);
            return "Salvo para a empresa " + emp.get().getNome() + ": " + desc;
        }
        return "Erro: Categoria ou Empresa não encontrada!";
    }

    // API JSON DA DASHBOARD (Usada pelo script do Chart.js para desenhar o gráfico)
    @GetMapping("/api/dashboard-dados")
    @ResponseBody
    public ResponseEntity<RelatorioFinanceiroDTO> getDashboardDados(@RequestParam(defaultValue = "1") long empresaId) {
        Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
        if (empresaOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Empresa empresa = empresaOpt.get();

        // Isolamento total no gráfico
        List<Lancamento> todos = repository.findByEmpresaId(empresaId);
        
        BigDecimal totalReceitas = todos.stream()
                .filter(l -> l.getCategoria() != null && "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalDespesas = todos.stream()
                .filter(l -> l.getCategoria() != null && "DESPESA".equalsIgnoreCase(l.getCategoria().getTipo()))
                .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal saldoCaixa = totalReceitas.subtract(totalDespesas);
        BigDecimal imposto = service.calcularImpostoAutomatico(totalReceitas, saldoCaixa, empresa);

        RelatorioFinanceiroDTO dto = new RelatorioFinanceiroDTO(
            empresa.getNome(), totalReceitas, totalDespesas, saldoCaixa, imposto,
            service.avaliarSaudeFinanceira(saldoCaixa), service.sugerirMelhorRegime(totalReceitas, saldoCaixa), todos
        );

        return ResponseEntity.ok(dto);
    }

    // CALCULAR SALDO
    @ResponseBody
    public BigDecimal calcularSaldo(@RequestParam Long empresaId) {
        List<Lancamento> todos = repository.findByEmpresaId(empresaId);
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

    // GERAR RELATÓRIO PDF
    @GetMapping("/pdf")
    @ResponseBody
    public String gerarPdf(@RequestParam(defaultValue = "1") long empresaId) {
        Optional<Empresa> empresaOpt = empresaRepository.findById(empresaId);
        if (empresaOpt.isEmpty()) {
            return "Erro: Empresa não encontrada!";
        }
        Empresa empresa = empresaOpt.get();

        try (PdfWriter writer = new PdfWriter("Relatorio_Executivo_" + empresa.getNome() + ".pdf");
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            Table header = new Table(2).useAllAvailableWidth();
            header.addCell(new Cell().add(new Paragraph("RELATÓRIO DE GESTÃO CONTÁBIL").setFontSize(16).setBold()).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            header.addCell(new Cell().add(new Paragraph("EMITIDO EM: " + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setTextAlignment(TextAlignment.RIGHT).setFontSize(9)).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER));
            document.add(header);

            document.add(new Paragraph("\nDADOS DA ORGANIZAÇÃO").setBold().setFontSize(11).setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f)));
            document.add(new Paragraph("Razão Social: " + empresa.getNome() + " | CNPJ/CPF: " + empresa.getDocumento()).setFontSize(10));
            document.add(new Paragraph("Regime Tributário: " + empresa.getRegimeTributario() + " | Tipo: " + empresa.getTipoPessoa()).setFontSize(10));
            document.add(new Paragraph("\n"));

            List<Lancamento> todos = repository.findByEmpresaId(empresaId);
            
            document.add(new Paragraph("DISCRIMINAÇÃO DOS LANÇAMENTOS").setBold().setFontSize(11));
            float[] columnWidths = {4, 2}; 
            Table itemTable = new Table(columnWidths).useAllAvailableWidth();
            
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

            document.add(new Paragraph("\nANÁLISE TRIBUTÁRIA").setBold().setFontSize(11).setBorderBottom(new com.itextpdf.layout.borders.SolidBorder(0.5f)));
            BigDecimal imposto = service.calcularImpostoAutomatico(totalReceitas, saldoCaixa, empresa);
            document.add(new Paragraph("Com base no faturamento de R$ " + totalReceitas + " e no regime " + empresa.getRegimeTributario() + ", o imposto estimado é de: R$ " + imposto.setScale(2, RoundingMode.HALF_UP)).setBold());

            document.add(new Paragraph("\nPARECER TÉCNICO").setBold().setFontSize(11));
            document.add(new Paragraph("Saúde Financeira: " + service.avaliarSaudeFinanceira(saldoCaixa)).setFontSize(10));
            document.add(new Paragraph("Sugestão de Regime: " + service.sugerirMelhorRegime(totalReceitas, saldoCaixa)).setFontSize(10));

            document.add(new Paragraph("\n\n------------------------------------------------------------").setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Documento gerado para fins de acompanhamento interno - PI 3º Semestre").setFontSize(7).setTextAlignment(TextAlignment.CENTER).setItalic());

            return "Relatório Executivo gerado com sucesso!";

        } catch (Exception e) {
            return "Erro ao gerar PDF: " + e.getMessage();
        }
    }

    // DELETAR LANÇAMENTO
    @SuppressWarnings("null")
    @GetMapping("/deletar")
    @ResponseBody
    public String deletar(@RequestParam Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return "Lançamento ID " + id + " deletado com sucesso!";
        }
        return "Erro: Lançamento ID " + id + " não encontrado!";
    }

    // EDITAR VALOR
    @SuppressWarnings("null")
    @GetMapping("/editar")
    @ResponseBody
    public String editar(@RequestParam Long id, @RequestParam BigDecimal valor) {
        Optional<Lancamento> lancamentoOpt = repository.findById(id);
        if (lancamentoOpt.isPresent()) {
            Lancamento l = lancamentoOpt.get();
            l.setValor(valor);
            repository.save(l);
            return "Valor do lançamento " + id + " atualizado para R$ " + valor;
        }
        return "Erro: Lançamento não encontrado!";
    }
}