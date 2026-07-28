package com.commerceos.iam.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SignupRequest(
    @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        @Pattern(
            regexp = "^[a-zA-Z0-9_]+$",
            message = "Username can only contain letters, numbers, and underscores")
        String username,
    @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address")
        String email,
    @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain uppercase, lowercase, and a number")
        String password) {}
