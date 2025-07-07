
// ============================================
// AlterarRoleRequest.java
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
public class AlterarRoleRequest {

    @NotNull(message = "ID do usuário é obrigatório")
    private UUID userId;

    @NotNull(message = "Novo role é obrigatório")
    private RoleCompania novoRole;

    @Size(max = 500, message = "Justificativa deve ter no máximo 500 caracteres")
    private String justificativa;

    // Permissões customizadas (opcional - senão usa padrão do role)
    private Boolean podeCreiarExcursoes;
    private Boolean podeGerenciarUsuarios;
    private Boolean podeVerFinanceiro;
    private Boolean podeEditarCompania;
    private Boolean podeEnviarNotificacoes;
}
