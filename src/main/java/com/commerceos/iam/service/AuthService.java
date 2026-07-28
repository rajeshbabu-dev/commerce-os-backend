package com.commerceos.iam.service;

import com.commerceos.iam.dto.request.CreateUserRequest;
import com.commerceos.iam.dto.request.LoginRequest;
import com.commerceos.iam.dto.request.SignupRequest;
import com.commerceos.iam.dto.response.AuthResponse;
import com.commerceos.iam.entity.Role;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.exception.BusinessException;
import com.commerceos.iam.redis.LoginRateLimiter;
import com.commerceos.iam.redis.RefreshTokenRedisService;
import com.commerceos.iam.repository.RoleRepository;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.iam.util.JwtUtil;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final RefreshTokenRedisService refreshTokenRedisService;
  private final LoginRateLimiter loginRateLimiter;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;

  @Value("${jwt.refresh-token-ttl:604800000}")
  private long refreshTokenTtlMs;

  @Value("${jwt.access-token-ttl:900000}")
  private long accessTokenTtlMs;

  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public User createUser(CreateUserRequest request) {
    log.info(
        "Admin creating user with username: {} and email: {}", request.username(), request.email());

    if (userRepository.existsByEmail(request.email())) {
      throw new BusinessException(
          "EMAIL_ALREADY_EXISTS", "A user with this email already exists", 400);
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new BusinessException(
          "USERNAME_ALREADY_EXISTS", "A user with this username already exists", 400);
    }

    Role role =
        roleRepository
            .findByName(request.roleName())
            .orElseThrow(
                () ->
                    new BusinessException(
                        "ROLE_NOT_FOUND", "Role not found: " + request.roleName(), 400));

    User user =
        User.builder()
            .username(request.username())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .roles(Set.of(role))
            .build();

    User savedUser = userRepository.save(user);
    log.info("User created successfully with ID: {}", savedUser.getId());
    return savedUser;
  }

  @Transactional
  public AuthResponse signUp(SignupRequest request, String clientIp) {
    log.info("Public signup attempt for email: {} from IP: {}", request.email(), clientIp);

    if (loginRateLimiter.isSignupBlocked(clientIp)) {
      throw new BusinessException(
          "TOO_MANY_SIGNUP_ATTEMPTS",
          "Too many registration attempts from this IP address. Please try again later.",
          429);
    }

    if (userRepository.existsByEmail(request.email())) {
      loginRateLimiter.recordSignupAttempt(clientIp);
      throw new BusinessException(
          "EMAIL_ALREADY_EXISTS", "A user with this email already exists", 400);
    }
    if (userRepository.existsByUsername(request.username())) {
      loginRateLimiter.recordSignupAttempt(clientIp);
      throw new BusinessException(
          "USERNAME_ALREADY_EXISTS", "A user with this username already exists", 400);
    }

    Role viewerRole =
        roleRepository
            .findByName("VIEWER")
            .orElseThrow(
                () ->
                    new BusinessException(
                        "ROLE_NOT_FOUND", "Default VIEWER role not configured", 500));

    User user =
        User.builder()
            .username(request.username())
            .email(request.email())
            .passwordHash(passwordEncoder.encode(request.password()))
            .roles(Set.of(viewerRole))
            .build();

    User savedUser = userRepository.save(user);
    loginRateLimiter.clearSignupAttempts(clientIp);
    log.info("Public user self-registered successfully with ID: {}", savedUser.getId());

    return generateAuthResponse(savedUser);
  }

  @Transactional(noRollbackFor = BadCredentialsException.class)
  public AuthResponse login(LoginRequest request) {
    log.info("Login attempt for email: {}", request.email());

    if (loginRateLimiter.isBlocked(request.email())) {
      throw new BusinessException(
          "ACCOUNT_LOCKED",
          "Too many failed login attempts. Account temporarily locked, try again later.",
          429);
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (BadCredentialsException ex) {
      boolean nowBlocked = loginRateLimiter.recordFailedAttempt(request.email());
      if (nowBlocked) {
        log.warn("Account locked after failed attempt for email: {}", request.email());
        throw new BusinessException(
            "ACCOUNT_LOCKED",
            "Too many failed login attempts. Account temporarily locked, try again later.",
            429);
      }
      throw ex;
    }

    loginRateLimiter.clearAttempts(request.email());

    User user =
        userRepository
            .findByEmail(request.email())
            .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

    return generateAuthResponse(user);
  }

  @Transactional
  public AuthResponse refreshToken(String refreshToken) {
    log.info("Token refresh attempt");

    if (!refreshTokenRedisService.existsAndNotRevoked(refreshToken)) {
      throw new BusinessException(
          "INVALID_REFRESH_TOKEN", "Refresh token is invalid or has been revoked", 401);
    }

    String email = jwtUtil.extractUsername(refreshToken);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new BusinessException(
                        "USER_NOT_FOUND", "User not found for refresh token", 401));

    if (!jwtUtil.isTokenValid(refreshToken, user)) {
      throw new BusinessException("EXPIRED_REFRESH_TOKEN", "Refresh token has expired", 401);
    }

    refreshTokenRedisService.revoke(refreshToken);
    return generateAuthResponse(user);
  }

  public void logout(String refreshToken) {
    log.info("Logout attempt");
    refreshTokenRedisService.revoke(refreshToken);
  }

  public void logoutAll(UUID userId) {
    log.info("Logout all sessions attempt for user ID: {}", userId);
    refreshTokenRedisService.revokeAllByUserId(userId);
  }

  private AuthResponse generateAuthResponse(User user) {
    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    long ttlSeconds = refreshTokenTtlMs / 1000;
    refreshTokenRedisService.store(refreshToken, user.getId(), user.getEmail(), ttlSeconds);

    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    long accessTokenExpiresInSeconds = accessTokenTtlMs / 1000;
    return AuthResponse.of(accessToken, refreshToken, accessTokenExpiresInSeconds, roleNames);
  }
}
