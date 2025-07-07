package br.com.tourapp.dto.enums;


/**
 * Enum para roles específicos dentro de uma compania
 */
public enum RoleCompania {
    ADMIN("Administrador", "Acesso total à compania"),
    ORGANIZADOR("Organizador", "Pode criar e gerenciar excursões"),
    COLABORADOR("Colaborador", "Acesso limitado");

    private final String nome;
    private final String descricao;

    RoleCompania(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isAdministrador() {
        return this == ADMIN;
    }

    public boolean podeGerenciarExcursoes() {
        return this == ADMIN || this == ORGANIZADOR;
    }

    public boolean podeVerFinanceiro() {
        return this == ADMIN || this == ORGANIZADOR;
    }

    public boolean podeGerenciarUsuarios() {
        return this == ADMIN;
    }

    /**
     * Verifica se pode ser promovido para outro role
     */
    public boolean podeSerPromovidoPara(RoleCompania novoRole) {
        // COLABORADOR pode ser promovido para qualquer role
        if (this == COLABORADOR) {
            return true;
        }

        // ORGANIZADOR pode ser promovido apenas para ADMIN
        if (this == ORGANIZADOR) {
            return novoRole == ADMIN;
        }

        // ADMIN não pode ser "promovido" (já é o máximo)
        return false;
    }

    /**
     * Verifica se pode ser rebaixado para outro role
     */
    public boolean podeSerRebaixadoPara(RoleCompania novoRole) {
        // ADMIN pode ser rebaixado para qualquer role
        if (this == ADMIN) {
            return novoRole != ADMIN;
        }

        // ORGANIZADOR pode ser rebaixado apenas para COLABORADOR
        if (this == ORGANIZADOR) {
            return novoRole == COLABORADOR;
        }

        // COLABORADOR não pode ser rebaixado (já é o mínimo)
        return false;
    }
}