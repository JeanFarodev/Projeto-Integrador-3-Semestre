package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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



    @GetMapping("/novo")
    public String salvar(@RequestParam String desc, @RequestParam BigDecimal valor, @RequestParam long categoriaId) {
        Optional<Categoria> cat = catRepository.findById(categoriaId);
      
       if (cat.isPresent()) {
            Lancamento l = new Lancamento();
            l.setDescricao(desc);
            l.setValor(valor);
            l.setCategoria(cat.get());
            repository.save(l);
            return "Salvo: " + desc;
        }
        return "Erro: Categoria " + categoriaId + " não encontrada!";
    }

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
    public String gerarPdf() {
        try (PdfWriter writer = new PdfWriter("Relatorio_Financeiro_Gerencial.pdf");
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf)) {

            document.add(new Paragraph("RELATÓRIO DE FLUXO DE CAIXA E IMPOSTOS")
                    .setFontSize(18).setBold().setTextAlignment(TextAlignment.CENTER));
            document.add(new Paragraph("Gerado em: " + LocalDateTime.now()).setFontSize(10));
            document.add(new Paragraph("\n"));

            List<Lancamento> todos = repository.findAll();
            String tipoDeTributo = "";
            
            // Cálculos de Fluxo
            BigDecimal totalReceitas = todos.stream()
                    .filter(l -> l.getCategoria() != null && "RECEITA".equalsIgnoreCase(l.getCategoria().getTipo()))
                    .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal totalDespesas = todos.stream()
                    .filter(l -> l.getCategoria() != null && "DESPESA".equalsIgnoreCase(l.getCategoria().getTipo()))
                    .map(Lancamento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal saldoCaixa = totalReceitas.subtract(totalDespesas);

            // Tabela Gerencial
            Table table = new Table(2).useAllAvailableWidth();
            table.addCell(new Cell().add(new Paragraph("INDICADOR").setBold()));
            table.addCell(new Cell().add(new Paragraph("VALOR (R$)").setBold()));

            table.addCell("(+) TOTAL RECEITAS");
            table.addCell(new Cell().add(new Paragraph("R$ " + totalReceitas).setFontColor(ColorConstants.GREEN)));

            table.addCell("(-) TOTAL DESPESAS");
            table.addCell(new Cell().add(new Paragraph("R$ " + totalDespesas).setFontColor(ColorConstants.RED)));

            table.addCell(new Cell().add(new Paragraph("(=) SALDO EM CAIXA").setBold()));
            table.addCell(new Cell().add(new Paragraph("R$ " + saldoCaixa).setBold()));

            document.add(table);
            
            // Projeção de Impostos
            document.add(new Paragraph("\nPROJEÇÃO TRIBUTÁRIA").setBold().setUnderline());

            if(tipoDeTributo == "Simples Nacional"){ document.add(new Paragraph("• Simples Nacional (6%): R$ " + totalReceitas.multiply(new BigDecimal("0.06")))); }

           else if ( tipoDeTributo == "Lucro Real" ){document.add(new Paragraph("• Lucro Real (15%): R$ " + (saldoCaixa.signum() > 0 ? saldoCaixa.multiply(new BigDecimal("0.15")) : "0.00")));
}
            
            return "PDF Gerencial gerado com sucesso!";

        } catch (Exception e) {
            return "Erro ao gerar PDF: " + e.getMessage();
        }
    }
}