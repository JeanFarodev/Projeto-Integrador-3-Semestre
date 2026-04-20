package Pi.demo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class LogAtividade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String acao; // "DELETAR" ou "EDITAR"
    private String detalhes; // O que mudou?
    private LocalDateTime dataHora;

    public LogAtividade() {}

    public LogAtividade(String acao, String detalhes) {
        this.acao = acao;
        this.detalhes = detalhes;
        this.dataHora = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public String getAcao() { return acao; }
    public String getDetalhes() { return detalhes; }
    public LocalDateTime getDataHora() { return dataHora; }
}