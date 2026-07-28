package com.commerceos.iam.infrastructure.persistence.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * Redis hash entity for refresh tokens.
 *
 * <p>Stored in Redis with a 7-day TTL. The {@code token} field is the key (the actual JWT refresh
 * token string). Indexed fields allow lookup by userId and username.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "refresh_tokens", timeToLive = 604800) // 7 days default
public class RefreshTokenEntity implements Serializable {

  @Id private String token;

  @Indexed private UUID userId;

  @Indexed private String username;

  @Indexed private boolean revoked;

  private Instant issuedAt;

  private Instant expiresAt;

  @TimeToLive private Long ttl;
}
