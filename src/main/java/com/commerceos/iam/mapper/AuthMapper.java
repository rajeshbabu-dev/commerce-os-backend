package com.commerceos.iam.mapper;

import com.commerceos.iam.dto.response.AuthResponseDto;
import com.commerceos.iam.dto.response.UserResponseDto;
import com.commerceos.iam.entity.Role;
import com.commerceos.iam.entity.User;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

  public UserResponseDto toUserResponse(User user) {
    if (user == null) return null;
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

  public List<UserResponseDto> toUserResponseList(List<User> users) {
    if (users == null) return List.of();
    return users.stream().map(this::toUserResponse).toList();
  }

  public AuthResponseDto toAuthResponse(
      String accessToken, String refreshToken, long expiresInSeconds, Set<String> roles) {
    return AuthResponseDto.of(accessToken, refreshToken, expiresInSeconds, roles);
  }
}
