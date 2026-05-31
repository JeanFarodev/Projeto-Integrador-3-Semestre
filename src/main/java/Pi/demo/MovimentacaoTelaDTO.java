package Pi.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class MovimentacaoTelaDTO {

    public MovimentacaoTelaDTO(LocalDateTime data, String tipo, 
                                String descricao, String usuario, 
                                BigDecimal valor) {
        this.data = data;
        this.tipo = tipo;
        this.descricao = descricao;
        this.usuario = usuario;
        this.valor = valor;
    }

    private LocalDateTime data;
    private String tipo;
    private String descricao;
    private String usuario;
    private BigDecimal valor;

    public LocalDateTime getData()      { return data; }
    public void setData(LocalDateTime data) { this.data = data; }

    public String getTipo()             { return tipo; }
    public void setTipo(String tipo)    { this.tipo = tipo; }

    public String getDescricao()        { return descricao; }
    public void setDescricao(String d)  { this.descricao = d; }

    public String getUsuario()          { return usuario; }
    public void setUsuario(String u)    { this.usuario = u; }

    public BigDecimal getValor()        { return valor; }
    public void setValor(BigDecimal v)  { this.valor = v; }
}