package br.com.tourapp.service;

import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.entity.UserCompaniaEntity;
import br.com.tourapp.exception.AccessDeniedException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.CompaniaRepository;
import br.com.tourapp.repository.ExcursaoRepository;
import br.com.tourapp.repository.UserCompaniaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Serviço responsável por validar permissões e acessos relacionados às companias
 */
@Service
@Transactional(readOnly = true)
public class CompaniaSecurityService {

    private static final Logger logger = LoggerFactory.getLogger(CompaniaSecurityService.class);

    private final UserCompaniaRepository userCompaniaRepository;
    private final CompaniaRepository companiaRepository;
    private final ExcursaoRepository excursaoRepository;

    public CompaniaSecurityService(UserCompaniaRepository userCompaniaRepository,
                                   CompaniaRepository companiaRepository,
                                   ExcursaoRepository excursaoRepository) {
        this.userCompaniaRepository = userCompaniaRepository;
        this.companiaRepository = companiaRepository;
        this.excursaoRepository = excursaoRepository;
    }

    // ============================================
    // MÉTODOS PÚBLICOS PARA VALIDAÇÃO
    // ============================================

    /**
     * Verifica se o usuário tem acesso à compania
     */
    public boolean temAcessoCompania(UUID userId, UUID companiaId) {
        logger.debug("Verificando acesso do usuário {} à compania {}", userId, companiaId);

        return userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(userId, companiaId);
    }

