package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FechamentoDTO {
    private LocalDateTime dataHora;
    private String operador;
    private BigDecimal totalSistema;
    private BigDecimal totalContado;
    private BigDecimal diferenca;

    public FechamentoDTO(LocalDateTime dataHora, String operador,
                         BigDecimal totalSistema, BigDecimal totalContado,
                         BigDecimal diferenca) {
        this.dataHora = dataHora;
        this.operador = operador;
        this.totalSistema = totalSistema;
        this.totalContado = totalContado;
        this.diferenca = diferenca;
    }

    public LocalDateTime getDataHora()      { return dataHora; }
    public String getOperador()             { return operador; }
    public BigDecimal getTotalSistema()     { return totalSistema; }
    public BigDecimal getTotalContado()     { return totalContado; }
    public BigDecimal getDiferenca()        { return diferenca; }
}