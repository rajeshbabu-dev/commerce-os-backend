package com.commerceos.notification.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class EmailRateLimiterTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private EmailRateLimiter rateLimiter;

  @Test
  @DisplayName("First email in the window is allowed")
  void isAllowed_FirstEmail_True() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(anyString())).thenReturn(1L);

    assertTrue(rateLimiter.isAllowed(UUID.randomUUID()));
  }

  @Test
  @DisplayName("Email within the per-window limit is allowed")
  void isAllowed_WithinLimit_True() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(anyString())).thenReturn(10L);

    assertTrue(rateLimiter.isAllowed(UUID.randomUUID()));
  }

  @Test
  @DisplayName("Email above the per-window limit is blocked")
  void isAllowed_OverLimit_False() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.increment(anyString())).thenReturn(11L);

    assertFalse(rateLimiter.isAllowed(UUID.randomUUID()));
  }
}
