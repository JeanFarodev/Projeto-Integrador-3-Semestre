package Pi.demo;

public class UsuarioDTO {
    private String nome;
    private String cargo;

    public UsuarioDTO(String nome, String cargo) {
        this.nome = nome;
        this.cargo = cargo;
    }

    public String getNome() { return nome; }
    public String getCargo() { return cargo; }
}
