package br.com.tourapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    // Dados básicos obrigatórios
    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    // Autenticação
    @Column(name = "password") // Para cadastro tradicional (futuro)
    private String password;

    @Column(name = "google_id", unique = true) // Para OAuth Google
    private String googleId;

    @Column(name = "profile_picture")
    private String profilePicture;

    // Dados de contato
    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Column(name = "phone", length = 20)
    private String phone;

    // Endereço
    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    @Column(name = "cep", length = 10)
    private String cep;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Column(name = "endereco", length = 200)
    private String endereco;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    @Column(name = "cidade", length = 100)
    private String cidade;

    @Size(max = 2, message = "Estado deve ter no máximo 2 caracteres")
    @Column(name = "estado", length = 2)
    private String estado;

    // Configurações de notificação
    @Column(name = "email_notifications", nullable = false)
    private Boolean emailNotifications = true;

    @Column(name = "sms_notifications", nullable = false)
    private Boolean smsNotifications = true;

    @Column(name = "push_token")
    private String pushToken;

    // Status e controle
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    // Subscription (mantido do sistema OAuth)
    @Column(name = "subscription_plan")
    private String subscriptionPlan;

    @Column(name = "subscription_expiry")
    private LocalDateTime subscriptionExpiry;

    // Relacionamentos
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Inscricao> inscricoes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCompaniaEntity> companias;

    // Construtores
    public UserEntity() {}

    public UserEntity(String email, String fullName) {
        this.email = email;
        this.fullName = fullName;
        this.active = true;
        this.emailNotifications = true;
        this.smsNotifications = true;
    }

    public UserEntity(String email, String fullName, String googleId) {
        this(email, fullName);
        this.googleId = googleId;
    }

    // Métodos de negócio
    public boolean isAtivo() {
        return active != null && active;
    }

    public boolean temPerfilCompleto() {
        return fullName != null && !fullName.trim().isEmpty() &&
                phone != null && !phone.trim().isEmpty() &&
                endereco != null && !endereco.trim().isEmpty() &&
                cidade != null && !cidade.trim().isEmpty() &&
                estado != null && !estado.trim().isEmpty();
    }

    public boolean isCliente() {
        return roles.stream().anyMatch(role -> "ROLE_CLIENTE".equals(role.getName()));
    }

    public boolean isOrganizador() {
        return roles.stream().anyMatch(role -> "ROLE_ORGANIZADOR".equals(role.getName()));
    }

    public boolean isAdmin() {
        return roles.stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
    }

    public boolean temAssinaturaAtiva() {
        return subscriptionExpiry != null && subscriptionExpiry.isAfter(LocalDateTime.now());
    }

    public boolean aceitaEmailNotificacoes() {
        return emailNotifications != null && emailNotifications;
    }

    public boolean aceitaSmsNotificacoes() {
        return smsNotifications != null && smsNotifications;
    }

    public boolean temPushToken() {
        return pushToken != null && !pushToken.trim().isEmpty();
    }

    // Métodos para gerenciar companias
    public List<UserCompaniaEntity> getCompaniasAtivas() {
        return companias != null ?
                companias.stream()
                        .filter(UserCompaniaEntity::getAtivo)
                        .toList() :
                List.of();
    }

    public boolean fazParteDeCompania(String companiaId) {
        return getCompaniasAtivas().stream()
                .anyMatch(uc -> uc.getCompania().getId().toString().equals(companiaId));
    }

    public boolean isAdminDeCompania(String companiaId) {
        return getCompaniasAtivas().stream()
                .anyMatch(uc -> uc.getCompania().getId().toString().equals(companiaId) &&
                        uc.isAdmin());
    }

    public boolean podeGerenciarExcursoes(String companiaId) {
        return getCompaniasAtivas().stream()
                .anyMatch(uc -> uc.getCompania().getId().toString().equals(companiaId) &&
                        uc.podeGerenciarExcursoes());
    }

    // Métodos para atualização
    public void atualizarUltimoLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public void ativar() {
        this.active = true;
    }

    public void desativar() {
        this.active = false;
    }

    public void atualizarPushToken(String novoToken) {
        this.pushToken = novoToken;
    }

    public void atualizarConfiguracaoNotificacao(Boolean email, Boolean sms) {
        if (email != null) {
            this.emailNotifications = email;
        }
        if (sms != null) {
            this.smsNotifications = sms;
        }
    }

    public void completarPerfil(String phone, String cep, String endereco, String cidade, String estado) {
        this.phone = phone;
        this.cep = cep;
        this.endereco = endereco;
        this.cidade = cidade;
        this.estado = estado;
    }

    public String getNome() {
        return this.fullName;
    }

    // Override do equals e hashCode da BaseEntity são mantidos
}