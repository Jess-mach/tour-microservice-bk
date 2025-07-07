package br.com.tourapp.dto.response;

import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.dto.enums.StatusCompania;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO para retornar dados do relacionamento usuário-compania
 * Usado quando queremos ver informações completas sobre um usuário em uma empresa específica
 */
@Data
public class UserCompaniaResponse {

    // ============================================
    // IDs DE RELACIONAMENTO
    // ============================================

    private UUID id;                    // ID do relacionamento user_compania
    private UUID userId;                // ID do usuário
    private UUID companiaId;            // ID da compania

    // ============================================
    // DADOS DO RELACIONAMENTO
    // ============================================

    private RoleCompania roleCompania;  // Role do usuário na empresa (ADMIN, ORGANIZADOR, COLABORADOR)

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataIngresso;  // Quando o usuário entrou na empresa

    private Boolean ativo;              // Se o relacionamento está ativo

    // ============================================
    // PERMISSÕES ESPECÍFICAS NA COMPANIA
    // ============================================

    private Boolean podeCreiarExcursoes;     // Se pode criar excursões nesta empresa
    private Boolean podeGerenciarUsuarios;   // Se pode adicionar/remover usuários
    private Boolean podeVerFinanceiro;       // Se pode ver dados financeiros
    private Boolean podeEditarCompania;      // Se pode editar dados da empresa
    private Boolean podeEnviarNotificacoes;  // Se pode enviar notificações

    // ============================================
    // DADOS DO USUÁRIO
    // ============================================

    private String nomeUsuario;         // Nome completo do usuário
    private String emailUsuario;        // Email do usuário
    private String telefoneUsuario;     // Telefone do usuário
    private String profilePictureUsuario; // URL da foto de perfil
    private Boolean usuarioAtivo;       // Se o usuário está ativo no sistema

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime ultimoLoginUsuario; // Último login do usuário

    // ============================================
    // DADOS DA COMPANIA
    // ============================================

    private String nomeEmpresa;         // Nome da empresa
    private String cnpjEmpresa;         // CNPJ da empresa (parcial para privacidade)
    private String cidadeEmpresa;       // Cidade da empresa
    private String estadoEmpresa;       // Estado da empresa
    private StatusCompania statusCompania; // Status da empresa (ATIVA, SUSPENSA, etc)

    // ============================================
    // DADOS DO CONVITE (SE APLICÁVEL)
    // ============================================

    private String convidadoPor;        // Email de quem convidou este usuário

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataConvite;  // Quando foi enviado o convite

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataAceiteConvite; // Quando aceitou o convite (null se ainda não aceitou)

    private Boolean isConvitePendente;  // Se o convite ainda está pendente
    private String observacoesConvite;  // Observações sobre o convite/relacionamento

    // ============================================
    // ESTATÍSTICAS DO USUÁRIO NA COMPANIA
    // ============================================

    private Long totalExcursoesCriadas;      // Quantas excursões este usuário criou nesta empresa
    private Long totalNotificacoesEnviadas; // Quantas notificações enviou nesta empresa
    private Long totalInscricoesGerenciadas; // Quantas inscrições ele gerenciou

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataUltimaAcao;    // Última vez que fez algo na empresa

    // ============================================
    // METADADOS
    // ============================================

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;    // Quando o relacionamento foi criado

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;    // Última atualização do relacionamento

    private Long version;               // Versão para controle de concorrência

    // ============================================
    // CAMPOS CALCULADOS/DERIVADOS
    // ============================================

    /**
     * Retorna se o usuário é administrador desta empresa
     */
    public Boolean isAdmin() {
        return roleCompania == RoleCompania.ADMIN;
    }

    /**
     * Retorna se o usuário pode gerenciar excursões (criar/editar/deletar)
     */
    public Boolean podeGerenciarExcursoes() {
        return podeCreiarExcursoes || isAdmin();
    }

    /**
     * Retorna se o usuário tem permissão completa na empresa
     */
    public Boolean temPermissaoCompleta() {
        return isAdmin();
    }

    /**
     * Retorna descrição amigável do role
     */
    public String getDescricaoRole() {
        if (roleCompania == null) return "Indefinido";

        return switch (roleCompania) {
            case ADMIN -> "Administrador";
            case ORGANIZADOR -> "Organizador";
            case COLABORADOR -> "Colaborador";
        };
    }

    /**
     * Retorna se o convite expirou (se aplicável)
     */
    public Boolean isConviteExpirado() {
        if (!isConvitePendente || dataConvite == null) {
            return false;
        }

        // Convites expiram em 7 dias
        LocalDateTime dataExpiracao = dataConvite.plusDays(7);
        return LocalDateTime.now().isAfter(dataExpiracao);
    }

    /**
     * Retorna há quanto tempo está na empresa
     */
    public String getTempoNaEmpresa() {
        if (dataIngresso == null) return "N/A";

        LocalDateTime agora = LocalDateTime.now();
        long dias = java.time.Duration.between(dataIngresso, agora).toDays();

        if (dias < 30) {
            return dias + " dias";
        } else if (dias < 365) {
            long meses = dias / 30;
            return meses + " meses";
        } else {
            long anos = dias / 365;
            return anos + " anos";
        }
    }

    /**
     * Retorna nível de atividade na empresa
     */
    public String getNivelAtividade() {
        if (totalExcursoesCriadas == null) totalExcursoesCriadas = 0L;
        if (totalNotificacoesEnviadas == null) totalNotificacoesEnviadas = 0L;

        long atividadeTotal = totalExcursoesCriadas + totalNotificacoesEnviadas;

        if (atividadeTotal == 0) return "Inativo";
        if (atividadeTotal < 5) return "Baixo";
        if (atividadeTotal < 20) return "Médio";
        return "Alto";
    }

    /**
     * Retorna se está ativo tanto como usuário quanto na empresa
     */
    public Boolean isCompletoAtivo() {
        return ativo && usuarioAtivo;
    }

    // ============================================
    // CONSTRUTORES
    // ============================================

    public UserCompaniaResponse() {
        // Valores padrão
        this.ativo = true;
        this.usuarioAtivo = true;
        this.isConvitePendente = false;
        this.totalExcursoesCriadas = 0L;
        this.totalNotificacoesEnviadas = 0L;
        this.totalInscricoesGerenciadas = 0L;
    }

    // ============================================
    // MÉTODOS UTILITÁRIOS
    // ============================================

    /**
     * Retorna resumo do usuário para exibição
     */
    public String getResumoUsuario() {
        return String.format("%s (%s) - %s na %s",
                nomeUsuario,
                emailUsuario,
                getDescricaoRole(),
                nomeEmpresa);
    }

    /**
     * Retorna se precisa de atenção (convite pendente, inativo, etc)
     */
    public Boolean precisaAtencao() {
        return isConvitePendente ||
                !ativo ||
                !usuarioAtivo ||
                isConviteExpirado();
    }
}