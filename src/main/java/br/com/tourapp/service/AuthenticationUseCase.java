package br.com.tourapp.service;

import br.com.tourapp.dto.request.CompleteProfileRequest;
import br.com.tourapp.dto.response.CompleteProfileResponse;
import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import jakarta.validation.Valid;

import java.util.UUID;

public interface AuthenticationUseCase {
    JwtResponse authenticateWithGoogle(String token);

    TokenRefreshResponse refreshToken(String refreshToken);

    CompleteProfileResponse completeProfile(CompleteProfileRequest request, String username, UUID userId);
}
