package br.com.tourapp.service;

import br.com.tourapp.entity.RefreshTokenEntity;
import br.com.tourapp.security.SecurityUser;

public interface RefreshTokenUseCase {
    void deleteByUserEmail(String username);

    RefreshTokenEntity createRefreshToken(String email, SecurityUser securityUser);

    RefreshTokenEntity findAndValidateToken(String refreshToken);
}
