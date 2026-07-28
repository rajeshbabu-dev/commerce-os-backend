package com.commerceos.iam.application.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record UserResponse(
    UUID id,
    String username,
    String email,
    Set<String> roles,
    Set<String> permissions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
