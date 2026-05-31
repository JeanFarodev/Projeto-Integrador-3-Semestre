package Pi.demo;

import java.math.BigDecimal;

public class ProdutoDTO {
    private String nome;
    private String categoria;
    private int quantidade;
    private BigDecimal preco;

    public ProdutoDTO(String nome, String categoria, int quantidade, BigDecimal preco) {
        this.nome = nome;
        this.categoria = categoria;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public String getNome()          { return nome; }
    public String getCategoria()     { return categoria; }
    public int getQuantidade()       { return quantidade; }
    public BigDecimal getPreco()     { return preco; }

    public void setNome(String nome)             { this.nome = nome; }
    public void setCategoria(String categoria)   { this.categoria = categoria; }
    public void setQuantidade(int quantidade)    { this.quantidade = quantidade; }
    public void setPreco(BigDecimal preco)       { this.preco = preco; }

}