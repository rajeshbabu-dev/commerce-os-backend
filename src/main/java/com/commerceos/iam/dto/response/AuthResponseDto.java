package com.commerceos.iam.dto.response;

import java.util.Set;

public record AuthResponseDto(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresInSeconds,
    Set<String> roles) {

  public static AuthResponseDto of(
      String accessToken, String refreshToken, long expiresInSeconds, Set<String> roles) {
    return new AuthResponseDto(accessToken, refreshToken, "Bearer", expiresInSeconds, roles);
  }
}
