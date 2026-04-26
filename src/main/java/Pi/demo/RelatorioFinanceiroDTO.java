package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;

public class RelatorioFinanceiroDTO {
    private String empresaNome;
    private BigDecimal totalReceitas;
    private BigDecimal totalDespesas;
    private BigDecimal saldoCaixa;
    private BigDecimal impostoEstimado;
    private String analiseSaude;
    private String sugestaoRegime;
    private List<Lancamento> lancamentos;

    @JsonFormat(pattern = "dd/MM/yyyy") // Define como a data aparecerá no JSON
    private LocalDate data;

    // Construtor
    public RelatorioFinanceiroDTO(String empresaNome, BigDecimal totalReceitas, BigDecimal totalDespesas, 
                                 BigDecimal saldoCaixa, BigDecimal impostoEstimado, String analiseSaude, 
                                 String sugestaoRegime, List<Lancamento> lancamentos) {
        this.empresaNome = empresaNome;
        this.totalReceitas = totalReceitas;
        this.totalDespesas = totalDespesas;
        this.saldoCaixa = saldoCaixa;
        this.impostoEstimado = impostoEstimado;
        this.analiseSaude = analiseSaude;
        this.sugestaoRegime = sugestaoRegime;
        this.lancamentos = lancamentos;
    }

    // Getters (Essenciais para o JSON funcionar)
    public String getEmpresaNome() { return empresaNome; }
    public BigDecimal getTotalReceitas() { return totalReceitas; }
    public BigDecimal getTotalDespesas() { return totalDespesas; }
    public BigDecimal getSaldoCaixa() { return saldoCaixa; }
    public BigDecimal getImpostoEstimado() { return impostoEstimado; }
    public String getAnaliseSaude() { return analiseSaude; }
    public String getSugestaoRegime() { return sugestaoRegime; }
    public List<Lancamento> getLancamentos() { return lancamentos; }

    // Setters (Caso precise alterar algum valor manualmente)
    public void setEmpresaNome(String empresaNome) { this.empresaNome = empresaNome; }
    public void setTotalReceitas(BigDecimal totalReceitas) { this.totalReceitas = totalReceitas; }
    public void setTotalDespesas(BigDecimal totalDespesas) { this.totalDespesas = totalDespesas; }
    public void setSaldoCaixa(BigDecimal saldoCaixa) { this.saldoCaixa = saldoCaixa; }
    public void setImpostoEstimado(BigDecimal impostoEstimado) { this.impostoEstimado = impostoEstimado; }
    public void setAnaliseSaude(String analiseSaude) { this.analiseSaude = analiseSaude; }
    public void setSugestaoRegime(String sugestaoRegime) { this.sugestaoRegime = sugestaoRegime; }
    public void setLancamentos(List<Lancamento> lancamentos) { this.lancamentos = lancamentos; }
}