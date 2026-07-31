package com.commerceos.iam.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequestDto(@NotBlank(message = "Refresh token is required") String refreshToken) {}
