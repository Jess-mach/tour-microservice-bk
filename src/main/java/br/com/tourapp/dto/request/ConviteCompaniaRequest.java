
// ============================================
// ConviteCompaniaRequest.java
// ============================================
package br.com.tourapp.dto.request;

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConviteCompaniaRequest {

    @Email(message = "Email deve ser válido")
    @NotBlank(message = "Email é obrigatório")
    private String emailConvidado;

    @NotNull(message = "Role na compania é obrigatório")
    private RoleCompania roleCompania;

    @Size(max = 500, message = "Mensagem do convite deve ter no máximo 500 caracteres")
    private String mensagemConvite;

    // Permissões customizadas (opcional)
    private Boolean podeCreiarExcursoes;
    private Boolean podeGerenciarUsuarios;
    private Boolean podeVerFinanceiro;
    private Boolean podeEditarCompania;
    private Boolean podeEnviarNotificacoes;
}
