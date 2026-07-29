package com.commerceos.iam.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  @Test
  @DisplayName("RefreshToken building and state validation")
  void refreshTokenBuilding() {
    UUID userId = UUID.randomUUID();
    Instant now = Instant.now();
    RefreshToken token =
        RefreshToken.builder()
            .token("token123")
            .userId(userId)
            .username("test@example.com")
            .revoked(false)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .ttl(3600L)
            .build();

    assertEquals("token123", token.getToken());
    assertEquals(userId, token.getUserId());
    assertEquals("test@example.com", token.getUsername());
    assertFalse(token.isRevoked());
  }
}
