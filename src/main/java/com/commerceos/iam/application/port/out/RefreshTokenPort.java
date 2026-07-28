package com.commerceos.iam.application.port.out;

import java.time.Instant;
import java.util.UUID;

public interface RefreshTokenPort {
  void store(String token, UUID userId, String username, long ttlSeconds);

  boolean existsAndNotRevoked(String token);

  String getUsernameByToken(String token);

  Instant getExpiresAt(String token);

  void revoke(String token);

  void revokeAllByUserId(UUID userId);

  void delete(String token);
}
