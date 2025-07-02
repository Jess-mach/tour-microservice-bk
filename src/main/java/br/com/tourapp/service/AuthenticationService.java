package br.com.tourapp.service;

import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import br.com.tourapp.entity.RefreshTokenEntity;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.entity.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService implements AuthenticationUseCase{
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    public AuthenticationService(
            UserService userService,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Método central para autenticação via Google
     */
    @Transactional
    public JwtResponse authenticateWithGoogle(String googleToken) {
        // Obter informações do usuário
        UserService.Pair<UserEntity, UserDetails> userInfo = userService.processGoogleToken(googleToken);

        // Gerar tokens JWT
        String accessToken = userService.generateAccessToken(userInfo.getSecond());

        // Gerar refresh token
        RefreshTokenEntity refreshToken = refreshTokenService.createRefreshToken(
                userInfo.getFirst().getEmail(),
                userInfo.getSecond()
        );

        // Construir e retornar a resposta
        return userService.buildJwtResponse(
                userInfo.getFirst(),
                accessToken,
                refreshToken.getToken()
        );
    }

    /**
     * Método para renovar o token de acesso
     */
    public TokenRefreshResponse refreshToken(String refreshToken) {
        // Verificar e obter o refresh token
        RefreshTokenEntity tokenEntity = refreshTokenService.findAndValidateToken(refreshToken);

        // Obter detalhes do usuário
        SecurityUser securityUser = userService.loadSecurityUserByEmail(tokenEntity.getUserEmail());

        // Gerar novo token de acesso
        String newAccessToken = userService.generateAccessToken(securityUser);

        // Retornar resposta
        return new TokenRefreshResponse(
                newAccessToken,
                tokenEntity.getToken()
        );
    }
}