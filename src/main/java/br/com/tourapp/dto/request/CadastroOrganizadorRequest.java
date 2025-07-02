package br.com.tourapp.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CadastroOrganizadorRequest {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(min = 2, max = 150, message = "Nome da empresa deve ter entre 2 e 150 caracteres")
    private String nomeEmpresa;

    @NotBlank(message = "Nome do responsável é obrigatório")
    @Size(min = 2, max = 100, message = "Nome do responsável deve ter entre 2 e 100 caracteres")
    private String nomeResponsavel;

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    private String senha;

    @Size(max = 20, message = "Telefone deve ter no máximo 20 caracteres")
    private String telefone;

    @Size(max = 100, message = "Chave PIX deve ter no máximo 100 caracteres")
    private String pixKey;

    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    private String cnpj;

    // Construtores
    public CadastroOrganizadorRequest() {}

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
}
