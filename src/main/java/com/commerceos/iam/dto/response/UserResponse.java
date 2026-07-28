package com.commerceos.iam.dto.response;

import com.commerceos.iam.entity.User;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record UserResponse(
    UUID id,
    String username,
    String email,
    Set<String> roles,
    Set<String> permissions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static UserResponse fromEntity(User user) {
    Set<String> roleNames =
        user.getRoles().stream().map(role -> role.getName()).collect(Collectors.toSet());

    Set<String> permissionNames =
        user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(permission -> permission.getName())
            .collect(Collectors.toSet());

    return new UserResponse(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        roleNames,
        permissionNames,
        user.getCreatedAt(),
        user.getUpdatedAt());
  }
}