    /**
     * Verifica se o usuário pode gerenciar outros usuários da compania
     */
    public boolean podeGerenciarUsuarios(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} pode gerenciar usuários da compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(uc -> uc.getPodeGerenciarUsuarios() || uc.isAdmin())
                .orElse(false);
    }

    /**
     * Verifica se o usuário pode ver dados financeiros da compania
     */
    public boolean podeVerFinanceiro(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} pode ver financeiro da compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(UserCompaniaEntity::getPodeVerFinanceiro)
                .orElse(false);
    }

    /**
     * Verifica se o usuário pode editar dados da compania
     */
    public boolean podeEditarCompania(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} pode editar compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(uc -> uc.getPodeEditarCompania() || uc.isAdmin())
                .orElse(false);
    }

    /**
     * Verifica se o usuário pode criar excursões na compania
     */
    public boolean podeCreiarExcursoes(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} pode criar excursões na compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(UserCompaniaEntity::getPodeCreiarExcursoes)
                .orElse(false);
    }

    /**
     * Verifica se o usuário pode enviar notificações na compania
     */
    public boolean podeEnviarNotificacoes(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} pode enviar notificações na compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(UserCompaniaEntity::getPodeEnviarNotificacoes)
                .orElse(false);
    }

    /**
     * Verifica se o usuário pode editar uma excursão específica
     */
    public boolean podeEditarExcursao(UUID userId, UUID excursaoId) {
        logger.debug("Verificando se usuário {} pode editar excursão {}", userId, excursaoId);

        return excursaoRepository.findById(excursaoId)
                .map(excursao -> {
                    // Pode editar se tem permissão na compania OU se foi ele quem criou
                    UUID companiaId = excursao.getCompania().getId();
                    boolean temPermissaoCompania = podeCreiarExcursoes(userId, companiaId);
                    boolean isCriador = excursao.getCriador().getId().equals(userId);

                    return temPermissaoCompania || isCriador;
                })
                .orElse(false);
    }

    /**
     * Verifica se o usuário é admin da compania
     */
    public boolean isAdminCompania(UUID userId, UUID companiaId) {
        logger.debug("Verificando se usuário {} é admin da compania {}", userId, companiaId);

        return userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .map(UserCompaniaEntity::isAdmin)
                .orElse(false);
    }

    /**
     * Obter o relacionamento user-compania se existir
     */
    public Optional<UserCompaniaEntity> obterRelacionamento(UUID userId, UUID companiaId) {
        return userCompaniaRepository.findByUserAndCompania(userId, companiaId);
    }

    // ============================================
    // MÉTODOS DE VALIDAÇÃO COM EXCEÇÃO
    // ============================================

    /**
     * Valida acesso à compania ou lança exceção
     */
    public void validarAcessoCompania(UUID userId, UUID companiaId) {
        if (!temAcessoCompania(userId, companiaId)) {
            logger.warn("Usuário {} tentou acessar compania {} sem permissão", userId, companiaId);
            throw new AccessDeniedException("Acesso negado à compania");
        }
    }

    /**
     * Valida permissão para gerenciar usuários ou lança exceção
     */
    public void validarPermissaoGerenciarUsuarios(UUID userId, UUID companiaId) {
        validarAcessoCompania(userId, companiaId);

        if (!podeGerenciarUsuarios(userId, companiaId)) {
            logger.warn("Usuário {} tentou gerenciar usuários da compania {} sem permissão", userId, companiaId);
            throw new AccessDeniedException("Sem permissão para gerenciar usuários");
        }
    }

    /**
     * Valida permissão para criar excursões ou lança exceção
     */
    public void validarPermissaoCriarExcursoes(UUID userId, UUID companiaId) {
        validarAcessoCompania(userId, companiaId);

        if (!podeCreiarExcursoes(userId, companiaId)) {
            logger.warn("Usuário {} tentou criar excursão na compania {} sem permissão", userId, companiaId);
            throw new AccessDeniedException("Sem permissão para criar excursões");
        }
    }

    /**
     * Valida permissão para editar excursão ou lança exceção
     */
    public void validarPermissaoEditarExcursao(UUID userId, UUID excursaoId) {
        if (!podeEditarExcursao(userId, excursaoId)) {
            logger.warn("Usuário {} tentou editar excursão {} sem permissão", userId, excursaoId);
            throw new AccessDeniedException("Sem permissão para editar esta excursão");
        }
    }

    /**
     * Valida permissão para ver financeiro ou lança exceção
     */
    public void validarPermissaoVerFinanceiro(UUID userId, UUID companiaId) {
        validarAcessoCompania(userId, companiaId);

        if (!podeVerFinanceiro(userId, companiaId)) {
            logger.warn("Usuário {} tentou acessar financeiro da compania {} sem permissão", userId, companiaId);
            throw new AccessDeniedException("Sem permissão para ver dados financeiros");
        }
    }

    /**
     * Valida permissão para editar compania ou lança exceção
     */
    public void validarPermissaoEditarCompania(UUID userId, UUID companiaId) {
        validarAcessoCompania(userId, companiaId);

        if (!podeEditarCompania(userId, companiaId)) {
            logger.warn("Usuário {} tentou editar compania {} sem permissão", userId, companiaId);
            throw new AccessDeniedException("Sem permissão para editar a compania");
        }
    }

    // ============================================
    // MÉTODOS DE NEGÓCIO
    // ============================================

    /**
     * Verifica se é seguro remover um usuário da compania
     * (não pode remover o último admin)
     */
    public boolean podeRemoverUsuario(UUID userId, UUID companiaId, UUID usuarioParaRemover) {
        // Se não é admin, não pode remover ninguém
        if (!isAdminCompania(userId, companiaId)) {
            return false;
        }

        // Verifica se o usuário a ser removido é admin
        Optional<UserCompaniaEntity> relacionamento = obterRelacionamento(usuarioParaRemover, companiaId);
        if (relacionamento.isEmpty() || !relacionamento.get().isAdmin()) {
            return true; // Pode remover não-admins
        }

        // Se é admin, verifica se não é o último
        Long outrosAdmins = userCompaniaRepository.countOtherAdminsByCompania(companiaId, usuarioParaRemover);
        return outrosAdmins > 0;
    }

    /**
     * Verifica se pode alterar o role de um usuário
     */
    public boolean podeAlterarRole(UUID userId, UUID companiaId, UUID usuarioAlvo, RoleCompania novoRole) {
        // Só admin pode alterar roles
        if (!isAdminCompania(userId, companiaId)) {
            return false;
        }

        // Não pode se rebaixar se for o último admin
        if (userId.equals(usuarioAlvo) && novoRole != RoleCompania.ADMIN) {
            Long outrosAdmins = userCompaniaRepository.countOtherAdminsByCompania(companiaId, userId);
            return outrosAdmins > 0;
        }

        return true;
    }

    /**
     * Obter compania padrão do usuário (primeira compania ativa)
     */
    public Optional<UUID> obterCompaniaPadrao(UUID userId) {
        return userCompaniaRepository.findByUserIdAndAtivoTrue(userId)
                .stream()
                .findFirst()
                .map(uc -> uc.getCompania().getId());
    }

    /**
     * Validar se compania existe e está ativa
     */
    public void validarCompaniaAtiva(UUID companiaId) {
        companiaRepository.findById(companiaId)
                .filter(compania -> compania.isAtiva())
                .orElseThrow(() -> new NotFoundException("Compania não encontrada ou inativa"));
    }

    // ============================================
    // MÉTODOS PARA USO EM @PreAuthorize
    // ============================================

    /**
     * Para uso em anotações @PreAuthorize
     * Exemplo: @PreAuthorize("@companiaSecurityService.hasAccessToCompania(authentication.principal.id, #companiaId)")
     */
    public boolean hasAccessToCompania(UUID userId, UUID companiaId) {
        return temAcessoCompania(userId, companiaId);
    }

    /**
     * Para uso em anotações @PreAuthorize
     */
    public boolean canManageUsers(UUID userId, UUID companiaId) {
        return podeGerenciarUsuarios(userId, companiaId);
    }

    /**
     * Para uso em anotações @PreAuthorize
     */
    public boolean canCreateExcursions(UUID userId, UUID companiaId) {
        return podeCreiarExcursoes(userId, companiaId);
    }

    /**
     * Para uso em anotações @PreAuthorize
     */
    public boolean canEditExcursion(UUID userId, UUID excursaoId) {
        return podeEditarExcursao(userId, excursaoId);
    }

    /**
     * Para uso em anotações @PreAuthorize
     */
    public boolean canViewFinancial(UUID userId, UUID companiaId) {
        return podeVerFinanceiro(userId, companiaId);
    }

    /**
     * Para uso em anotações @PreAuthorize
     */
    public boolean canEditCompania(UUID userId, UUID companiaId) {
        return podeEditarCompania(userId, companiaId);
    }
}