package br.com.tourapp.service;

import br.com.tourapp.config.security.GoogleTokenVerifier;
import br.com.tourapp.controller.OrganizadorController;
import br.com.tourapp.dto.GoogleUserInfo;
import br.com.tourapp.dto.request.UpdateUserRequest;
import br.com.tourapp.dto.response.DashboardResponse;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.entity.CompaniaEntity;
import br.com.tourapp.entity.RoleEntity;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.enums.StatusExcursao;
import br.com.tourapp.exception.AccessDeniedException;
import br.com.tourapp.exception.BusinessException;
import br.com.tourapp.exception.NotFoundException;
import br.com.tourapp.repository.*;
import br.com.tourapp.dto.SecurityUser;
import br.com.tourapp.util.JwtUtils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final CompaniaRepository companiaRepository;
    private final UserCompaniaRepository userCompaniaRepository;
    private final ExcursaoRepository excursaoRepository;
    private final InscricaoRepository inscricaoRepository;

    /**
     * Método para processar Google ID Token e retornar usuário e SecurityUser
     * CORRIGIDO: Remove dependência de RefreshTokenService para evitar circulação
     */
    @Transactional
    public Pair<UserEntity, UserDetails> processGoogleToken(String googleIdToken) {
        logger.info("Processando Google ID Token");

        // Validar o Google ID Token usando GoogleTokenVerifier
        GoogleUserInfo googleUserInfo = googleTokenVerifier.verify(googleIdToken);
        if (googleUserInfo == null) {
            logger.error("Google ID Token inválido ou expirado");
            throw new RuntimeException("Token inválido ou expirado");
        }

        logger.info("Google ID Token validado com sucesso para usuário: {}", googleUserInfo.getEmail());

        // Criar ou atualizar usuário
        UserEntity user = findOrCreateUser(googleUserInfo);

        // Criar SecurityUser para o usuário
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("") // Senha vazia para OAuth2
                .authorities(authorities)
                .build();

        logger.info("SecurityUser criado com sucesso para: {}", user.getEmail());
        return new Pair<>(user, userDetails);
    }

    /**
     * Gera token JWT INTERNO da aplicação (não confundir com Google ID Token)
     */
    public String generateAccessToken(UserDetails securityUser) {
        logger.debug("Gerando token de acesso interno para: {}", securityUser.getUsername());
        return jwtUtils.generateJwtToken(securityUser);
    }

    /**
     * Constrói a resposta JWT com informações do usuário
     */
    public JwtResponse buildJwtResponse(UserEntity user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        boolean hasActiveSubscription = user.getSubscriptionExpiry() != null &&
                user.getSubscriptionExpiry().isAfter(LocalDateTime.now());

        return new JwtResponse(
                accessToken,
                refreshToken,
                "Bearer",
                user.getId() != null ? user.getId().toString() : "",
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getProfilePicture(),
                user.getSubscriptionPlan(),
                hasActiveSubscription,
                true
        );
    }

    @Override
    public UserInfoResponse obterPerfil(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        boolean hasActiveSubscription = user.getSubscriptionExpiry() != null &&
                user.getSubscriptionExpiry().isAfter(LocalDateTime.now());

        return UserInfoResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .profilePicture(user.getProfilePicture())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .roles(roles)
                .subscriptionPlan(user.getSubscriptionPlan())
                .subscriptionExpiry(user.getSubscriptionExpiry())
                .hasActiveSubscription(hasActiveSubscription)
                .build();
    }

    @Override
    public UserInfoResponse atualizarPerfil(UUID id, UpdateUserRequest request) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));

        // Atualizar campos permitidos
        if (request.nome() != null) {
            user.setFullName(request.nome());
        }
        if (request.telefone() != null) {
            user.setPhone(request.telefone());
        }
        if (request.cep() != null) {
            user.setCep(request.cep());
        }
        if (request.endereco() != null) {
            user.setEndereco(request.endereco());
        }
        if (request.cidade() != null) {
            user.setCidade(request.cidade());
        }
        if (request.estado() != null) {
            user.setEstado(request.estado());
        }

        user = userRepository.save(user);
        return obterPerfil(user.getId());
    }

    @Override
    public DashboardResponse obterDashboard(UUID userId, UUID companiaId, LocalDate dataInicio, LocalDate dataFim) {
        // Validar se usuário tem acesso à compania
        if (!userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(userId, companiaId)) {
            throw new AccessDeniedException("Usuário não tem acesso a esta compania");
        }

        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setPeriodoInicio(dataInicio);
        dashboard.setPeriodoFim(dataFim);

        // Buscar estatísticas da compania
        Long totalExcursoes = excursaoRepository.countByCompaniaId(companiaId);
        Long excursoesAtivas = excursaoRepository.countByCompaniaIdAndStatus(companiaId, StatusExcursao.ATIVA);

        dashboard.setTotalExcursoes(totalExcursoes);
        dashboard.setExcursoesAtivas(excursoesAtivas);

        // Buscar receita do período
        BigDecimal receitaTotal = inscricaoRepository.findTotalReceitaByCompaniaAndPeriodo(
                companiaId, inicio, fim);
        dashboard.setReceitaTotal(receitaTotal != null ? receitaTotal : BigDecimal.ZERO);

        // Outras estatísticas
        Long totalInscricoes = inscricaoRepository.countInscricoesByCompaniaAndPeriodo(
                companiaId, inicio, fim);
        dashboard.setTotalInscricoes(totalInscricoes != null ? totalInscricoes : 0L);

        return dashboard;
    }

    @Override
    public DashboardResponse obterDashboardConsolidado(UUID userId, LocalDate dataInicio, LocalDate dataFim) {
        // Buscar todas as companias do usuário
        List<CompaniaEntity> companias = companiaRepository.findByUserId(userId);

        if (companias.isEmpty()) {
            throw new BusinessException("Usuário não possui nenhuma compania");
        }

        if (dataInicio == null) {
            dataInicio = LocalDate.now().withDayOfMonth(1);
        }
        if (dataFim == null) {
            dataFim = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        }

        DashboardResponse dashboardConsolidado = new DashboardResponse();
        dashboardConsolidado.setPeriodoInicio(dataInicio);
        dashboardConsolidado.setPeriodoFim(dataFim);

        // Consolidar dados de todas as companias
        Long totalExcursoes = 0L;
        Long excursoesAtivas = 0L;
        BigDecimal receitaTotal = BigDecimal.ZERO;

        for (CompaniaEntity compania : companias) {
            DashboardResponse dashCompania = obterDashboard(userId, compania.getId(), dataInicio, dataFim);
            totalExcursoes += dashCompania.getTotalExcursoes();
            excursoesAtivas += dashCompania.getExcursoesAtivas();
            receitaTotal = receitaTotal.add(dashCompania.getReceitaTotal());
        }

        dashboardConsolidado.setTotalExcursoes(totalExcursoes);
        dashboardConsolidado.setExcursoesAtivas(excursoesAtivas);
        dashboardConsolidado.setReceitaTotal(receitaTotal);

        return dashboardConsolidado;
    }

    @Override
    public UserEntity obterPorId(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado"));
    }

    @Override
    public void atualizarPushToken(UUID userId, String pushToken) {
        UserEntity user = obterPorId(userId);
        user.setPushToken(pushToken);
        userRepository.save(user);
    }

    @Override
    public void atualizarConfiguracoes(UUID userId, Boolean emailNotifications, Boolean smsNotifications) {
        UserEntity user = obterPorId(userId);

        if (emailNotifications != null) {
            user.setEmailNotifications(emailNotifications);
        }
        if (smsNotifications != null) {
            user.setSmsNotifications(smsNotifications);
        }

        userRepository.save(user);
    }

    @Override
    public OrganizadorController.ReceitaEstatisticasResponse obterEstatisticasReceita(
            UUID userId, UUID companiaId, LocalDate dataInicio, LocalDate dataFim) {

        if (companiaId != null) {
            // Validar acesso à compania
            if (!userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(userId, companiaId)) {
                throw new AccessDeniedException("Usuário não tem acesso a esta compania");
            }
        }

        OrganizadorController.ReceitaEstatisticasResponse response =
                new OrganizadorController.ReceitaEstatisticasResponse();

        // Implementar lógica de estatísticas de receita
        // Por enquanto, retornando resposta vazia
        return response;
    }

    @Override
    public OrganizadorController.ExcursoesEstatisticasResponse obterEstatisticasExcursoes(
            UUID userId, UUID companiaId, LocalDate dataInicio, LocalDate dataFim) {

        if (companiaId != null) {
            // Validar acesso à compania
            if (!userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(userId, companiaId)) {
                throw new AccessDeniedException("Usuário não tem acesso a esta compania");
            }
        }

        OrganizadorController.ExcursoesEstatisticasResponse response =
                new OrganizadorController.ExcursoesEstatisticasResponse();

        // Implementar lógica de estatísticas de excursões
        // Por enquanto, retornando resposta vazia
        return response;
    }

    @Override
    public OrganizadorController.RelatorioVendasResponse gerarRelatorioVendas(
            UUID userId, UUID companiaId, LocalDate dataInicio, LocalDate dataFim, Boolean incluirDetalhado) {

        if (companiaId != null) {
            // Validar acesso à compania
            if (!userCompaniaRepository.existsByUserIdAndCompaniaIdAndAtivoTrue(userId, companiaId)) {
                throw new AccessDeniedException("Usuário não tem acesso a esta compania");
            }
        }

        OrganizadorController.RelatorioVendasResponse response =
                new OrganizadorController.RelatorioVendasResponse();

        // Implementar lógica do relatório de vendas
        // Por enquanto, retornando resposta vazia
        return response;
    }

    /**
     * Encontra ou cria usuário baseado nas informações do Google
     */
    @Transactional
    private UserEntity findOrCreateUser(GoogleUserInfo googleUserInfo) {
        UserEntity user = userRepository.findByEmail(googleUserInfo.getEmail())
                .orElseGet(() -> {
                    logger.info("Criando novo usuário para: {}", googleUserInfo.getEmail());

                    // Cria novo usuário
                    UserEntity newUser = new UserEntity();
                    newUser.setEmail(googleUserInfo.getEmail());
                    newUser.setFullName(googleUserInfo.getName());
                    newUser.setGoogleId(googleUserInfo.getSub());
                    newUser.setProfilePicture(googleUserInfo.getPicture());
                    newUser.setActive(true);

                    // Atribui papel USER por padrão
                    RoleEntity role = roleRepository.findByName(RoleEntity.ROLE_USER)
                            .orElseGet(() -> {
                                RoleEntity newRole = new RoleEntity();
                                newRole.setName(RoleEntity.ROLE_USER);
                                return roleRepository.save(newRole);
                            });

                    newUser.getRoles().add(role);

                    // Atribuindo papel USER por padrão
                    role = roleRepository.findByName(RoleEntity.ROLE_ORGANIZADOR)
                            .orElseGet(() -> {
                                RoleEntity newRole = new RoleEntity();
                                newRole.setName(RoleEntity.ROLE_ORGANIZADOR);
                                return roleRepository.save(newRole);
                            });

                    newUser.getRoles().add(role);

                    // Atribuindo papel USER por padrão
                    role = roleRepository.findByName(RoleEntity.ROLE_CLIENTE)
                            .orElseGet(() -> {
                                RoleEntity newRole = new RoleEntity();
                                newRole.setName(RoleEntity.ROLE_CLIENTE);
                                return roleRepository.save(newRole);
                            });

                    newUser.getRoles().add(role);

                    return userRepository.save(newUser);
                });

        // Atualiza dados do usuário existente se necessário
        updateUserInfo(user, googleUserInfo);

        return user;
    }

    @Override
    public SecurityUser loadSecurityUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return (SecurityUser) org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("")
                .authorities(authorities)
                .build();
    }

    private void updateUserInfo(UserEntity user, GoogleUserInfo googleUserInfo) {
        boolean updated = false;

        if (googleUserInfo.getSub() != null && !googleUserInfo.getSub().equals(user.getGoogleId())) {
            user.setGoogleId(googleUserInfo.getSub());
            updated = true;
        }

        if (googleUserInfo.getName() != null && !googleUserInfo.getName().equals(user.getFullName())) {
            user.setFullName(googleUserInfo.getName());
            updated = true;
        }

        if (googleUserInfo.getPicture() != null &&
                !googleUserInfo.getPicture().equals(user.getProfilePicture())) {
            user.setProfilePicture(googleUserInfo.getPicture());
            updated = true;
        }

        // Atualiza o último login
        user.setLastLogin(LocalDateTime.now());
        updated = true;

        if (updated) {
            userRepository.save(user);
            logger.info("Informações do usuário atualizadas: {}", user.getEmail());
        }
    }

    public UserInfoResponse getUserInfo(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        List<String> roles = user.getRoles().stream()
                .map(RoleEntity::getName)
                .collect(Collectors.toList());

        boolean hasActiveSubscription = user.getSubscriptionExpiry() != null &&
                user.getSubscriptionExpiry().isAfter(LocalDateTime.now());

        return new UserInfoResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getProfilePicture(),
                user.getCreatedAt(),
                user.getLastLogin(),
                roles,
                user.getSubscriptionPlan(),
                user.getSubscriptionExpiry(),
                hasActiveSubscription
        );
    }

    public Map<String, Object> checkSubscription(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        boolean hasActiveSubscription = user.getSubscriptionExpiry() != null &&
                user.getSubscriptionExpiry().isAfter(LocalDateTime.now());

        return Map.of(
                "hasActiveSubscription", hasActiveSubscription,
                "subscriptionPlan", user.getSubscriptionPlan() != null ? user.getSubscriptionPlan() : "none",
                "subscriptionExpiry", user.getSubscriptionExpiry() != null ? user.getSubscriptionExpiry().toString() : ""
        );
    }

    @Transactional
    public void updateSubscription(String email, String plan, int months) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        user.setSubscriptionPlan(plan);
        user.setSubscriptionExpiry(LocalDateTime.now().plusMonths(months));

        // Adicionar ROLE_PREMIUM se não existir
        RoleEntity premiumRole = roleRepository.findByName(RoleEntity.ROLE_PREMIUM)
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName(RoleEntity.ROLE_PREMIUM);
                    return roleRepository.save(newRole);
                });

        boolean hasPremiumRole = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(RoleEntity.ROLE_PREMIUM));

        if (!hasPremiumRole) {
            user.getRoles().add(premiumRole);
        }

        userRepository.save(user);
    }

    // Classe auxiliar para simular o Pair
    public static class Pair<A, B> {
        private final A first;
        private final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }

        public A getFirst() {
            return first;
        }

        public B getSecond() {
            return second;
        }
    }
}