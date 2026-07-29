package com.commerceos.iam.dto.response;

import java.util.Set;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    Set<String> roles) {

  public static AuthResponse of(
      String accessToken, String refreshToken, long expiresInSeconds, Set<String> roles) {
    return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, roles);
  }
}
