
// ============================================
// UserCompaniaRequest.java
// ============================================
package br.com.tourapp.dto.request;

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class UserCompaniaRequest {

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String email;

    @NotNull(message = "Role na compania é obrigatório")
    private RoleCompania roleCompania;

    @NotNull(message = "ID da compania é obrigatório")
    private UUID companiaId;

    private Boolean podeCreiarExcursoes = true;
    private Boolean podeGerenciarUsuarios = false;
    private Boolean podeVerFinanceiro = true;
    private Boolean podeEditarCompania = false;
    private Boolean podeEnviarNotificacoes = true;

    @Size(max = 500, message = "Observações devem ter no máximo 500 caracteres")
    private String observacoes;
}