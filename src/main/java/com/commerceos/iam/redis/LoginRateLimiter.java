package com.commerceos.iam.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis-backed rate limiter for login and signup.
 *
 * <p>Tracks failed login attempts per email address. After {@code maxAttempts} failed attempts
 * within a {@code windowDuration} sliding window, further attempts are blocked until the window
 * expires or the key is manually cleared (on successful login).
 *
 * <p>Also tracks signup attempts per IP address to prevent mass account creation.
 *
 * <p>Login key pattern: {@code login:attempt:{email}}
 *
 * <p>Signup key pattern: {@code signup:attempt:{ip}}
 *
 * <p>Per 07-SECURITY-AND-ACCESS.md §4: "More than 5 failed logins in a short window blocks further
 * attempts temporarily."
 */
@Component
@RequiredArgsConstructor
public class LoginRateLimiter {

  private final StringRedisTemplate stringRedisTemplate;

  @Value("${auth.rate-limit.login.max-attempts:5}")
  private int maxAttempts;

  @Value("${auth.rate-limit.login.window-minutes:15}")
  private int windowMinutes;

  @Value("${auth.rate-limit.signup.max-attempts:5}")
  private int signupMaxAttempts;

  @Value("${auth.rate-limit.signup.window-minutes:60}")
  private int signupWindowMinutes;

  private static final String KEY_PREFIX = "login:attempt:";
  private static final String SIGNUP_KEY_PREFIX = "signup:attempt:";

  /**
   * Records a failed login attempt and returns {@code true} if the caller is now blocked.
   *
   * @param email the email address that failed to log in
   * @return {@code true} if the email has exceeded the maximum allowed attempts and should be
   *     blocked
   */
  public boolean recordFailedAttempt(String email) {
    String key = KEY_PREFIX + email;
    Long attempt = stringRedisTemplate.opsForValue().increment(key);

    // Set TTL on first increment (key didn't exist before)
    if (attempt != null && attempt == 1) {
      stringRedisTemplate.expire(key, Duration.ofMinutes(windowMinutes));
    }

    return attempt != null && attempt > maxAttempts;
  }

  /**
   * Returns {@code true} if the email is currently rate-limited and should be blocked from logging
   * in.
   */
  public boolean isBlocked(String email) {
    String key = KEY_PREFIX + email;
    String value = stringRedisTemplate.opsForValue().get(key);
    if (value == null) {
      return false;
    }
    int attempts = Integer.parseInt(value);
    return attempts > maxAttempts;
  }

  /** Clears the rate-limit counter — called after a successful login. */
  public void clearAttempts(String email) {
    String key = KEY_PREFIX + email;
    stringRedisTemplate.delete(key);
  }

  // -- Signup rate limiting (keyed by IP) ----------------------------------------

  /**
   * Records a signup attempt from the given IP address.
   *
   * @param ip the client IP address
   * @return {@code true} if the IP has exceeded the maximum allowed signup attempts
   */
  public boolean recordSignupAttempt(String ip) {
    String key = SIGNUP_KEY_PREFIX + ip;
    Long attempt = stringRedisTemplate.opsForValue().increment(key);

    if (attempt != null && attempt == 1) {
      stringRedisTemplate.expire(key, Duration.ofMinutes(signupWindowMinutes));
    }

    return attempt != null && attempt > signupMaxAttempts;
  }

  /** Returns {@code true} if the IP is currently rate-limited for signup. */
  public boolean isSignupBlocked(String ip) {
    String key = SIGNUP_KEY_PREFIX + ip;
    String value = stringRedisTemplate.opsForValue().get(key);
    if (value == null) {
      return false;
    }
    int attempts = Integer.parseInt(value);
    return attempts > signupMaxAttempts;
  }

  /** Clears the signup rate-limit counter for the given IP. */
  public void clearSignupAttempts(String ip) {
    String key = SIGNUP_KEY_PREFIX + ip;
    stringRedisTemplate.delete(key);
  }
}
