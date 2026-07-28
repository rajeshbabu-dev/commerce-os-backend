package com.commerceos.iam.application.service;

import com.commerceos.iam.application.dto.AuthRequest;
import com.commerceos.iam.application.dto.AuthResponse;
import com.commerceos.iam.application.port.in.AuthUseCase;
import com.commerceos.iam.application.port.out.JwtPort;
import com.commerceos.iam.application.port.out.LoginRateLimiterPort;
import com.commerceos.iam.application.port.out.RefreshTokenPort;
import com.commerceos.iam.application.port.out.RoleRepository;
import com.commerceos.iam.application.port.out.UserRepository;
import com.commerceos.iam.domain.model.Role;
import com.commerceos.iam.domain.model.User;
import com.commerceos.platform.exception.BusinessException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthUseCase {

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtPort jwtPort;
  private final AuthenticationManager authenticationManager;
  private final UserDetailsService userDetailsService;
  private final RefreshTokenPort refreshTokenPort;
  private final LoginRateLimiterPort loginRateLimiter;

  @Value("${jwt.refresh-token-ttl}")
  private long refreshTokenTtlMs;

  @Override
  @Transactional
  public User createUser(AuthRequest.CreateUserCommand command) {
    if (userRepository.existsByUsername(command.username())) {
      throw new IllegalArgumentException("Username already taken");
    }
    if (userRepository.existsByEmail(command.email())) {
      throw new IllegalArgumentException("Email already registered");
    }

    Role role =
        roleRepository
            .findByName(command.roleName())
            .orElseThrow(
                () -> new IllegalArgumentException("Role not found: " + command.roleName()));

    User user =
        User.builder()
            .username(command.username())
            .email(command.email())
            .passwordHash(passwordEncoder.encode(command.password()))
            .roles(new HashSet<>(Collections.singletonList(role)))
            .build();

    return userRepository.save(user);
  }

  @Override
  @Transactional
  public AuthResponse signUp(AuthRequest.SignUpCommand command, String clientIp) {
    // Rate limit: check if this IP has exceeded signup attempts
    if (clientIp != null && loginRateLimiter.isSignupBlocked(clientIp)) {
      throw new BusinessException(
          "RATE_LIMIT_EXCEEDED", "Too many signup attempts. Please try again later.", 429);
    }

    if (userRepository.existsByUsername(command.username())) {
      throw new IllegalArgumentException("Username already taken");
    }
    if (userRepository.existsByEmail(command.email())) {
      throw new IllegalArgumentException("Email already registered");
    }

    Role viewerRole =
        roleRepository
            .findByName("VIEWER")
            .orElseThrow(() -> new IllegalStateException("VIEWER role not found in database"));

    User user =
        User.builder()
            .username(command.username())
            .email(command.email())
            .passwordHash(passwordEncoder.encode(command.password()))
            .roles(new HashSet<>(Collections.singletonList(viewerRole)))
            .build();

    userRepository.save(user);

    // Rate limit: record successful signup from this IP
    if (clientIp != null) {
      loginRateLimiter.recordSignupAttempt(clientIp);
    }

    // Auto-login: load the saved user directly (avoids race condition with UserDetailsService)
    User savedUser =
        userRepository
            .findByEmail(user.getEmail())
            .orElseThrow(() -> new IllegalStateException("User not found after save"));
    return issueTokens(savedUser);
  }

  @Override
  @Transactional
  public AuthResponse login(AuthRequest.LoginCommand command) {
    // Step 1: Check rate limit before attempting authentication
    if (loginRateLimiter.isBlocked(command.email())) {
      throw new BusinessException(
          "RATE_LIMIT_EXCEEDED",
          "Too many login attempts. Please try again in a few minutes.",
          429);
    }

    try {
      // Step 2: Authenticate (may throw BadCredentialsException)
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(command.email(), command.password()));
      UserDetails userDetails = userDetailsService.loadUserByUsername(command.email());

      // Step 3: Successful login — clear rate limit counter
      loginRateLimiter.clearAttempts(command.email());

      // Step 4: Issue tokens
      return issueTokens(userDetails);

    } catch (BadCredentialsException e) {
      // Step 5: Failed login — record the attempt
      loginRateLimiter.recordFailedAttempt(command.email());
      throw e; // Let GlobalExceptionHandler format the 401 response
    }
  }

  @Override
  @Transactional
  public AuthResponse refreshToken(String refreshToken) {
    // Check if token exists in Redis and is not revoked
    if (!refreshTokenPort.existsAndNotRevoked(refreshToken)) {
      throw new IllegalArgumentException("Invalid or revoked refresh token");
    }

    String username = refreshTokenPort.getUsernameByToken(refreshToken);
    if (username == null) {
      throw new IllegalArgumentException("Refresh token not found");
    }

    // Explicitly check expiration
    Instant expiresAt = refreshTokenPort.getExpiresAt(refreshToken);
    if (expiresAt != null && expiresAt.isBefore(Instant.now())) {
      refreshTokenPort.delete(refreshToken);
      throw new IllegalArgumentException("Refresh token expired");
    }

    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

    // Issue a new access token AND a new refresh token FIRST (safer order —
    // if this fails, the old token is still valid and the user can retry)
    AuthResponse newTokens = issueTokens(userDetails);

    // Revoke the old refresh token only after successful issuance (rotation)
    refreshTokenPort.revoke(refreshToken);

    return newTokens;
  }

  @Override
  @Transactional
  public void logout(String refreshToken) {
    // Revoke the refresh token
    refreshTokenPort.revoke(refreshToken);
  }

  @Override
  @Transactional
  public void logoutAll(UUID userId) {
    // Delete all refresh tokens for the user
    refreshTokenPort.revokeAllByUserId(userId);
  }

  // -- Private helpers -----------------------------------------------------------

  /** Issues a paired access + refresh token and stores the refresh token in Redis. */
  private AuthResponse issueTokens(UserDetails userDetails) {
    String accessToken = jwtPort.generateAccessToken(userDetails);
    String refreshToken = jwtPort.generateRefreshToken(userDetails);

    UUID userId = null;
    String email = null;
    if (userDetails instanceof User user) {
      userId = user.getId();
      email = user.getEmail();
    }
    // Store the email (not username) because UserDetailsService loads by email
    long ttlSeconds = refreshTokenTtlMs / 1000;
    refreshTokenPort.store(refreshToken, userId, email, ttlSeconds);

    return new AuthResponse(accessToken, refreshToken);
  }
}
