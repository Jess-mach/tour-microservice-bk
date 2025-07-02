package br.com.tourapp.config.security;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.tourapp.dto.GoogleUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifier {
    private static final Logger logger = LoggerFactory.getLogger(GoogleTokenVerifier.class);

    private final GoogleIdTokenVerifier verifier;

    public GoogleTokenVerifier(@Value("${google.client-id}") String clientId) {
        logger.info("Inicializando verificador de token com Client ID: {}", clientId);

        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .setIssuer("https://accounts.google.com")
                .build();
    }

    public GoogleUserInfo verify(String idToken) {
        try {
            logger.info("Tentando verificar token: {}", idToken);

            GoogleIdToken token = verifier.verify(idToken);

            if (token != null) {
                GoogleIdToken.Payload payload = token.getPayload();

                // Log detalhado do payload
                logger.info("Payload verificado:");
                logger.info("Sub (User ID): {}", payload.getSubject());
                logger.info("Email: {}", payload.getEmail());
                logger.info("Name: {}", payload.get("name"));
                logger.info("Picture: {}", payload.get("picture"));

                return new GoogleUserInfo(
                        payload.getSubject(),
                        payload.getEmail(),
                        (String) payload.get("name"),
                        (String) payload.get("picture")
                );
            } else {
                logger.error("Token inválido ou não verificado");
                return null;
            }
        } catch (GeneralSecurityException e) {
            logger.error("Erro de segurança ao verificar token", e);
            return null;
        } catch (IOException e) {
            logger.error("Erro de IO ao verificar token", e);
            return null;
        } catch (Exception e) {
            logger.error("Erro inesperado ao verificar token", e);
            return null;
        }
    }

    // Método adicional para validação manual se necessário
    public boolean isTokenValid(String idToken) {
        try {
            return verifier.verify(idToken) != null;
        } catch (Exception e) {
            logger.error("Erro na validação do token", e);
            return false;
        }
    }
}