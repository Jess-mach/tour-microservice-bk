package br.com.tourapp.dto.enums;

/**
 * Enum para status da compania
 */
public enum StatusCompania {
    ATIVA("Ativa"),
    SUSPENSA("Suspensa"),
    INATIVA("Inativa"),
    PENDENTE_APROVACAO("Pendente de Aprovação");

    private final String descricao;

    StatusCompania(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean permiteOperacoes() {
        return this == ATIVA;
    }

    public boolean precisaAprovacao() {
        return this == PENDENTE_APROVACAO;
    }
}

