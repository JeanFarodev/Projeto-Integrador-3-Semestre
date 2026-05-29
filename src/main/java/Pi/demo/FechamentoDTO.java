package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FechamentoDTO {
    private LocalDateTime data;
    private String operador;
    private BigDecimal valorSistema;
    private BigDecimal valorDeclarado;
    private BigDecimal diferenca;

    public FechamentoDTO(LocalDateTime data, String operador,
                         BigDecimal valorSistema, BigDecimal valorDeclarado,
                         BigDecimal diferenca) {
        this.data = data;
        this.operador = operador;
        this.valorSistema = valorSistema;
        this.valorDeclarado = valorDeclarado;
        this.diferenca = diferenca;
    }

    public LocalDateTime getData() { return data; }
    public String getOperador() { return operador; }
    public BigDecimal getValorSistema() { return valorSistema; }
    public BigDecimal getValorDeclarado() { return valorDeclarado; }
    public BigDecimal getDiferenca() { return diferenca; }
}
