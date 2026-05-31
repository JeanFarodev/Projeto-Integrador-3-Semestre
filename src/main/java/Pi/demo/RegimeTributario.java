package Pi.demo;

public enum RegimeTributario {
    SIMPLES_NACIONAL("Simples Nacional"),
    LUCRO_REAL("Lucro Real"),
    LUCRO_PRESUMIDO("Lucro Presumido"),
    MEI("MEI"),
    TABELA_PROGRESSIVA_PF("Tabela Progressiva PF");

    private final String descricao;

    RegimeTributario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() { return descricao; }
}