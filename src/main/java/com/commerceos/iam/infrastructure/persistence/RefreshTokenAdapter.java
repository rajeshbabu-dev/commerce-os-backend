package com.commerceos.iam.infrastructure.persistence;

import com.commerceos.iam.application.port.out.RefreshTokenPort;
import com.commerceos.iam.infrastructure.persistence.entity.RefreshTokenEntity;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter between the {@link RefreshTokenPort} port and the Redis-backed {@link RefreshTokenEntity}
 * persistence layer.
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenAdapter implements RefreshTokenPort {

  private final RefreshTokenRepository redisRepo;

  @Override
  public void store(String token, UUID userId, String username, long ttlSeconds) {
    RefreshTokenEntity entity =
        RefreshTokenEntity.builder()
            .token(token)
            .userId(userId)
            .username(username)
            .revoked(false)
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(ttlSeconds))
            .ttl(ttlSeconds)
            .build();
    redisRepo.save(entity);
  }

  @Override
  public boolean existsAndNotRevoked(String token) {
    return redisRepo.findByToken(token).map(t -> !t.isRevoked()).orElse(false);
  }

  @Override
  public String getUsernameByToken(String token) {
    return redisRepo.findByToken(token).map(RefreshTokenEntity::getUsername).orElse(null);
  }

  @Override
  public Instant getExpiresAt(String token) {
    return redisRepo.findByToken(token).map(RefreshTokenEntity::getExpiresAt).orElse(null);
  }

  @Override
  public void revoke(String token) {
    redisRepo
        .findByToken(token)
        .ifPresent(
            storedToken -> {
              storedToken.setRevoked(true);
              redisRepo.save(storedToken);
            });
  }

  @Override
  public void revokeAllByUserId(UUID userId) {
    redisRepo.deleteByUserId(userId);
  }

  @Override
  public void delete(String token) {
    redisRepo.deleteByToken(token);
  }
}
