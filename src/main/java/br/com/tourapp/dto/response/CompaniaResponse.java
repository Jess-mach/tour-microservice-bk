package br.com.tourapp.dto.response;


// ============================================
// CompaniaResponse.java
// ============================================

import br.com.tourapp.dto.enums.StatusCompania;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CompaniaResponse {

    private UUID id;
    private String nomeEmpresa;
    private String cnpj;
    private String descricao;
    private String site;
    private String logoUrl;
    private String pixKey;
    private String cep;
    private String endereco;
    private String cidade;
    private String estado;
    private StatusCompania status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Permissões
    private Boolean podeCreiarExcursoes;
    private Boolean podeGerenciarUsuarios;
    private Boolean podeVerFinanceiro;
    private Boolean podeEditarCompania;
    private Boolean podeEnviarNotificacoes;

    // Dados do usuário
    private String nomeUsuario;
    private String emailUsuario;
    private String telefoneUsuario;
    private String profilePictureUsuario;

    // Dados da compania
    private String statusCompania;

    // Dados do convite
    private String convidadoPor;
    private LocalDateTime dataAceiteConvite;
    private String observacoes;
    private Boolean isConvitePendente;

}
