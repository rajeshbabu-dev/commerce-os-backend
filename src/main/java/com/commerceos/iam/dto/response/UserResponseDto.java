package com.commerceos.iam.dto.response;

import com.commerceos.iam.entity.Role;
import com.commerceos.iam.entity.User;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponseDto(
    UUID id,
    String username,
    String email,
    boolean enabled,
    Set<String> roles,
    LocalDateTime createdAt) {

  public static UserResponseDto fromEntity(User user) {
    Set<String> roleNames =
        user.getRoles() != null
            ? user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
            : Set.of();
    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.isEnabled(),
        roleNames,
        user.getCreatedAt());
  }
}
