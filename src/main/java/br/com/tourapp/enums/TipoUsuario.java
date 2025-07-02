package br.com.tourapp.enums;

public enum TipoUsuario {
    CLIENTE("Cliente"),
    ORGANIZADOR("Organizador"),
    ADMIN("Administrador");

    private final String descricao;

    TipoUsuario(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}

