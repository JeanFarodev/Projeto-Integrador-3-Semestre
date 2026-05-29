package Pi.demo;

public class RelatorioDTO {
    private String titulo;
    private String descricao;

    public RelatorioDTO(String titulo, String descricao) {
        this.titulo = titulo;
        this.descricao = descricao;
    }

    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
}
