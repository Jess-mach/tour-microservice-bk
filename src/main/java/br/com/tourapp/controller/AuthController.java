package br.com.tourapp.controller;

import br.com.tourapp.dto.request.TokenRefreshRequest;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.security.SecurityUser;
import br.com.tourapp.service.AuthenticationUseCase;
import br.com.tourapp.service.RefreshTokenUseCase;
import br.com.tourapp.service.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticação", description = "Endpoints para autenticação e gestão de usuários")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserUseCase userService;
    private final RefreshTokenUseCase refreshTokenService;
    private final AuthenticationUseCase authenticationService;

    public AuthController(
            UserUseCase userService,
            RefreshTokenUseCase refreshTokenService,
            AuthenticationUseCase authenticationService
    ) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/google")
    @Operation(summary = "Autenticar com Google", description = "Autentica usuário usando Google ID Token")
    public ResponseEntity<JwtResponse> authenticateWithGoogle(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            if (idToken == null || idToken.trim().isEmpty()) {
                throw new RuntimeException("ID Token é obrigatório");
            }

            logger.info("Recebendo solicitação de autenticação Google");
            JwtResponse jwtResponse = authenticationService.authenticateWithGoogle(idToken);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            logger.error("Erro na autenticação Google: {}", e.getMessage(), e);
            throw new RuntimeException("Falha na autenticação: " + e.getMessage());
        }
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate Google token", description = "Validates a Google ID token and returns JWT")
    public ResponseEntity<JwtResponse> validateToken(@RequestParam("token") String token) {
        // Este endpoint agora valida apenas tokens JWT internos, não Google ID Tokens
        try {
            // Aqui você pode implementar validação do seu próprio JWT se necessário
            // Para Google ID Tokens, use o endpoint /api/auth/google
            throw new RuntimeException("Use o endpoint /api/auth/google para autenticação com Google ID Token");
        } catch (Exception e) {
            logger.error("Erro na validação do token: {}", e.getMessage());
            throw new RuntimeException("Token inválido: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Generate a new access token using refresh token")
    public ResponseEntity<TokenRefreshResponse> refreshToken(@RequestBody TokenRefreshRequest request) {
        TokenRefreshResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida o refresh token do usuário")
    public ResponseEntity<Map<String, String>> logoutUser(@AuthenticationPrincipal SecurityUser securityUser) {
        refreshTokenService.deleteByUserEmail(securityUser.getUsername());
        return ResponseEntity.ok(Map.of("message", "Log out bem-sucedido!"));
    }

    @GetMapping("/me")
    @Operation(summary = "Obter informações do usuário", description = "Retorna as informações do usuário autenticado")
    public ResponseEntity<UserInfoResponse> getUserInfo(@AuthenticationPrincipal SecurityUser securityUser) {
        UserInfoResponse userInfoResponse = userService.getUserInfo(securityUser.getUsername());
        return ResponseEntity.ok(userInfoResponse);
    }

    @GetMapping("/check-subscription")
    @Operation(summary = "Verificar assinatura", description = "Verifica se o usuário possui uma assinatura ativa")
    public ResponseEntity<Map<String, Object>> checkSubscription(@AuthenticationPrincipal SecurityUser securityUser) {
        Map<String, Object> subscriptionInfo = userService.checkSubscription(securityUser.getUsername());
        return ResponseEntity.ok(subscriptionInfo);
    }

    @PostMapping("/subscription/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update subscription", description = "Update user subscription (admin only)")
    public ResponseEntity<Map<String, String>> updateSubscription(
            @RequestParam("email") String email,
            @RequestParam("plan") String plan,
            @RequestParam("months") int months
    ) {
        userService.updateSubscription(email, plan, months);
        return ResponseEntity.ok(Map.of("message", "Assinatura atualizada com sucesso"));
    }
}