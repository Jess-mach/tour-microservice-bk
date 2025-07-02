package br.com.tourapp.enums;

public enum StatusOrganizador {
    PENDENTE("Pendente de aprovação"),
    ATIVO("Ativo"),
    INATIVO("Inativo"),
    SUSPENSO("Suspenso");

    private final String descricao;

    StatusOrganizador(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
