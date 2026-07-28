package com.commerceos.iam.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

/**
 * Domain model for a refresh token.
 *
 * <p>Pure Java — no framework annotations. Redis persistence concerns are handled by {@code
 * RefreshTokenEntity} in the infrastructure layer.
 */
@Getter
@Builder
public class RefreshToken {

  private final String token;
  private final UUID userId;
  private final String username;
  private final boolean revoked;
  private final Instant issuedAt;
  private final Instant expiresAt;
  private final Long ttl;

  public static RefreshToken create(String token, UUID userId, String username, long ttlSeconds) {
    return RefreshToken.builder()
        .token(token)
        .userId(userId)
        .username(username)
        .revoked(false)
        .issuedAt(Instant.now())
        .expiresAt(Instant.now().plusSeconds(ttlSeconds))
        .ttl(ttlSeconds)
        .build();
  }

  public boolean isExpired() {
    return expiresAt != null && expiresAt.isBefore(Instant.now());
  }

  public RefreshToken revoke() {
    return RefreshToken.builder()
        .token(this.token)
        .userId(this.userId)
        .username(this.username)
        .revoked(true)
        .issuedAt(this.issuedAt)
        .expiresAt(this.expiresAt)
        .ttl(this.ttl)
        .build();
  }
}
