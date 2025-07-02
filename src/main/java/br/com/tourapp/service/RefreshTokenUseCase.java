package br.com.tourapp.service;

import br.com.tourapp.entity.RefreshTokenEntity;
import org.springframework.security.core.userdetails.UserDetails;

public interface RefreshTokenUseCase {
    void deleteByUserEmail(String username);

    RefreshTokenEntity createRefreshToken(String email, UserDetails userDetails);

    RefreshTokenEntity findAndValidateToken(String refreshToken);
}
