package com.commerceos.iam.application.port.out;

/** Port for rate limiting — records and checks failed login / signup attempts. */
public interface LoginRateLimiterPort {

  // -- Login rate limiting (keyed by email) ------------------------------------

  /**
   * Records a failed login attempt for the given email.
   *
   * @return {@code true} if the email has exceeded the maximum allowed attempts and should be
   *     blocked
   */
  boolean recordFailedAttempt(String email);

  /**
   * Returns {@code true} if the email is currently rate-limited and should be blocked from logging
   * in.
   */
  boolean isBlocked(String email);

  /** Clears the rate-limit counter — called after a successful login. */
  void clearAttempts(String email);

  // -- Signup rate limiting (keyed by IP address) -------------------------------

  /**
   * Records a signup attempt from the given IP address.
   *
   * @return {@code true} if the IP has exceeded the maximum allowed signup attempts
   */
  boolean recordSignupAttempt(String ip);

  /** Returns {@code true} if the IP is currently rate-limited for signup. */
  boolean isSignupBlocked(String ip);

  /** Clears the signup rate-limit counter for the given IP. */
  void clearSignupAttempts(String ip);
}
