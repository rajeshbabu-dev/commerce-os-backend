package com.commerceos.iam.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.commerceos.iam.dto.request.CreateUserRequest;
import com.commerceos.iam.dto.request.LoginRequest;
import com.commerceos.iam.dto.request.SignupRequest;
import com.commerceos.iam.entity.Role;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.redis.LoginRateLimiter;
import com.commerceos.iam.redis.RefreshTokenRedisService;
import com.commerceos.iam.repository.RoleRepository;
import com.commerceos.iam.repository.UserRepository;
import com.commerceos.platform.exception.BusinessException;
import com.commerceos.platform.security.JwtUtil;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private RoleRepository roleRepository;
  @Mock private RefreshTokenRedisService refreshTokenRedisService;
  @Mock private LoginRateLimiter loginRateLimiter;
  @Mock private JwtUtil jwtUtil;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthService authService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(authService, "refreshTokenTtlMs", 604800000L);
    ReflectionTestUtils.setField(authService, "accessTokenTtlMs", 900000L);
  }

  @Test
  @DisplayName("createUser success when admin creates user")
  void createUser_Success() {
    CreateUserRequest req =
        new CreateUserRequest("opsuser", "ops@example.com", "Password123", "OPS_EXECUTIVE");
    Role role = Role.builder().id(UUID.randomUUID()).name("OPS_EXECUTIVE").build();
    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .username("opsuser")
            .email("ops@example.com")
            .roles(Set.of(role))
            .build();

    when(userRepository.existsByEmail(req.email())).thenReturn(false);
    when(userRepository.existsByUsername(req.username())).thenReturn(false);
    when(roleRepository.findByName("OPS_EXECUTIVE")).thenReturn(Optional.of(role));
    when(passwordEncoder.encode(req.password())).thenReturn("hashed");
    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    User result = authService.createUser(req);

    assertNotNull(result);
    assertEquals("ops@example.com", result.getUsername());
    assertEquals("ops@example.com", result.getEmail());
  }

  @Test
  @DisplayName("signUp throws exception if email exists")
  void signUp_DuplicateEmail() {
    SignupRequest req = new SignupRequest("user1", "existing@example.com", "Password123");
    when(loginRateLimiter.isSignupBlocked("127.0.0.1")).thenReturn(false);
    when(userRepository.existsByEmail(req.email())).thenReturn(true);

    assertThrows(BusinessException.class, () -> authService.signUp(req, "127.0.0.1"));
  }

  @Test
  @DisplayName("login records failed attempt on BadCredentialsException")
  void login_FailedAttempt() {
    LoginRequest req = new LoginRequest("test@example.com", "wrongpass");
    when(loginRateLimiter.isBlocked(req.email())).thenReturn(false);
    when(authenticationManager.authenticate(any()))
        .thenThrow(new BadCredentialsException("Invalid"));
    when(loginRateLimiter.recordFailedAttempt(req.email())).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> authService.login(req));
    verify(loginRateLimiter).recordFailedAttempt(req.email());
  }
}
