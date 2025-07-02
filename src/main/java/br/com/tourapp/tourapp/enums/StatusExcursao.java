package br.com.tourapp.tourapp.enums;

public enum StatusExcursao {
    RASCUNHO("Rascunho"),
    ATIVA("Ativa"),
    LOTADA("Lotada"),
    CANCELADA("Cancelada"),
    FINALIZADA("Finalizada");

    private final String descricao;

    StatusExcursao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
