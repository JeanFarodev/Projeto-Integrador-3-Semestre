package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimentacaoTelaDTO {
    private LocalDateTime data;
    private String tipo;      // "ENTRADA" ou "SAIDA"
    private String descricao;
    private String usuario;
    private BigDecimal valor;

    public LocalDateTime getData() { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
}
