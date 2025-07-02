package br.com.tourapp.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration}")
    private long jwtExpirationMs;

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 dias por padrão
    private long refreshExpirationMs;

    private Key secretKey;

    @PostConstruct
    public void init() {
        // Gerar uma chave segura para HS512 para os tokens INTERNOS da aplicação
        // IMPORTANTE: Este JwtUtils é apenas para tokens internos da aplicação,
        // NÃO para validar Google ID Tokens (que usam RS256)
        secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS512);
        logger.info("JwtUtils inicializado para tokens internos da aplicação");
    }

    /**
     * Gera um token JWT INTERNO da aplicação (não confundir com Google ID Token)
     */
    public String generateJwtToken(UserDetails securityUser) {
        logger.debug("Gerando token JWT interno para usuário: {}", securityUser.getUsername());

        return Jwts.builder()
                .setSubject(securityUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Gera um refresh token INTERNO da aplicação
     */
    public String generateRefreshToken(UserDetails securityUser) {
        logger.debug("Gerando refresh token interno para usuário: {}", securityUser.getUsername());

        return Jwts.builder()
                .setSubject(securityUser.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * Extrai o username de um token JWT INTERNO da aplicação
     */
    public String getUsernameFromJwtToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            logger.error("Erro ao extrair username do token interno: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Valida um token JWT INTERNO da aplicação
     * IMPORTANTE: Este método NÃO deve ser usado para validar Google ID Tokens
     */
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            logger.error("Token JWT interno inválido: {}", e.getMessage());
            return false;
        }
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return (expiration.getTime() - System.currentTimeMillis()) < 300000; // 5 minutos
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * IMPORTANTE: Método para esclarecer que Google ID Tokens devem ser validados
     * usando GoogleTokenVerifier, não este JwtUtils
     */
    public void logGoogleTokenWarning() {
        logger.warn("ATENÇÃO: Para validar Google ID Tokens, use GoogleTokenVerifier, não JwtUtils!");
    }
}