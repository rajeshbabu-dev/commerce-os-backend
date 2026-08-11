package com.commerceos.notification.redis;

import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed per-recipient email rate limiter (TICKET-17).
 *
 * <p>Limits the number of emails a single user may receive per rolling hour window.
 */
@Component
@RequiredArgsConstructor
public class EmailRateLimiter {

  private static final String KEY_PREFIX = "notification:email:rate:";
  private static final Duration WINDOW = Duration.ofHours(1);
  private static final long MAX_PER_WINDOW = 10;

  private final StringRedisTemplate stringRedisTemplate;

  public boolean isAllowed(UUID userId) {
    long window = System.currentTimeMillis() / WINDOW.toMillis();
    String key = KEY_PREFIX + userId + ":" + window;
    Long count = stringRedisTemplate.opsForValue().increment(key);
    if (count != null && count == 1) {
      stringRedisTemplate.expire(key, WINDOW);
    }
    return count == null || count <= MAX_PER_WINDOW;
  }
}
