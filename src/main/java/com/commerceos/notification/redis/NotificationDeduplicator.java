package com.commerceos.notification.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed idempotency guard for RabbitMQ event processing (per architecture doc §16).
 *
 * <p>Every event is keyed by a SHA-256 hash of its routing key + payload. The first occurrence
 * atomically acquires the key; redelivered duplicates are skipped until the key expires.
 */
@Component
@RequiredArgsConstructor
public class NotificationDeduplicator {

  private static final String KEY_PREFIX = "notification:dedup:";
  private static final Duration KEY_TTL = Duration.ofMinutes(10);

  private final StringRedisTemplate stringRedisTemplate;

  public boolean isDuplicate(String routingKey, String payload) {
    String key = KEY_PREFIX + sha256(routingKey + payload);
    Boolean firstSeen = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", KEY_TTL);
    return !Boolean.TRUE.equals(firstSeen);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 algorithm not available", e);
    }
  }
}
