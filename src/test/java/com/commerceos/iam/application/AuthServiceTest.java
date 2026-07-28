package com.commerceos.iam.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.commerceos.iam.application.dto.AuthRequest;
import com.commerceos.iam.application.dto.AuthResponse;
import com.commerceos.iam.application.port.out.JwtPort;
import com.commerceos.iam.application.port.out.LoginRateLimiterPort;
import com.commerceos.iam.application.port.out.RefreshTokenPort;
import com.commerceos.iam.application.port.out.RoleRepository;
import com.commerceos.iam.application.port.out.UserRepository;
import com.commerceos.iam.application.service.AuthService;
import com.commerceos.iam.domain.model.Permission;
import com.commerceos.iam.domain.model.Role;
import com.commerceos.iam.domain.model.User;
import com.commerceos.platform.exception.BusinessException;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private JwtPort jwtPort;
  @Mock private AuthenticationManager authenticationManager;
  @Mock private UserDetailsService userDetailsService;
  @Mock private RefreshTokenPort refreshTokenPort;
  @Mock private LoginRateLimiterPort loginRateLimiter;

  private PasswordEncoder passwordEncoder;
  private AuthService authService;

  private Role viewerRole;
  private User testUser;

  @BeforeEach
  void setUp() {
    passwordEncoder = new BCryptPasswordEncoder();
    authService =
        new AuthService(
            userRepository,
            roleRepository,
            passwordEncoder,
            jwtPort,
            authenticationManager,
            userDetailsService,
            refreshTokenPort,
            loginRateLimiter);
    ReflectionTestUtils.setField(authService, "refreshTokenTtlMs", 604800000L);

    Permission readPerm = Permission.create("inventory:read", "");
    viewerRole = Role.create("VIEWER", "Read-only", Set.of(readPerm));
    testUser =
        User.builder()
            .id(UUID.randomUUID())
            .username("john")
            .email("john@test.com")
            .passwordHash(passwordEncoder.encode("password123"))
            .roles(Set.of(viewerRole))
            .build();
  }

  // -- createUser tests ---------------------------------------------------------

  @Test
  void createUser_shouldCreateUserWithSpecifiedRole() {
    when(userRepository.existsByUsername("john")).thenReturn(false);
    when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    AuthRequest.CreateUserCommand cmd =
        new AuthRequest.CreateUserCommand("john", "john@test.com", "password123", "VIEWER");
    User created = authService.createUser(cmd);

    assertEquals("john", created.getUsername());
    assertEquals("john@test.com", created.getEmail());
    assertTrue(passwordEncoder.matches("password123", created.getPasswordHash()));
    assertEquals(1, created.getRoles().size());
    assertEquals("VIEWER", created.getRoles().iterator().next().getName());
  }

  @Test
  void createUser_shouldThrowWhenUsernameTaken() {
    when(userRepository.existsByUsername("john")).thenReturn(true);

    AuthRequest.CreateUserCommand cmd =
        new AuthRequest.CreateUserCommand("john", "john@test.com", "password123", "VIEWER");
    assertThrows(IllegalArgumentException.class, () -> authService.createUser(cmd));
  }

  @Test
  void createUser_shouldThrowWhenEmailTaken() {
    when(userRepository.existsByUsername("john")).thenReturn(false);
    when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

    AuthRequest.CreateUserCommand cmd =
        new AuthRequest.CreateUserCommand("john", "john@test.com", "password123", "VIEWER");
    assertThrows(IllegalArgumentException.class, () -> authService.createUser(cmd));
  }

  @Test
  void createUser_shouldThrowWhenRoleNotFound() {
    when(userRepository.existsByUsername("john")).thenReturn(false);
    when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
    when(roleRepository.findByName("NONEXISTENT")).thenReturn(Optional.empty());

    AuthRequest.CreateUserCommand cmd =
        new AuthRequest.CreateUserCommand("john", "john@test.com", "password123", "NONEXISTENT");
    assertThrows(IllegalArgumentException.class, () -> authService.createUser(cmd));
  }

  // -- login tests --------------------------------------------------------------

  @Test
  void login_shouldReturnTokensForValidCredentials() {
    when(loginRateLimiter.isBlocked("john@test.com")).thenReturn(false);
    when(userDetailsService.loadUserByUsername("john@test.com")).thenReturn(testUser);
    when(jwtPort.generateAccessToken(testUser)).thenReturn("access-token");
    when(jwtPort.generateRefreshToken(testUser)).thenReturn("refresh-token");

    AuthRequest.LoginCommand cmd = new AuthRequest.LoginCommand("john@test.com", "password123");
    AuthResponse response = authService.login(cmd);

    assertNotNull(response);
    assertEquals("access-token", response.accessToken());
    assertEquals("refresh-token", response.refreshToken());
    verify(loginRateLimiter).clearAttempts("john@test.com");
    verify(refreshTokenPort).store(any(), eq(testUser.getId()), eq("john@test.com"), anyLong());
  }

  @Test
  void login_shouldThrowWhenRateLimited() {
    when(loginRateLimiter.isBlocked("john@test.com")).thenReturn(true);

    AuthRequest.LoginCommand cmd = new AuthRequest.LoginCommand("john@test.com", "password123");
    assertThrows(BusinessException.class, () -> authService.login(cmd));
    verify(authenticationManager, never()).authenticate(any());
  }

  @Test
  void login_shouldRecordFailedAttemptOnBadCredentials() {
    when(loginRateLimiter.isBlocked("john@test.com")).thenReturn(false);
    doThrow(new BadCredentialsException("Bad credentials"))
        .when(authenticationManager)
        .authenticate(any());

    AuthRequest.LoginCommand cmd = new AuthRequest.LoginCommand("john@test.com", "wrong");
    assertThrows(BadCredentialsException.class, () -> authService.login(cmd));
    verify(loginRateLimiter).recordFailedAttempt("john@test.com");
  }

  // -- refreshToken tests -------------------------------------------------------

  @Test
  void refreshToken_shouldReturnNewTokensAndRotate() {
    String oldRefreshToken = "old-refresh-token";
    when(refreshTokenPort.existsAndNotRevoked(oldRefreshToken)).thenReturn(true);
    when(refreshTokenPort.getUsernameByToken(oldRefreshToken)).thenReturn("john@test.com");
    when(refreshTokenPort.getExpiresAt(oldRefreshToken))
        .thenReturn(Instant.now().plusSeconds(3600));
    when(userDetailsService.loadUserByUsername("john@test.com")).thenReturn(testUser);
    when(jwtPort.generateAccessToken(testUser)).thenReturn("new-access-token");
    when(jwtPort.generateRefreshToken(testUser)).thenReturn("new-refresh-token");

    AuthResponse response = authService.refreshToken(oldRefreshToken);

    assertEquals("new-access-token", response.accessToken());
    assertEquals("new-refresh-token", response.refreshToken());
    // Old token should be revoked after successful issuance
    verify(refreshTokenPort).revoke(oldRefreshToken);
  }

  @Test
  void refreshToken_shouldThrowWhenRevoked() {
    when(refreshTokenPort.existsAndNotRevoked("revoked-token")).thenReturn(false);
    assertThrows(IllegalArgumentException.class, () -> authService.refreshToken("revoked-token"));
  }

  @Test
  void refreshToken_shouldThrowWhenExpired() {
    when(refreshTokenPort.existsAndNotRevoked("expired-token")).thenReturn(true);
    when(refreshTokenPort.getUsernameByToken("expired-token")).thenReturn("john@test.com");
    when(refreshTokenPort.getExpiresAt("expired-token"))
        .thenReturn(Instant.now().minusSeconds(3600));

    assertThrows(IllegalArgumentException.class, () -> authService.refreshToken("expired-token"));
    verify(refreshTokenPort).delete("expired-token");
  }

  // -- logout tests -------------------------------------------------------------

  @Test
  void logout_shouldRevokeToken() {
    authService.logout("some-token");
    verify(refreshTokenPort).revoke("some-token");
  }

  @Test
  void logoutAll_shouldRevokeAllUserTokens() {
    UUID userId = UUID.randomUUID();
    authService.logoutAll(userId);
    verify(refreshTokenPort).revokeAllByUserId(userId);
  }

  // -- signUp tests -------------------------------------------------------------  @Test
  void signUp_shouldCreateUserAndViewerRoleAndReturnTokens() {
    // Build a user that matches the signup command (newuser / new@test.com)
    User signupUser =
        User.builder()
            .id(UUID.randomUUID())
            .username("newuser")
            .email("new@test.com")
            .passwordHash(passwordEncoder.encode("Password1"))
            .roles(Set.of(viewerRole))
            .build();

    when(loginRateLimiter.isSignupBlocked("192.168.1.100")).thenReturn(false);
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.of(signupUser));
    when(jwtPort.generateAccessToken(signupUser)).thenReturn("signup-access");
    when(jwtPort.generateRefreshToken(signupUser)).thenReturn("signup-refresh");

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("newuser", "new@test.com", "Password1");
    AuthResponse response = authService.signUp(cmd, "192.168.1.100");

    assertNotNull(response);
    assertEquals("signup-access", response.accessToken());
    assertEquals("signup-refresh", response.refreshToken());

    // Verify VIEWER role was used (not any other role)
    verify(roleRepository).findByName("VIEWER");
    verify(userRepository).save(any());
    verify(loginRateLimiter).recordSignupAttempt("192.168.1.100");
    verify(refreshTokenPort).store(any(), eq(signupUser.getId()), eq("new@test.com"), anyLong());
  }

  @Test
  void signUp_shouldThrowWhenRateLimited() {
    when(loginRateLimiter.isSignupBlocked("192.168.1.100")).thenReturn(true);

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("newuser", "new@test.com", "Password1");
    assertThrows(BusinessException.class, () -> authService.signUp(cmd, "192.168.1.100"));
    verify(userRepository, never()).save(any());
  }

  @Test
  void signUp_shouldThrowWhenUsernameTaken() {
    when(loginRateLimiter.isSignupBlocked("192.168.1.100")).thenReturn(false);
    when(userRepository.existsByUsername("john")).thenReturn(true);

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("john", "new@test.com", "Password1");
    assertThrows(IllegalArgumentException.class, () -> authService.signUp(cmd, "192.168.1.100"));
  }

  @Test
  void signUp_shouldThrowWhenEmailTaken() {
    when(loginRateLimiter.isSignupBlocked("192.168.1.100")).thenReturn(false);
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("newuser", "john@test.com", "Password1");
    assertThrows(IllegalArgumentException.class, () -> authService.signUp(cmd, "192.168.1.100"));
  }

  @Test
  void signUp_shouldThrowWhenViewerRoleNotFound() {
    when(loginRateLimiter.isSignupBlocked("192.168.1.100")).thenReturn(false);
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.empty());

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("newuser", "new@test.com", "Password1");
    assertThrows(IllegalStateException.class, () -> authService.signUp(cmd, "192.168.1.100"));
  }

  @Test
  void signUp_shouldSkipRateLimitCheckWhenClientIpIsNull() {
    when(userRepository.existsByUsername("newuser")).thenReturn(false);
    when(userRepository.existsByEmail("new@test.com")).thenReturn(false);
    when(roleRepository.findByName("VIEWER")).thenReturn(Optional.of(viewerRole));
    when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.of(testUser));
    when(jwtPort.generateAccessToken(testUser)).thenReturn("access");
    when(jwtPort.generateRefreshToken(testUser)).thenReturn("refresh");

    AuthRequest.SignUpCommand cmd =
        new AuthRequest.SignUpCommand("newuser", "new@test.com", "Password1");
    AuthResponse response = authService.signUp(cmd, null);

    assertNotNull(response);
    // Should not interact with rate limiter when IP is null
    verify(loginRateLimiter, never()).isSignupBlocked(any());
    verify(loginRateLimiter, never()).recordSignupAttempt(any());
  }
}
