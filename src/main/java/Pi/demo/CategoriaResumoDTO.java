package Pi.demo;

import java.math.BigDecimal;

public class CategoriaResumoDTO {
    private String nome;
    private BigDecimal total;

    public CategoriaResumoDTO(String nome, BigDecimal total) {
        this.nome = nome;
        this.total = total;
    }

    // Getters para o Spring conseguir transformar em JSON
    public String getNome() { return nome; }
    public BigDecimal getTotal() { return total; }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}