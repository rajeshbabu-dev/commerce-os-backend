package com.commerceos.iam.infrastructure.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Unit tests for {@link LoginRateLimiter} — tests the actual implementation with a mock Redis
 * backing store.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LoginRateLimiterTest {

  @Mock private StringRedisTemplate stringRedisTemplate;
  @Mock private ValueOperations<String, String> valueOperations;

  private LoginRateLimiter rateLimiter;

  @BeforeEach
  void setUp() {
    when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    rateLimiter = new LoginRateLimiter(stringRedisTemplate);
    // Login defaults: 5 attempts, 15-minute window
    ReflectionTestUtils.setField(rateLimiter, "maxAttempts", 5);
    ReflectionTestUtils.setField(rateLimiter, "windowMinutes", 15);
    // Signup defaults: 5 attempts, 60-minute window
    ReflectionTestUtils.setField(rateLimiter, "signupMaxAttempts", 5);
    ReflectionTestUtils.setField(rateLimiter, "signupWindowMinutes", 60);
  }

  // -- Signup rate limiting tests -----------------------------------------------

  @Test
  void isSignupBlocked_shouldReturnFalseWhenNoAttemptsExist() {
    when(valueOperations.get("signup:attempt:192.168.1.100")).thenReturn(null);

    assertFalse(rateLimiter.isSignupBlocked("192.168.1.100"));
  }

  @Test
  void isSignupBlocked_shouldReturnFalseWhenBelowThreshold() {
    when(valueOperations.get("signup:attempt:192.168.1.100")).thenReturn("3");

    assertFalse(rateLimiter.isSignupBlocked("192.168.1.100"));
  }

  @Test
  void isSignupBlocked_shouldReturnTrueWhenAtThreshold() {
    // Threshold is > maxAttempts (5), so 6 attempts should block
    when(valueOperations.get("signup:attempt:192.168.1.100")).thenReturn("6");

    assertTrue(rateLimiter.isSignupBlocked("192.168.1.100"));
  }

  @Test
  void recordSignupAttempt_shouldIncrementCounterAndReturnFalse() {
    // First attempt (counter goes from null to 1)
    when(valueOperations.increment("signup:attempt:192.168.1.100")).thenReturn(1L);

    assertFalse(rateLimiter.recordSignupAttempt("192.168.1.100"));

    // Should set TTL on first attempt
    verify(stringRedisTemplate).expire("signup:attempt:192.168.1.100", Duration.ofMinutes(60));
  }

  @Test
  void recordSignupAttempt_shouldReturnTrueWhenExceedsMax() {
    // 6th attempt (exceeds max of 5)
    when(valueOperations.increment("signup:attempt:192.168.1.100")).thenReturn(6L);

    assertTrue(rateLimiter.recordSignupAttempt("192.168.1.100"));
  }

  @Test
  void recordSignupAttempt_shouldNotSetTTLOnSubsequentAttempts() {
    // Second attempt (counter goes to 2) — TTL should not be set
    when(valueOperations.increment("signup:attempt:192.168.1.100")).thenReturn(2L);

    assertFalse(rateLimiter.recordSignupAttempt("192.168.1.100"));

    // TTL should NOT be set on non-first attempts
    verify(stringRedisTemplate, never()).expire(anyString(), any(Duration.class));
  }

  @Test
  void clearSignupAttempts_shouldDeleteRedisKey() {
    rateLimiter.clearSignupAttempts("192.168.1.100");

    verify(stringRedisTemplate).delete("signup:attempt:192.168.1.100");
  }

  @Test
  void clearSignupAttempts_shouldAllowSignupAfterClear() {
    // Simulate: IP was blocked, then cleared
    when(valueOperations.get("signup:attempt:192.168.1.100")).thenReturn("6");
    assertTrue(rateLimiter.isSignupBlocked("192.168.1.100"));

    rateLimiter.clearSignupAttempts("192.168.1.100");

    // After clear, Redis returns null
    when(valueOperations.get("signup:attempt:192.168.1.100")).thenReturn(null);
    assertFalse(rateLimiter.isSignupBlocked("192.168.1.100"));
  }

  // -- Login rate limiting tests (existing) -------------------------------------

  @Test
  void isBlocked_shouldReturnFalseWhenNoAttemptsExist() {
    when(valueOperations.get("login:attempt:john@test.com")).thenReturn(null);

    assertFalse(rateLimiter.isBlocked("john@test.com"));
  }

  @Test
  void isBlocked_shouldReturnTrueWhenExceedsMax() {
    when(valueOperations.get("login:attempt:john@test.com")).thenReturn("6");

    assertTrue(rateLimiter.isBlocked("john@test.com"));
  }

  @Test
  void recordFailedAttempt_shouldIncrementCounterAndSetTTL() {
    when(valueOperations.increment("login:attempt:john@test.com")).thenReturn(1L);

    assertFalse(rateLimiter.recordFailedAttempt("john@test.com"));

    verify(stringRedisTemplate).expire("login:attempt:john@test.com", Duration.ofMinutes(15));
  }

  @Test
  void clearAttempts_shouldDeleteRedisKey() {
    rateLimiter.clearAttempts("john@test.com");

    verify(stringRedisTemplate).delete("login:attempt:john@test.com");
  }
}
