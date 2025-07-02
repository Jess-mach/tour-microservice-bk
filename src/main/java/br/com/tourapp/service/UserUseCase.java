package br.com.tourapp.service;

import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;

public interface UserUseCase {
    UserDetails loadUserDetailsByEmail(String email);

    UserInfoResponse getUserInfo(String username);

    Map<String, Object> checkSubscription(String username);

    void updateSubscription(String email, String plan, int months);

    UserService.Pair<UserEntity, UserDetails> processGoogleToken(String googleToken);

    JwtResponse buildJwtResponse(UserEntity user, String accessToken, String token);
}
