package com.commerceos.iam.service.impl;

import com.commerceos.iam.dto.request.CreateUserRequestDto;
import com.commerceos.iam.dto.request.LoginRequestDto;
import com.commerceos.iam.dto.request.SignupRequestDto;
import com.commerceos.iam.dto.response.AuthResponseDto;
import com.commerceos.iam.dto.response.UserResponseDto;
import com.commerceos.iam.entity.Role;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.redis.LoginRateLimiter;
import com.commerceos.iam.redis.RefreshTokenRedisService;
import com.commerceos.iam.repository.RoleRepository;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.iam.service.AuthService;
import com.commerceos.platform.exception.DuplicateResourceException;
import com.commerceos.platform.exception.InvalidCredentialsException;
import com.commerceos.platform.exception.RateLimitExceededException;
import com.commerceos.platform.exception.ResourceNotFoundException;
import com.commerceos.platform.security.JwtUtil;
import java.util.List;
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
public class AuthServiceImpl implements AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

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

  @Override
  @Transactional
  @PreAuthorize("hasRole('ADMIN')")
  public User createUser(CreateUserRequestDto request) {
    log.info(
        "Admin creating user with username: {} and email: {}", request.username(), request.email());

    if (userRepository.existsByEmail(request.email())) {
      throw new DuplicateResourceException(
          "EMAIL_ALREADY_EXISTS", "A user with this email already exists");
    }
    if (userRepository.existsByUsername(request.username())) {
      throw new DuplicateResourceException(
          "USERNAME_ALREADY_EXISTS", "A user with this username already exists");
    }

    Role role =
        roleRepository
            .findByName(request.roleName())
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "ROLE_NOT_FOUND", "Role not found: " + request.roleName()));

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

  @Override
  @Transactional
  public User signUp(SignupRequestDto request, String clientIp) {
    log.info("Public signup attempt for email: {} from IP: {}", request.email(), clientIp);

    if (loginRateLimiter.isSignupBlocked(clientIp)) {
      throw new RateLimitExceededException(
          "TOO_MANY_SIGNUP_ATTEMPTS",
          "Too many registration attempts from this IP address. Please try again later.");
    }

    if (userRepository.existsByEmail(request.email())) {
      loginRateLimiter.recordSignupAttempt(clientIp);
      throw new DuplicateResourceException(
          "EMAIL_ALREADY_EXISTS", "A user with this email already exists");
    }
    if (userRepository.existsByUsername(request.username())) {
      loginRateLimiter.recordSignupAttempt(clientIp);
      throw new DuplicateResourceException(
          "USERNAME_ALREADY_EXISTS", "A user with this username already exists");
    }

    Role viewerRole =
        roleRepository
            .findByName("VIEWER")
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "ROLE_NOT_FOUND", "Default VIEWER role not configured"));

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

    return savedUser;
  }

  @Override
  @Transactional(noRollbackFor = BadCredentialsException.class)
  public AuthResponseDto login(LoginRequestDto request) {
    log.info("Login attempt for email: {}", request.email());

    if (loginRateLimiter.isBlocked(request.email())) {
      throw new RateLimitExceededException(
          "ACCOUNT_LOCKED",
          "Too many failed login attempts. Account temporarily locked, try again later.");
    }

    try {
      authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.email(), request.password()));
    } catch (BadCredentialsException ex) {
      boolean nowBlocked = loginRateLimiter.recordFailedAttempt(request.email());
      if (nowBlocked) {
        log.warn("Account locked after failed attempt for email: {}", request.email());
        throw new RateLimitExceededException(
            "ACCOUNT_LOCKED",
            "Too many failed login attempts. Account temporarily locked, try again later.");
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

  @Override
  @Transactional
  public AuthResponseDto refreshToken(String refreshToken) {
    log.info("Token refresh attempt");

    if (!refreshTokenRedisService.existsAndNotRevoked(refreshToken)) {
      throw new InvalidCredentialsException("Refresh token is invalid or has been revoked");
    }

    String email = jwtUtil.extractUsername(refreshToken);
    User user =
        userRepository
            .findByEmail(email)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "USER_NOT_FOUND", "User not found for refresh token"));

    if (!jwtUtil.isTokenValid(refreshToken, user)) {
      throw new InvalidCredentialsException("Refresh token has expired");
    }

    refreshTokenRedisService.revoke(refreshToken);
    return generateAuthResponse(user);
  }

  @Override
  public void logout(String refreshToken) {
    log.info("Logout attempt");
    refreshTokenRedisService.revoke(refreshToken);
  }

  @Override
  public void logoutAll(UUID userId) {
    log.info("Logout all sessions attempt for user ID: {}", userId);
    refreshTokenRedisService.revokeAllByUserId(userId);
  }

  @Override
  public List<UserResponseDto> listUsers() {
    return userRepository.findAll().stream().map(UserResponseDto::fromEntity).toList();
  }

  private AuthResponseDto generateAuthResponse(User user) {
    String accessToken = jwtUtil.generateAccessToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    long ttlSeconds = refreshTokenTtlMs / 1000;
    refreshTokenRedisService.store(refreshToken, user.getId(), user.getEmail(), ttlSeconds);

    Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

    long accessTokenExpiresInSeconds = accessTokenTtlMs / 1000;
    return AuthResponseDto.of(accessToken, refreshToken, accessTokenExpiresInSeconds, roleNames);
  }
}
