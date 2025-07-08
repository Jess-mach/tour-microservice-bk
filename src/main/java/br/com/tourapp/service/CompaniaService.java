package br.com.tourapp.service;

import br.com.tourapp.dto.enums.RoleCompania;
import br.com.tourapp.dto.enums.StatusCompania;
import br.com.tourapp.dto.request.CompaniaRequest;
import br.com.tourapp.dto.request.UserCompaniaRequest;
import br.com.tourapp.dto.request.AlterarRoleRequest;
import br.com.tourapp.dto.response.CompaniaResponse;
import br.com.tourapp.dto.response.UserCompaniaResponse;
import br.com.tourapp.dto.response.CompaniaUsuarioResponse;
import br.com.tourapp.dto.response.CompaniaEstatisticasResponse;
import br.com.tourapp.entity.CompaniaEntity;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.entity.UserCompaniaEntity;

import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.CompaniaRepository;
import br.com.tourapp.repository.UserCompaniaRepository;
import br.com.tourapp.repository.UserRepository;
import br.com.tourapp.repository.ExcursaoRepository;
import br.com.tourapp.repository.InscricaoRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class CompaniaService {

    private static final Logger logger = LoggerFactory.getLogger(CompaniaService.class);

    private final CompaniaRepository companiaRepository;
    private final UserCompaniaRepository userCompaniaRepository;
    private final UserRepository userRepository;
    private final ExcursaoRepository excursaoRepository;
    private final InscricaoRepository inscricaoRepository;
    private final CompaniaSecurityService securityService;
    private final ModelMapper modelMapper;

    public CompaniaService(CompaniaRepository companiaRepository,
                           UserCompaniaRepository userCompaniaRepository,
                           UserRepository userRepository,
                           ExcursaoRepository excursaoRepository,
                           InscricaoRepository inscricaoRepository,
                           CompaniaSecurityService securityService,
                           ModelMapper modelMapper) {
        this.companiaRepository = companiaRepository;
        this.userCompaniaRepository = userCompaniaRepository;
        this.userRepository = userRepository;
        this.excursaoRepository = excursaoRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.securityService = securityService;
        this.modelMapper = modelMapper;
    }

    // ============================================
    // CRUD BÁSICO DE COMPANIAS
    // ============================================

    /**
     * Criar nova compania e adicionar o usuário como admin
     */
    public CompaniaResponse criarCompania(CompaniaRequest request, UUID userId) {
        logger.info("Criando nova compania para usuário: {}", userId);

        // Validar dados
        validarDadosCompania(request);

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Criar compania
        CompaniaEntity compania = new CompaniaEntity();
        compania.setNomeEmpresa(request.getNomeEmpresa());
        compania.setCnpj(request.getCnpj());
        compania.setDescricao(request.getDescricao());
        compania.setSite(request.getSite());
        compania.setPixKey(request.getPixKey());
        compania.setCep(request.getCep());
        compania.setEndereco(request.getEndereco());
        compania.setCidade(request.getCidade());
        compania.setEstado(request.getEstado());
        compania.setStatus(StatusCompania.ATIVA);

        compania = companiaRepository.save(compania);

        // Adicionar usuário como admin da compania
        UserCompaniaEntity userCompania = new UserCompaniaEntity(user, compania, RoleCompania.ADMIN);
        userCompaniaRepository.save(userCompania);

        logger.info("Compania {} criada com sucesso para usuário {}", compania.getId(), userId);

        return converterParaResponse(compania, userId);
    }

    /**
     * Listar companias do usuário
     */
    @Transactional(readOnly = true)
    public List<CompaniaResponse> listarMinhasCompanias(UUID userId) {
        logger.debug("Listando companias do usuário: {}", userId);

        List<CompaniaEntity> companias = companiaRepository.findByUserId(userId);

        return companias.stream()
                .map(compania -> converterParaResponse(compania, userId))
                .collect(Collectors.toList());
    }

    /**
     * Obter compania por ID
     */
    @Transactional(readOnly = true)
    public CompaniaResponse obterCompania(UUID companiaId, UUID userId) {
        logger.debug("Obtendo compania {} para usuário {}", companiaId, userId);

        securityService.validarAcessoCompania(userId, companiaId);

        CompaniaEntity compania = companiaRepository.findById(companiaId)
                .orElseThrow(() -> new NotFoundException("Compania não encontrada"));

        return converterParaResponse(compania, userId);
    }

    /**
     * Atualizar compania
     */
    public CompaniaResponse atualizarCompania(UUID companiaId, CompaniaRequest request, UUID userId) {
        logger.info("Atualizando compania {} pelo usuário {}", companiaId, userId);

        securityService.validarPermissaoEditarCompania(userId, companiaId);

        CompaniaEntity compania = companiaRepository.findById(companiaId)
                .orElseThrow(() -> new NotFoundException("Compania não encontrada"));

        // Validar dados
        if (!request.getCnpj().equals(compania.getCnpj()) &&
                companiaRepository.existsByCnpj(request.getCnpj())) {
            throw new BusinessException("CNPJ já está sendo usado por outra empresa");
        }

        // Atualizar dados
        compania.setNomeEmpresa(request.getNomeEmpresa());
        compania.setCnpj(request.getCnpj());
        compania.setDescricao(request.getDescricao());
        compania.setSite(request.getSite());
        compania.setPixKey(request.getPixKey());
        compania.setCep(request.getCep());
        compania.setEndereco(request.getEndereco());
        compania.setCidade(request.getCidade());
        compania.setEstado(request.getEstado());

        compania = companiaRepository.save(compania);

        logger.info("Compania {} atualizada com sucesso", companiaId);

        return converterParaResponse(compania, userId);
    }

    // ============================================
    // GESTÃO DE USUÁRIOS NA COMPANIA
    // ============================================

    /**
     * Listar usuários da compania
     */
    @Transactional(readOnly = true)
    public Page<CompaniaUsuarioResponse> listarUsuariosCompania(UUID companiaId, UUID userId, Pageable pageable) {
        logger.debug("Listando usuários da compania {} para usuário {}", companiaId, userId);

        securityService.validarAcessoCompania(userId, companiaId);

        Page<UserEntity> usuarios = userRepository.findByCompaniaId(companiaId, pageable);

        return usuarios.map(user -> converterParaCompaniaUsuarioResponse(user, companiaId));
    }

    /**
     * Adicionar usuário à compania
     */
    public UserCompaniaResponse adicionarUsuario(UUID companiaId, UserCompaniaRequest request, UUID adminId) {
        logger.info("Adicionando usuário {} à compania {} pelo admin {}",
                request.getEmail(), companiaId, adminId);

        securityService.validarPermissaoGerenciarUsuarios(adminId, companiaId);

        CompaniaEntity compania = companiaRepository.findById(companiaId)
                .orElseThrow(() -> new NotFoundException("Compania não encontrada"));

        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado com email: " + request.getEmail()));

        // Verificar se já faz parte da compania
        if (userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(user.getId(), companiaId)) {
            throw new BusinessException("Usuário já faz parte desta compania");
        }

        // Criar relacionamento
        UserCompaniaEntity userCompania = new UserCompaniaEntity(user, compania, request.getRoleCompania());

        // Configurar permissões customizadas se fornecidas
        if (request.getPodeCreiarExcursoes() != null) {
            userCompania.setPodeCreiarExcursoes(request.getPodeCreiarExcursoes());
        }
        if (request.getPodeGerenciarUsuarios() != null) {
            userCompania.setPodeGerenciarUsuarios(request.getPodeGerenciarUsuarios());
        }
        if (request.getPodeVerFinanceiro() != null) {
            userCompania.setPodeVerFinanceiro(request.getPodeVerFinanceiro());
        }
        if (request.getPodeEditarCompania() != null) {
            userCompania.setPodeEditarCompania(request.getPodeEditarCompania());
        }
        if (request.getPodeEnviarNotificacoes() != null) {
            userCompania.setPodeEnviarNotificacoes(request.getPodeEnviarNotificacoes());
        }

        userCompania.setObservacoes(request.getObservacoes());

        userCompania = userCompaniaRepository.save(userCompania);

        logger.info("Usuário {} adicionado à compania {} com role {}",
                user.getEmail(), companiaId, request.getRoleCompania());

        return converterParaUserCompaniaResponse(userCompania);
    }

    /**
     * Alterar role de usuário na compania
     */
    public UserCompaniaResponse alterarRoleUsuario(UUID companiaId, AlterarRoleRequest request, UUID adminId) {
        logger.info("Alterando role do usuário {} na compania {} para {} pelo admin {}",
                request.getUserId(), companiaId, request.getNovoRole(), adminId);

        securityService.validarPermissaoGerenciarUsuarios(adminId, companiaId);

        UserCompaniaEntity userCompania = userCompaniaRepository.findByUserAndCompania(request.getUserId(), companiaId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado na compania"));

        // Validar se pode alterar o role
        if (!securityService.podeAlterarRole(adminId, companiaId, request.getUserId(), request.getNovoRole())) {
            throw new BusinessException("Não é possível alterar o role deste usuário");
        }

        // Alterar role e reconfigurar permissões
        userCompania.promoverPara(request.getNovoRole());

        // Aplicar permissões customizadas se fornecidas
        if (request.getPodeCreiarExcursoes() != null) {
            userCompania.setPodeCreiarExcursoes(request.getPodeCreiarExcursoes());
        }
        if (request.getPodeGerenciarUsuarios() != null) {
            userCompania.setPodeGerenciarUsuarios(request.getPodeGerenciarUsuarios());
        }
        if (request.getPodeVerFinanceiro() != null) {
            userCompania.setPodeVerFinanceiro(request.getPodeVerFinanceiro());
        }
        if (request.getPodeEditarCompania() != null) {
            userCompania.setPodeEditarCompania(request.getPodeEditarCompania());
        }
        if (request.getPodeEnviarNotificacoes() != null) {
            userCompania.setPodeEnviarNotificacoes(request.getPodeEnviarNotificacoes());
        }

        userCompania.setObservacoes(request.getJustificativa());

        userCompania = userCompaniaRepository.save(userCompania);

        logger.info("Role do usuário {} alterado para {} na compania {}",
                request.getUserId(), request.getNovoRole(), companiaId);

        return converterParaUserCompaniaResponse(userCompania);
    }

    /**
     * Remover usuário da compania
     */
    public void removerUsuario(UUID companiaId, UUID userId, UUID adminId) {
        logger.info("Removendo usuário {} da compania {} pelo admin {}", userId, companiaId, adminId);

        securityService.validarPermissaoGerenciarUsuarios(adminId, companiaId);

        UserCompaniaEntity userCompania = userCompaniaRepository.findByUserAndCompania(userId, companiaId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado na compania"));

        // Validar se pode remover
        if (!securityService.podeRemoverUsuario(adminId, companiaId, userId)) {
            throw new BusinessException("Não é possível remover este usuário da compania");
        }

        // Desativar ao invés de excluir (para manter histórico)
        userCompania.desativar();
        userCompaniaRepository.save(userCompania);

        logger.info("Usuário {} removido da compania {}", userId, companiaId);
    }

    // ============================================
    // ESTATÍSTICAS E RELATÓRIOS
    // ============================================

    /**
     * Obter estatísticas da compania
     */
    @Transactional(readOnly = true)
    public CompaniaEstatisticasResponse obterEstatisticas(UUID companiaId, UUID userId,
                                                          LocalDate dataInicio, LocalDate dataFim) {
        logger.debug("Obtendo estatísticas da compania {} para usuário {}", companiaId, userId);

        securityService.validarPermissaoVerFinanceiro(userId, companiaId);

        // Se não informar datas, usar o mês atual
        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        CompaniaEstatisticasResponse stats = new CompaniaEstatisticasResponse();
        stats.setPeriodoInicio(dataInicio);
        stats.setPeriodoFim(dataFim);

        // Estatísticas de usuários
        stats.setTotalUsuarios(userCompaniaRepository.countActiveUsersByCompania(companiaId));
        stats.setTotalAdmins(userCompaniaRepository.countByCompaniaAndRole(companiaId, RoleCompania.ADMIN));
        stats.setTotalOrganizadores(userCompaniaRepository.countByCompaniaAndRole(companiaId, RoleCompania.ORGANIZADOR));
        stats.setTotalColaboradores(userCompaniaRepository.countByCompaniaAndRole(companiaId, RoleCompania.COLABORADOR));

        // Estatísticas de excursões
        stats.setTotalExcursoes(companiaRepository.countExcursoesAtivas(companiaId));
        // ... outras estatísticas seriam implementadas aqui

        return stats;
    }

    // ============================================
    // MÉTODOS AUXILIARES
    // ============================================

    private void validarDadosCompania(CompaniaRequest request) {
        if (request.getCnpj() != null && companiaRepository.existsByCnpj(request.getCnpj())) {
            throw new BusinessException("CNPJ já está sendo usado por outra empresa");
        }

        if (companiaRepository.existsByNomeEmpresa(request.getNomeEmpresa())) {
            throw new BusinessException("Já existe uma empresa com este nome");
        }
    }

    private CompaniaResponse converterParaResponse(CompaniaEntity compania, UUID userId) {
        CompaniaResponse response = modelMapper.map(compania, CompaniaResponse.class);

        // Adicionar dados estatísticos TODO resolver com a Claude
//        response.setTotalUsuarios(userCompaniaRepository.countActiveUsersByCompania(compania.getId()));
//        response.setTotalExcursoes(companiaRepository.countExcursoesAtivas(compania.getId()));
//        response.setPerfilCompleto(compania.isPerfilCompleto());

        // Adicionar dados do usuário na compania
        userCompaniaRepository.findByUserAndCompania(userId, compania.getId())
                .ifPresent(uc -> {
//                    response.setRoleUsuario(uc.getRoleCompania().name()); TODO resolver com a Claude
                    response.setPodeCreiarExcursoes(uc.getPodeCreiarExcursoes());
                    response.setPodeGerenciarUsuarios(uc.getPodeGerenciarUsuarios());
                    response.setPodeVerFinanceiro(uc.getPodeVerFinanceiro());
                    response.setPodeEditarCompania(uc.getPodeEditarCompania());
                    response.setPodeEnviarNotificacoes(uc.getPodeEnviarNotificacoes());
//                    response.setDataIngresso(uc.getDataIngresso()); TODO resolver com a Claude
                });

        return response;
    }

    private UserCompaniaResponse converterParaUserCompaniaResponse(UserCompaniaEntity userCompania) {
        UserCompaniaResponse response = modelMapper.map(userCompania, UserCompaniaResponse.class);

        response.setUserId(userCompania.getUser().getId());
        response.setCompaniaId(userCompania.getCompania().getId());
        response.setNomeUsuario(userCompania.getUser().getFullName());
        response.setEmailUsuario(userCompania.getUser().getEmail());
        response.setTelefoneUsuario(userCompania.getUser().getPhone());
        response.setNomeEmpresa(userCompania.getCompania().getNomeEmpresa());
//        response.setStatusCompania(userCompania.getCompania().getStatus().name()); TODO resolver posteriormnte
        response.setIsConvitePendente(userCompania.isConvidado());

        return response;
    }

    private CompaniaUsuarioResponse converterParaCompaniaUsuarioResponse(UserEntity user, UUID companiaId) {
        CompaniaUsuarioResponse response = new CompaniaUsuarioResponse();

        response.setUserId(user.getId());
        response.setNome(user.getFullName());
        response.setEmail(user.getEmail());
        response.setTelefone(user.getPhone());
        response.setProfilePicture(user.getProfilePicture());
        response.setAtivo(user.getActive());
        response.setUltimoLogin(user.getLastLogin());

        // Dados na compania
        userCompaniaRepository.findByUserAndCompania(user.getId(), companiaId)
                .ifPresent(uc -> {
                    response.setRoleCompania(uc.getRoleCompania());
                    response.setDataIngresso(uc.getDataIngresso());
                    response.setAtivoNaCompania(uc.getAtivo());
                    response.setPodeCreiarExcursoes(uc.getPodeCreiarExcursoes());
                    response.setPodeGerenciarUsuarios(uc.getPodeGerenciarUsuarios());
                    response.setPodeVerFinanceiro(uc.getPodeVerFinanceiro());
                    response.setPodeEditarCompania(uc.getPodeEditarCompania());
                    response.setPodeEnviarNotificacoes(uc.getPodeEnviarNotificacoes());
                    response.setIsConvitePendente(uc.isConvidado());
                    response.setConvidadoPor(uc.getConvidadoPor());
                    response.setDataAceiteConvite(uc.getDataAceiteConvite());
                });

        return response;
    }
}