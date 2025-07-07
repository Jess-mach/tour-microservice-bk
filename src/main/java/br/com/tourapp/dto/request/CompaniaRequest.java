// ============================================
// CompaniaRequest.java
// ============================================
package br.com.tourapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CompaniaRequest {

    @NotBlank(message = "Nome da empresa é obrigatório")
    @Size(min = 2, max = 150, message = "Nome da empresa deve ter entre 2 e 150 caracteres")
    private String nomeEmpresa;

    @Size(max = 18, message = "CNPJ deve ter no máximo 18 caracteres")
    private String cnpj;

    @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
    private String descricao;

    @Size(max = 200, message = "Site deve ter no máximo 200 caracteres")
    private String site;

    @Size(max = 100, message = "Chave PIX deve ter no máximo 100 caracteres")
    private String pixKey;

    @Size(max = 10, message = "CEP deve ter no máximo 10 caracteres")
    private String cep;

    @Size(max = 200, message = "Endereço deve ter no máximo 200 caracteres")
    private String endereco;

    @Size(max = 100, message = "Cidade deve ter no máximo 100 caracteres")
    private String cidade;

    @Size(max = 2, message = "Estado deve ter no máximo 2 caracteres")
    private String estado;
}
