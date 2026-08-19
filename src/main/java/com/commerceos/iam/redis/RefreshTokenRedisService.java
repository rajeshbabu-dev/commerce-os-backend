package com.commerceos.iam.redis;

import com.commerceos.iam.entity.RefreshToken;
import com.commerceos.iam.repository.RefreshTokenRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisService {

  private final RefreshTokenRepository redisRepo;

  public void store(String token, UUID userId, String username, long ttlSeconds) {
    RefreshToken entity =
        RefreshToken.builder()
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

  public boolean existsAndNotRevoked(String token) {
    return redisRepo.findByToken(token).map(t -> !t.isRevoked()).orElse(false);
  }

  public String getUsernameByToken(String token) {
    return redisRepo.findByToken(token).map(RefreshToken::getUsername).orElse(null);
  }

  public Instant getExpiresAt(String token) {
    return redisRepo.findByToken(token).map(RefreshToken::getExpiresAt).orElse(null);
  }

  public void revoke(String token) {
    redisRepo
        .findByToken(token)
        .ifPresent(
            storedToken -> {
              storedToken.setRevoked(true);
              redisRepo.save(storedToken);
            });
  }

  public void revokeAllByUserId(UUID userId) {
    redisRepo.deleteByUserId(userId);
  }

  public void delete(String token) {
    redisRepo.deleteByToken(token);
  }
}
