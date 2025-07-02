package br.com.tourapp.service;

import br.com.tourapp.dto.response.JwtResponse;
import br.com.tourapp.dto.response.UserInfoResponse;
import br.com.tourapp.security.SecurityUser;

import java.util.Map;

public interface UserUseCase {
    SecurityUser loadSecurityUserByEmail(String email);

    UserInfoResponse getUserInfo(String username);

    Map<String, Object> checkSubscription(String username);

    void updateSubscription(String email, String plan, int months);

    UserService.Pair<UserEntity, SecurityUser> processGoogleToken(String googleToken);

    JwtResponse buildJwtResponse(UserEntity user, String accessToken, String token);
}
