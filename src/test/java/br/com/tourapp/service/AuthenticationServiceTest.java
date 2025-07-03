package br.com.tourapp.service;

import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.TokenRefreshResponse;
import br.com.tourapp.entity.RefreshTokenEntity;
import br.com.tourapp.entity.UserEntity;
import br.com.tourapp.security.SecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UserEntity userEntity;
    private UserDetails userDetails;
    private SecurityUser securityUser;
    private RefreshTokenEntity refreshTokenEntity;
    private JwtResponse jwtResponse;
    private TokenRefreshResponse tokenRefreshResponse;

    @BeforeEach
    void setUp() {
        // Setup UserEntity
        userEntity = new UserEntity();
        userEntity.setEmail("test@example.com");
        userEntity.setId(1L);
        userEntity.setFullName("Test User");

        // Setup UserDetails (mock)
        userDetails = mock(UserDetails.class);

        // Setup SecurityUser
        securityUser = mock(SecurityUser.class);

        // Setup RefreshTokenEntity
        refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setToken("refresh-token-123");
        refreshTokenEntity.setUserEmail("test@example.com");

        // Setup JwtResponse
        jwtResponse = new JwtResponse();
        jwtResponse.setRefreshToken("refresh-token-123");
        jwtResponse.setEmail("test@example.com");
        jwtResponse.setName("Test User");

        // Setup TokenRefreshResponse
        tokenRefreshResponse = new TokenRefreshResponse("new-access-token", "refresh-token-123");
    }

    @Test
    void authenticateWithGoogle_Success() {
        // Arrange
        String googleToken = "google-token-123";
        String accessToken = "access-token-123";

        UserService.Pair<UserEntity, UserDetails> userInfo = new UserService.Pair<>(userEntity, userDetails);

        when(userService.processGoogleToken(googleToken)).thenReturn(userInfo);
        when(userService.generateAccessToken(userDetails)).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(userEntity.getEmail(), userDetails))
                .thenReturn(refreshTokenEntity);
        when(userService.buildJwtResponse(userEntity, accessToken, refreshTokenEntity.getToken()))
                .thenReturn(jwtResponse);

        // Act
        JwtResponse result = authenticationService.authenticateWithGoogle(googleToken);

        // Assert
        assertNotNull(result);
        assertEquals(jwtResponse.getRefreshToken(), result.getRefreshToken());
        assertEquals(jwtResponse.getEmail(), result.getEmail());
        assertEquals(jwtResponse.getName(), result.getName());

        // Verify method calls
        verify(userService, times(1)).processGoogleToken(googleToken);
        verify(userService, times(1)).generateAccessToken(userDetails);
        verify(refreshTokenService, times(1)).createRefreshToken(userEntity.getEmail(), userDetails);
        verify(userService, times(1)).buildJwtResponse(userEntity, accessToken, refreshTokenEntity.getToken());
    }

    @Test
    void authenticateWithGoogle_UserServiceThrowsException() {
        // Arrange
        String googleToken = "invalid-google-token";

        when(userService.processGoogleToken(googleToken))
                .thenThrow(new RuntimeException("Invalid Google token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticateWithGoogle(googleToken);
        });

        // Verify that subsequent methods are not called
        verify(userService, times(1)).processGoogleToken(googleToken);
        verify(userService, never()).generateAccessToken(any());
        verify(refreshTokenService, never()).createRefreshToken(anyString(), any());
        verify(userService, never()).buildJwtResponse(any(), anyString(), anyString());
    }

    @Test
    void authenticateWithGoogle_RefreshTokenServiceThrowsException() {
        // Arrange
        String googleToken = "google-token-123";
        String accessToken = "access-token-123";

        UserService.Pair<UserEntity, UserDetails> userInfo = new UserService.Pair<>(userEntity, userDetails);

        when(userService.processGoogleToken(googleToken)).thenReturn(userInfo);
        when(userService.generateAccessToken(userDetails)).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken(userEntity.getEmail(), userDetails))
                .thenThrow(new RuntimeException("Failed to create refresh token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticateWithGoogle(googleToken);
        });

        // Verify method calls
        verify(userService, times(1)).processGoogleToken(googleToken);
        verify(userService, times(1)).generateAccessToken(userDetails);
        verify(refreshTokenService, times(1)).createRefreshToken(userEntity.getEmail(), userDetails);
        verify(userService, never()).buildJwtResponse(any(), anyString(), anyString());
    }

    @Test
    void refreshToken_Success() {
        // Arrange
        String refreshToken = "refresh-token-123";
        String newAccessToken = "new-access-token-123";

        when(refreshTokenService.findAndValidateToken(refreshToken)).thenReturn(refreshTokenEntity);
        when(userService.loadSecurityUserByEmail(refreshTokenEntity.getUserEmail())).thenReturn(securityUser);
        when(userService.generateAccessToken(securityUser)).thenReturn(newAccessToken);

        // Act
        TokenRefreshResponse result = authenticationService.refreshToken(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals(newAccessToken, result.getAccessToken());
        assertEquals(refreshToken, result.getRefreshToken());

        // Verify method calls
        verify(refreshTokenService, times(1)).findAndValidateToken(refreshToken);
        verify(userService, times(1)).loadSecurityUserByEmail(refreshTokenEntity.getUserEmail());
        verify(userService, times(1)).generateAccessToken(securityUser);
    }

    @Test
    void refreshToken_InvalidToken() {
        // Arrange
        String invalidRefreshToken = "invalid-refresh-token";

        when(refreshTokenService.findAndValidateToken(invalidRefreshToken))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(invalidRefreshToken);
        });

        // Verify method calls
        verify(refreshTokenService, times(1)).findAndValidateToken(invalidRefreshToken);
        verify(userService, never()).loadSecurityUserByEmail(anyString());
        verify(userService, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_UserNotFound() {
        // Arrange
        String refreshToken = "refresh-token-123";

        when(refreshTokenService.findAndValidateToken(refreshToken)).thenReturn(refreshTokenEntity);
        when(userService.loadSecurityUserByEmail(refreshTokenEntity.getUserEmail()))
                .thenThrow(new RuntimeException("User not found"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(refreshToken);
        });

        // Verify method calls
        verify(refreshTokenService, times(1)).findAndValidateToken(refreshToken);
        verify(userService, times(1)).loadSecurityUserByEmail(refreshTokenEntity.getUserEmail());
        verify(userService, never()).generateAccessToken(any());
    }

    @Test
    void refreshToken_TokenGenerationFails() {
        // Arrange
        String refreshToken = "refresh-token-123";

        when(refreshTokenService.findAndValidateToken(refreshToken)).thenReturn(refreshTokenEntity);
        when(userService.loadSecurityUserByEmail(refreshTokenEntity.getUserEmail())).thenReturn(securityUser);
        when(userService.generateAccessToken(securityUser))
                .thenThrow(new RuntimeException("Token generation failed"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(refreshToken);
        });

        // Verify method calls
        verify(refreshTokenService, times(1)).findAndValidateToken(refreshToken);
        verify(userService, times(1)).loadSecurityUserByEmail(refreshTokenEntity.getUserEmail());
        verify(userService, times(1)).generateAccessToken(securityUser);
    }

    @Test
    void refreshToken_NullToken() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(null);
        });

        // Verify that methods are still called (depends on implementation)
        verify(refreshTokenService, times(1)).findAndValidateToken(null);
    }

    @Test
    void authenticateWithGoogle_NullToken() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticateWithGoogle(null);
        });

        // Verify that processGoogleToken is called with null
        verify(userService, times(1)).processGoogleToken(null);
    }

    @Test
    void authenticateWithGoogle_EmptyToken() {
        // Arrange
        String emptyToken = "";

        when(userService.processGoogleToken(emptyToken))
                .thenThrow(new RuntimeException("Empty Google token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.authenticateWithGoogle(emptyToken);
        });

        // Verify method calls
        verify(userService, times(1)).processGoogleToken(emptyToken);
    }

    @Test
    void refreshToken_EmptyToken() {
        // Arrange
        String emptyToken = "";

        when(refreshTokenService.findAndValidateToken(emptyToken))
                .thenThrow(new RuntimeException("Empty refresh token"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            authenticationService.refreshToken(emptyToken);
        });

        // Verify method calls
        verify(refreshTokenService, times(1)).findAndValidateToken(emptyToken);
    }
}