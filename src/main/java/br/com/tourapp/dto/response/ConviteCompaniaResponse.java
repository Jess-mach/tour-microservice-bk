
// ============================================
// ConviteCompaniaResponse.java
// ============================================
package br.com.tourapp.dto.response;

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ConviteCompaniaResponse {

    private UUID id;
    private String emailConvidado;
    private String nomeCompania;
    private String cnpjCompania;
    private RoleCompania roleCompania;
    private String convidadoPor;
    private String mensagemConvite;
    private LocalDateTime dataConvite;
    private LocalDateTime dataExpiracao;
    private Boolean aceito;
    private LocalDateTime dataAceite;

    // Link do convite (se aplicável)
    private String linkConvite;

    // Status
    private Boolean expirado;
    private Boolean pendente;
}
