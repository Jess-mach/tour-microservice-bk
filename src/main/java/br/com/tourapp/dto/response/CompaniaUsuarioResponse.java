package br.com.tourapp.dto.response;


// ============================================
// CompaniaUsuarioResponse.java
// ============================================

import br.com.tourapp.dto.enums.RoleCompania;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompaniaUsuarioResponse {

    private UUID userId;
    private String nome;
    private String email;
    private String telefone;
    private String profilePicture;
    private Boolean ativo;

    // Dados na compania
    private RoleCompania roleCompania;
    private LocalDateTime dataIngresso;
    private Boolean ativoNaCompania;

    // Permissões
    private Boolean podeCreiarExcursoes;
    private Boolean podeGerenciarUsuarios;
    private Boolean podeVerFinanceiro;
    private Boolean podeEditarCompania;
    private Boolean podeEnviarNotificacoes;

    // Estatísticas
    private Long totalExcursoesCriadas;
    private Long totalNotificacoesEnviadas;
    private LocalDateTime ultimoLogin;

    // Status do convite
    private Boolean isConvitePendente;
    private String convidadoPor;
    private LocalDateTime dataAceiteConvite;
}
