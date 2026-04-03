package Pi.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // Define que esta classe é uma tabela no banco de dados
@Table(name = "usuarios_projeto") // Opcional: define o nome da tabela no MySQL
public class Usuario {

    @Id // Define que este atributo é a Chave Primária (PK)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Define o Auto-Incremento
    private Long id;

    private String nome;
    private String email;

    // Construtor vazio (obrigatório para o Hibernate)
    public Usuario() {
    }

    // Getters e Setters (Essenciais para o Spring ler e gravar os dados)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}