package br.com.tourapp.entity;

import br.com.tourapp.enums.StatusOrganizador;
import br.com.tourapp.enums.TipoUsuario;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
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

    // Campo adicional para nome completo (usado no complete profile)
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String nome;

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

    // Campos de endereço
    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    @Column(length = 10)
    private String cep;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    @Column(length = 200)
    private String endereco;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    @Column(length = 100)
    private String cidade;

    @Size(max = 2, message = "Estado deve ter no máximo 2 caracteres")
    @Column(length = 2)
    private String estado;

    @Column(name = "pix_key", length = 100)
    private String pixKey;

    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    @Column(length = 18, unique = true)
    private String cnpj;

    // Campos adicionais para o perfil
    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Column(length = 1000)
    private String descricao;

    @Size(max = 200, message = "Site deve ter no máximo 200 caracteres")
    @Column(length = 200)
    private String site;

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

    // Método para verificar se perfil está completo
    public boolean isPerfilCompleto() {
        return nome != null && !nome.trim().isEmpty() &&
                telefone != null && !telefone.trim().isEmpty() &&
                nomeEmpresa != null && !nomeEmpresa.trim().isEmpty() &&
                cnpj != null && !cnpj.trim().isEmpty() &&
                endereco != null && !endereco.trim().isEmpty() &&
                cidade != null && !cidade.trim().isEmpty() &&
                estado != null && !estado.trim().isEmpty();
    }

    // Getter para nome que fallback para nomeResponsavel se nome estiver vazio
    public String getNome() {
        if (nome != null && !nome.trim().isEmpty()) {
            return nome;
        }
        return nomeResponsavel;
    }
}