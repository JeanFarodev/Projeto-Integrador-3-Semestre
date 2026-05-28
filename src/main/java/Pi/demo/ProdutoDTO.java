package Pi.demo;

import java.math.BigDecimal;

public class ProdutoDTO {
    private String nome;
    private String categoria;
    private Integer estoque;
    private BigDecimal preco;

    public ProdutoDTO(String nome, String categoria, Integer estoque, BigDecimal preco) {
        this.nome = nome;
        this.categoria = categoria;
        this.estoque = estoque;
        this.preco = preco;
    }

    public String getNome() { return nome; }
    public String getCategoria() { return categoria; }
    public Integer getEstoque() { return estoque; }
    public BigDecimal getPreco() { return preco; }
}
