package br.com.tourapp.tourapp.enums;

public enum TipoNotificacao {
    INFO("Informação"),
    PROMOCAO("Promoção"),
    LEMBRETE("Lembrete"),
    URGENTE("Urgente"),
    CONFIRMACAO("Confirmação"),
    CANCELAMENTO("Cancelamento");

    private final String descricao;

    TipoNotificacao(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
