package com.commerceos.iam.redis;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class LoginRateLimiterTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  @InjectMocks private LoginRateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(rateLimiter, "maxAttempts", 5);
    ReflectionTestUtils.setField(rateLimiter, "windowMinutes", 15);
    ReflectionTestUtils.setField(rateLimiter, "signupMaxAttempts", 5);
    ReflectionTestUtils.setField(rateLimiter, "signupWindowMinutes", 60);
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
  }

  @Test
  @DisplayName("Recording 6th attempt blocks user")
  void recordFailedAttempt_BlocksOnSixthAttempt() {
    String email = "test@example.com";
    when(valueOperations.increment("login:attempt:" + email)).thenReturn(6L);

    boolean blocked = rateLimiter.recordFailedAttempt(email);
    assertTrue(blocked);
  }

  @Test
  @DisplayName("Recording 1st attempt sets TTL")
  void recordFailedAttempt_SetsTtlOnFirstAttempt() {
    String email = "test@example.com";
    when(valueOperations.increment("login:attempt:" + email)).thenReturn(1L);

    boolean blocked = rateLimiter.recordFailedAttempt(email);
    assertFalse(blocked);
    verify(stringRedisTemplate).expire(eq("login:attempt:" + email), any());
  }
}
