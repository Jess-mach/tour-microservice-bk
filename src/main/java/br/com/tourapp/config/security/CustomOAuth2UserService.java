package br.com.tourapp.config.security;

import com.tourapp.entity.RoleEntity;
import com.tourapp.entity.UserEntity;
import com.tourapp.repository.RoleRepository;
import com.tourapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public CustomOAuth2UserService(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // Extraindo informações do usuário Google
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = (String) attributes.get("sub");
        String pictureUrl = (String) attributes.get("picture");

        // Verificando se o usuário já existe ou criando um novo
        Optional<UserEntity> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            updateExistingUser(user, googleId, name, pictureUrl);
        } else {
            createNewUser(email, googleId, name, pictureUrl);
        }

        return oAuth2User;
    }

    private void updateExistingUser(UserEntity user, String googleId, String name, String pictureUrl) {
        user.setGoogleId(googleId);
        user.setFullName(name);
        user.setProfilePicture(pictureUrl);
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        logger.info("Usuário existente atualizado: {}", user.getEmail());
    }

    private void createNewUser(String email, String googleId, String name, String pictureUrl) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFullName(name);
        user.setGoogleId(googleId);
        user.setProfilePicture(pictureUrl);
        user.setActive(true);

        // Atribuindo papel USER por padrão
        RoleEntity userRole = roleRepository.findByName(RoleEntity.ROLE_USER)
                .orElseGet(() -> {
                    RoleEntity newRole = new RoleEntity();
                    newRole.setName(RoleEntity.ROLE_USER);
                    return roleRepository.save(newRole);
                });

        user.getRoles().add(userRole);
        userRepository.save(user);
        logger.info("Novo usuário criado: {}", email);
    }
}