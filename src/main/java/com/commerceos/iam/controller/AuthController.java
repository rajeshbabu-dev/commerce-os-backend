package com.commerceos.iam.controller;

import com.commerceos.iam.dto.ApiResponse;
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
  public ResponseEntity<ApiResponse<AuthResponse>> signUp(
      @Valid @RequestBody SignupRequest request, HttpServletRequest servletRequest) {
    String clientIp = extractClientIp(servletRequest);
    AuthResponse response = authService.signUp(request, clientIp);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("User registered successfully", response));
  }

  @PostMapping("/auth/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    AuthResponse response = authService.refreshToken(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody RefreshTokenRequest request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
  }

  @PostMapping("/auth/logout-all")
  public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
    authService.logoutAll(user.getId());
    return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully"));
  }

  @GetMapping("/auth/me")
  public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        ApiResponse.success("User profile fetched successfully", UserResponse.fromEntity(user)));
  }

  @PostMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
      @Valid @RequestBody CreateUserRequest request) {
    User createdUser = authService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success("User created successfully", UserResponse.fromEntity(createdUser)));
  }

  private String extractClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
