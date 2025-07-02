package br.com.tourapp.dto.response;

import br.com.tourapp.enums.StatusOrganizador;

import java.time.LocalDateTime;
import java.util.UUID;

public class OrganizadorResponse {

    private UUID id;
    private String nomeEmpresa;
    private String nomeResponsavel;
    private String email;
    private String telefone;
    private String pixKey;
    private String cnpj;
    private StatusOrganizador status;
    private LocalDateTime createdAt;

    // Construtores
    public OrganizadorResponse() {}

    // Getters e Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNomeEmpresa() { return nomeEmpresa; }
    public void setNomeEmpresa(String nomeEmpresa) { this.nomeEmpresa = nomeEmpresa; }

    public String getNomeResponsavel() { return nomeResponsavel; }
    public void setNomeResponsavel(String nomeResponsavel) { this.nomeResponsavel = nomeResponsavel; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getPixKey() { return pixKey; }
    public void setPixKey(String pixKey) { this.pixKey = pixKey; }

    public String getCnpj() { return cnpj; }
    public void setCnpj(String cnpj) { this.cnpj = cnpj; }

    public StatusOrganizador getStatus() { return status; }
    public void setStatus(StatusOrganizador status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
