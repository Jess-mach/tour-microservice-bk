package br.com.tourapp.entity;

import br.com.tourapp.dto.enums.StatusCompania;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "companias")
public class CompaniaEntity extends BaseEntity {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(min = 2, max = 150, message = "Nome da empresa deve ter entre 2 e 150 caracteres")
    @Column(name = "nome_empresa", nullable = false, length = 150)
    private String nomeEmpresa;

    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    @Column(name = "cnpj", length = 18, unique = true)
    private String cnpj;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Size(max = 200, message = "Site deve ter no máximo 200 caracteres")
    @Column(name = "site", length = 200)
    private String site;

    @Column(name = "logo_url")
    private String logoUrl;

    // Dados bancários
    @Size(max = 100, message = "Chave PIX deve ter no máximo 100 caracteres")
    @Column(name = "pix_key", length = 100)
    private String pixKey;

    // Endereço da empresa
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StatusCompania status = StatusCompania.ATIVA;

    // Relacionamentos
    @OneToMany(mappedBy = "compania", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserCompaniaEntity> usuarios;

    @OneToMany(mappedBy = "compania", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Excursao> excursoes;

    @OneToMany(mappedBy = "compania", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Notificacao> notificacoes;

    // Construtores
    public CompaniaEntity() {}

    public CompaniaEntity(String nomeEmpresa, String cnpj) {
        this.nomeEmpresa = nomeEmpresa;
        this.cnpj = cnpj;
    }

    // Métodos de negócio
    public boolean isAtiva() {
        return status == StatusCompania.ATIVA;
    }

    public boolean isPerfilCompleto() {
        return nomeEmpresa != null && !nomeEmpresa.trim().isEmpty() &&
                cnpj != null && !cnpj.trim().isEmpty() &&
                endereco != null && !endereco.trim().isEmpty() &&
                cidade != null && !cidade.trim().isEmpty() &&
                estado != null && !estado.trim().isEmpty();
    }

    public long getTotalExcursoes() {
        return excursoes != null ? excursoes.size() : 0;
    }

    public long getTotalUsuarios() {
        return usuarios != null ? usuarios.stream()
                .filter(UserCompaniaEntity::getAtivo)
                .count() : 0;
    }
}