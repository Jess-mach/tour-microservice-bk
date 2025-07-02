package br.com.tourapp.tourapp.entity;

import br.com.tourapp.tourapp.enums.StatusOrganizador;
import br.com.tourapp.tourapp.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Entity
@Table(name = "organizadores")
public class Organizador extends BaseEntity {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(min = 2, max = 150, message = "Nome da empresa deve ter entre 2 e 150 caracteres")
    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nomeEmpresa;

    @NotBlank(message = "Nome do responsável é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do responsável deve ter entre 2 e 100 caracteres")
    @Column(name = "nome_responsavel", nullable = false, length = 100)
    private String nomeResponsavel;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Column(nullable = false)
    private String senha;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    @Column(length = 20)
    private String telefone;

    @Column(name = "pix_key", length = 100)
    private String pixKey;

    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    @Column(length = 18)
    private String cnpj;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrganizador status = StatusOrganizador.PENDENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_usuario", nullable = false)
    private TipoUsuario tipoUsuario = TipoUsuario.ORGANIZADOR;

    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Excursao> excursoes;

    @OneToMany(mappedBy = "organizador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notificacao> notificacoes;

    // Construtores
    public Organizador() {}

    public Organizador(String nomeEmpresa, String nomeResponsavel, String email, String senha) {
        this.nomeEmpresa = nomeEmpresa;
        this.nomeResponsavel = nomeResponsavel;
        this.email = email;
        this.senha = senha;
    }

    // Getters e Setters
    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public StatusOrganizador getStatus() { return status; }
    public void setStatus(StatusOrganizador status) { this.status = status; }

    public TipoUsuario getTipoUsuario() { return tipoUsuario; }
    public void setTipoUsuario(TipoUsuario tipoUsuario) { this.tipoUsuario = tipoUsuario; }

    public List<Excursao> getExcursoes() { return excursoes; }
    public void setExcursoes(List<Excursao> excursoes) { this.excursoes = excursoes; }

    public List<Notificacao> getNotificacoes() { return notificacoes; }
    public void setNotificacoes(List<Notificacao> notificacoes) { this.notificacoes = notificacoes; }
}
