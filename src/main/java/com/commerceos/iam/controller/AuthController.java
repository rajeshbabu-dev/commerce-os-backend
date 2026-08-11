package com.commerceos.iam.controller;

import com.commerceos.common.dto.ApiResponse;
import com.commerceos.iam.dto.request.CreateUserRequestDto;
import com.commerceos.iam.dto.request.LoginRequestDto;
import com.commerceos.iam.dto.request.RefreshTokenRequestDto;
import com.commerceos.iam.dto.request.SignupRequestDto;
import com.commerceos.iam.dto.response.AuthResponseDto;
import com.commerceos.iam.dto.response.UserResponseDto;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
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

  @PostMapping({"/auth/signup", "/auth/register"})
  public ResponseEntity<ApiResponse<UserResponseDto>> signUp(
      @Valid @RequestBody SignupRequestDto request, HttpServletRequest servletRequest) {
    String clientIp = extractClientIp(servletRequest);
    User user = authService.signUp(request, clientIp);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success("User registered successfully", UserResponseDto.fromEntity(user)));
  }

  @PostMapping("/auth/login")
  public ResponseEntity<ApiResponse<AuthResponseDto>> login(
      @Valid @RequestBody LoginRequestDto request) {
    AuthResponseDto response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.success("Login successful", response));
  }

  @PostMapping("/auth/refresh")
  public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(
      @Valid @RequestBody RefreshTokenRequestDto request) {
    AuthResponseDto response = authService.refreshToken(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
  }

  @PostMapping("/auth/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      @Valid @RequestBody RefreshTokenRequestDto request) {
    authService.logout(request.refreshToken());
    return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
  }

  @PostMapping("/auth/logout-all")
  public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal User user) {
    authService.logoutAll(user.getId());
    return ResponseEntity.ok(ApiResponse.success("All sessions logged out successfully"));
  }

  @GetMapping("/auth/me")
  public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(
      @AuthenticationPrincipal User user) {
    return ResponseEntity.ok(
        ApiResponse.success("User profile fetched successfully", UserResponseDto.fromEntity(user)));
  }

  @GetMapping("/health")
  public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
    Map<String, Object> details =
        Map.of(
            "status", "UP",
            "service", "commerceos-backend",
            "timestamp", java.time.Instant.now().toString());
    return ResponseEntity.ok(ApiResponse.success("Service is healthy", details));
  }

  @PostMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<UserResponseDto>> createUser(
      @Valid @RequestBody CreateUserRequestDto request) {
    User createdUser = authService.createUser(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "User created successfully", UserResponseDto.fromEntity(createdUser)));
  }

  @GetMapping("/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<ApiResponse<List<UserResponseDto>>> listUsers() {
    List<UserResponseDto> users = authService.listUsers();
    return ResponseEntity.ok(ApiResponse.success("Users fetched successfully", users));
  }

  private String extractClientIp(HttpServletRequest request) {
    String xForwardedFor = request.getHeader("X-Forwarded-For");
    if (xForwardedFor != null && !xForwardedFor.isBlank()) {
      return xForwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
  }
}
