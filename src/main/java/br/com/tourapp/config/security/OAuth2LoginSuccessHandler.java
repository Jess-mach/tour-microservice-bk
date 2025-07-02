package br.com.tourapp.config.security;


import br.com.tourapp.security.SecurityUser;
import br.com.tourapp.service.UserDetailsProvider;
import br.com.tourapp.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserDetailsProvider userDetailsProvider;

    @Value("${app.oauth2.redirectUri}")
    private String redirectUri;

    public OAuth2LoginSuccessHandler(
            JwtUtils jwtUtils,
            UserDetailsProvider userDetailsProvider
    ) {
        this.jwtUtils = jwtUtils;
        this.userDetailsProvider = userDetailsProvider;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = (String) oAuth2User.getAttributes().get("email");

        // Carregando SecurityUser para gerar o token JWT
        SecurityUser securityUser = userDetailsProvider.loadUserByUsername(email);
        String token = jwtUtils.generateJwtToken(securityUser);

        // Construindo a URL de redirecionamento com o token
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("token", token)
                .build().toUriString();

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}