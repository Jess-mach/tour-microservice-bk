package br.com.tourapp.dto.response;


import java.util.List;

public record JwtResponse(
    String token,
    String refreshToken,
    String type,
    String id,
    String email,
    String name,
    List<String> roles,
    String profilePicture,
    String subscriptionPlan,
    boolean hasActiveSubscription,
    boolean success
) {
}

