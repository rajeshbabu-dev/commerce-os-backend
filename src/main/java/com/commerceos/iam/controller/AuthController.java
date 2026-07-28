package com.commerceos.iam.controller;

import com.commerceos.iam.dto.request.CreateUserRequest;
import com.commerceos.iam.dto.request.LoginRequest;
import com.commerceos.iam.dto.request.RefreshTokenRequest;
import com.commerceos.iam.dto.request.SignupRequest;
import com.commerceos.iam.dto.response.AuthResponse;
import com.commerceos.iam.dto.response.UserResponse;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/auth/signup")
  public ResponseEntity<AuthResponse> signUp(
      @Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
    String clientIp = extractClientIp(servletRequest);
    AuthResponse response = authService.signUp(request, clientIp);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PostMapping("/auth/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse response = authService.refreshToken(request.refreshToken());
    return ResponseEntity.ok(response);
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/auth/logout-all")
  public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal User user) {
    authService.logoutAll(user.getId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/auth/me")
  public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
    return ResponseEntity.ok(UserResponse.fromEntity(user));
  }

  @PostMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
    User createdUser = authService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(createdUser));
  }

  private String extractClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
