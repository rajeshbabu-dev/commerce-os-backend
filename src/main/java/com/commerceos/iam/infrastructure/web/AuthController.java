package com.commerceos.iam.infrastructure.web;

import com.commerceos.iam.application.dto.AuthRequest;
import com.commerceos.iam.application.dto.AuthResponse;
import com.commerceos.iam.application.dto.UserResponse;
import com.commerceos.iam.application.port.in.AuthUseCase;
import com.commerceos.iam.domain.model.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

  private final AuthUseCase authUseCase;

  // -- Authentication endpoints (public) ----------------------------------------

  @PostMapping("/api/v1/auth/signup")
  public ResponseEntity<AuthResponse> signUp(
      @Valid @RequestBody AuthRequest.SignUpCommand request, HttpServletRequest httpRequest) {
    String clientIp = extractClientIp(httpRequest);
    return ResponseEntity.ok(authUseCase.signUp(request, clientIp));
  }

  @PostMapping("/api/v1/auth/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest.LoginCommand request) {
    return ResponseEntity.ok(authUseCase.login(request));
  }

  @PostMapping("/api/v1/auth/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @RequestBody AuthRequest.RefreshTokenCommand request) {
    return ResponseEntity.ok(authUseCase.refreshToken(request.refreshToken()));
  }

  @PostMapping("/api/v1/auth/logout")
  public ResponseEntity<Void> logout(@RequestBody AuthRequest.RefreshTokenCommand request) {
    authUseCase.logout(request.refreshToken());
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/api/v1/auth/logout-all")
  public ResponseEntity<Void> logoutAll(@AuthenticationPrincipal User user) {
    authUseCase.logoutAll(user.getId());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/api/v1/auth/me")
  public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal User user) {
    Set<String> roleNames =
        user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());

    Set<String> permissionNames =
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());

    UserResponse response =
        new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roleNames,
            permissionNames,
            user.getCreatedAt(),
            user.getUpdatedAt());

    return ResponseEntity.ok(response);
  }

  // -- Admin endpoints (admin-only) ---------------------------------------------

  @PostMapping("/api/v1/admin/users")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<UserResponse> createUser(
      @Valid @RequestBody AuthRequest.CreateUserCommand request) {
    User user = authUseCase.createUser(request);

    Set<String> roleNames =
        user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());

    Set<String> permissionNames =
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());

    UserResponse response =
        new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            roleNames,
            permissionNames,
            user.getCreatedAt(),
            user.getUpdatedAt());

    return ResponseEntity.ok(response);
  }

  // -- Private helpers ----------------------------------------------------------

  /**
   * Extracts the real client IP from the request, respecting reverse-proxy headers like
   * X-Forwarded-For and X-Real-IP.
   */
  private String extractClientIp(HttpServletRequest request) {
    String xff = request.getHeader("X-Forwarded-For");
    if (xff != null && !xff.isBlank()) {
      // X-Forwarded-For may contain a comma-separated list; the first entry is the client IP
      return xff.split(",")[0].trim();
    }
    String xRealIp = request.getHeader("X-Real-IP");
    if (xRealIp != null && !xRealIp.isBlank()) {
      return xRealIp.trim();
    }
    return request.getRemoteAddr();
  }
}
