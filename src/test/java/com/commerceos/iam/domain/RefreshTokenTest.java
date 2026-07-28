package com.commerceos.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.domain.model.RefreshToken;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private final UUID userId = UUID.randomUUID();

  @Test
  void create_shouldSetAllFields() {
    RefreshToken token = RefreshToken.create("token-123", userId, "john@test.com", 604800L);

    assertEquals("token-123", token.getToken());
    assertEquals(userId, token.getUserId());
    assertEquals("john@test.com", token.getUsername());
    assertFalse(token.isRevoked());
    assertNotNull(token.getIssuedAt());
    assertNotNull(token.getExpiresAt());
    assertEquals(604800L, token.getTtl());
  }

  @Test
  void isExpired_shouldReturnTrueWhenPastExpiry() {
    RefreshToken token =
        RefreshToken.builder()
            .token("token-123")
            .userId(userId)
            .username("john@test.com")
            .revoked(false)
            .issuedAt(Instant.now().minus(8, ChronoUnit.DAYS))
            .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
            .ttl(604800L)
            .build();

    assertTrue(token.isExpired());
  }

  @Test
  void isExpired_shouldReturnFalseWhenNotExpired() {
    RefreshToken token =
        RefreshToken.builder()
            .token("token-123")
            .userId(userId)
            .username("john@test.com")
            .revoked(false)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .ttl(604800L)
            .build();

    assertFalse(token.isExpired());
  }

  @Test
  void revoke_shouldReturnNewRevokedToken() {
    RefreshToken token = RefreshToken.create("token-123", userId, "john@test.com", 604800L);

    RefreshToken revoked = token.revoke();

    assertTrue(revoked.isRevoked());
    assertEquals(token.getToken(), revoked.getToken());
    assertEquals(token.getUserId(), revoked.getUserId());
    // Original should remain unchanged (immutable pattern)
    assertFalse(token.isRevoked());
  }
}
