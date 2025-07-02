package br.com.tourapp.service;

import br.com.tourapp.config.security.GoogleTokenVerifier;
import br.com.tourapp.dto.GoogleUserInfo;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.entity.RoleEntity;
import br.com.tourapp.repository.RoleRepository;
import br.com.tourapp.repository.UserRepository;
import br.com.tourapp.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class UserService implements UserUseCase {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final GoogleTokenVerifier googleTokenVerifier;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            JwtUtils jwtUtils,
            GoogleTokenVerifier googleTokenVerifier
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.jwtUtils = jwtUtils;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    /**
     * Método para processar Google ID Token e retornar usuário e UserDetails
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

        // Criar UserDetails para o usuário
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password("") // Senha vazia para OAuth2
                .authorities(authorities)
                .build();

        logger.info("UserDetails criado com sucesso para: {}", user.getEmail());
        return new Pair<>(user, userDetails);
    }

    /**
     * Gera token JWT INTERNO da aplicação (não confundir com Google ID Token)
     */
    public String generateAccessToken(UserDetails userDetails) {
        logger.debug("Gerando token de acesso interno para: {}", userDetails.getUsername());
        return jwtUtils.generateJwtToken(userDetails);
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
                user.getId() != null ? user.getId() : 0,
                user.getEmail(),
                user.getFullName(),
                roles,
                user.getProfilePicture(),
                user.getSubscriptionPlan(),
                hasActiveSubscription
        );
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
                    RoleEntity userRole = roleRepository.findByName(RoleEntity.ROLE_USER)
                            .orElseGet(() -> {
                                RoleEntity newRole = new RoleEntity();
                                newRole.setName(RoleEntity.ROLE_USER);
                                return roleRepository.save(newRole);
                            });

                    newUser.getRoles().add(userRole);
                    return userRepository.save(newUser);
                });

        // Atualiza dados do usuário existente se necessário
        updateUserInfo(user, googleUserInfo);

        return user;
    }

    @Override
    public UserDetails loadUserDetailsByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado com o email: " + email));

        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
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
                user.getId() != null ? user.getId() : 0,
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