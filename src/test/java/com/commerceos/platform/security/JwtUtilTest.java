package com.commerceos.platform.security;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.entity.User;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtUtilTest {

  // 256-bit base64 secret
  private static final String SECRET =
      "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
  private JwtUtil jwtUtil;
  private User user;

  @BeforeEach
  void setUp() {
    jwtUtil = new JwtUtil(SECRET, 900000, 604800000);
    // Note: username is a display name only; UserDetails.getUsername() returns email
    user =
        User.builder()
            .id(UUID.randomUUID())
            .username("testuser")
            .email("test@example.com")
            .passwordHash("pass")
            .build();
  }

  @Test
  @DisplayName("Generate access token and extract subject")
  void generateAccessTokenAndExtractSubject() {
    String token = jwtUtil.generateAccessToken(user);
    assertNotNull(token);
    assertEquals("test@example.com", jwtUtil.extractUsername(token));
  }

  @Test
  @DisplayName("Validate token with valid user")
  void validateToken() {
    String token = jwtUtil.generateAccessToken(user);
    assertTrue(jwtUtil.isTokenValid(token, user));
  }
}
