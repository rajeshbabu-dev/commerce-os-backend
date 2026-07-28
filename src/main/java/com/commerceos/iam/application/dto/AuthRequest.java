package com.commerceos.iam.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AuthRequest {

  public record LoginCommand(@NotBlank @Email String email, @NotBlank String password) {}

  public record RefreshTokenCommand(@NotBlank String refreshToken) {}

  /** Admin-only: create a new user with a specific role. */
  public record CreateUserCommand(
      @NotBlank String username,
      @NotBlank @Email String email,
      @NotBlank @Size(min = 8) String password,
      @NotBlank String roleName) {}

  /** Public self-registration: always assigned the VIEWER role. */
  public record SignUpCommand(
      @NotBlank @Size(min = 3, max = 50) String username,
      @NotBlank @Email String email,
      @NotBlank
          @Size(min = 8)
          @Pattern(
              regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
              message = "Password must contain uppercase, lowercase, and a number")
          String password) {}
}
