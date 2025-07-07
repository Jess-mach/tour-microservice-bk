package br.com.tourapp.entity;

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "user_compania",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "compania_id"}))
public class UserCompaniaEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "Usuário é obrigatório")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compania_id", nullable = false)
    @NotNull(message = "Compania é obrigatória")
    private CompaniaEntity compania;

    @Enumerated(EnumType.STRING)
    @Column(name = "role_compania", nullable = false)
    @NotNull(message = "Role na compania é obrigatório")
    private RoleCompania roleCompania;

    @Column(name = "data_ingresso", nullable = false)
    private LocalDateTime dataIngresso = LocalDateTime.now();

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    // Permissões específicas
    @Column(name = "pode_criar_excursoes", nullable = false)
    private Boolean podeCreiarExcursoes = true;

    @Column(name = "pode_gerenciar_usuarios", nullable = false)
    private Boolean podeGerenciarUsuarios = false;

    @Column(name = "pode_ver_financeiro", nullable = false)
    private Boolean podeVerFinanceiro = true;

    @Column(name = "pode_editar_compania", nullable = false)
    private Boolean podeEditarCompania = false;

    @Column(name = "pode_enviar_notificacoes", nullable = false)
    private Boolean podeEnviarNotificacoes = true;

    // Dados do convite (se aplicável)
    @Column(name = "convidado_por")
    private String convidadoPor;

    @Column(name = "data_aceite_convite")
    private LocalDateTime dataAceiteConvite;

    @Column(name = "observacoes", length = 500)
    private String observacoes;

    // Construtores
    public UserCompaniaEntity() {}

    public UserCompaniaEntity(UserEntity user, CompaniaEntity compania, RoleCompania roleCompania) {
        this.user = user;
        this.compania = compania;
        this.roleCompania = roleCompania;
        this.dataIngresso = LocalDateTime.now();
        configurarPermissoesPorRole();
    }

    // Métodos de negócio
    public boolean isAdmin() {
        return roleCompania == RoleCompania.ADMIN;
    }

    public boolean isOrganizador() {
        return roleCompania == RoleCompania.ORGANIZADOR || isAdmin();
    }

    public boolean isColaborador() {
        return roleCompania == RoleCompania.COLABORADOR;
    }

    public boolean temPermissaoCompleta() {
        return isAdmin();
    }

    public boolean podeGerenciarExcursoes() {
        return (isOrganizador() && podeCreiarExcursoes) || isAdmin();
    }

    public boolean podeVerDashboard() {
        return podeVerFinanceiro || isAdmin();
    }

    public void configurarPermissoesPorRole() {
        switch (roleCompania) {
            case ADMIN:
                this.podeCreiarExcursoes = true;
                this.podeGerenciarUsuarios = true;
                this.podeVerFinanceiro = true;
                this.podeEditarCompania = true;
                this.podeEnviarNotificacoes = true;
                break;
            case ORGANIZADOR:
                this.podeCreiarExcursoes = true;
                this.podeGerenciarUsuarios = false;
                this.podeVerFinanceiro = true;
                this.podeEditarCompania = false;
                this.podeEnviarNotificacoes = true;
                break;
            case COLABORADOR:
                this.podeCreiarExcursoes = false;
                this.podeGerenciarUsuarios = false;
                this.podeVerFinanceiro = false;
                this.podeEditarCompania = false;
                this.podeEnviarNotificacoes = false;
                break;
        }
    }

    public void promoverPara(RoleCompania novoRole) {
        this.roleCompania = novoRole;
        configurarPermissoesPorRole();
    }

    public void desativar() {
        this.ativo = false;
    }

    public void ativar() {
        this.ativo = true;
    }

    public boolean isConvidado() {
        return convidadoPor != null && dataAceiteConvite == null;
    }

    public void aceitarConvite() {
        this.dataAceiteConvite = LocalDateTime.now();
    }
}