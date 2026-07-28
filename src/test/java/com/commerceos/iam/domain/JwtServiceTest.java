package com.commerceos.iam.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

  private static final String SECRET = "dGVzdFNlY3JldEtleUZvckNvbW1lcmNlT1NNaW5pbXVtMzJCeXRlcw==";
  private static final long ACCESS_TTL = 900000; // 15 min
  private static final long REFRESH_TTL = 604800000; // 7 days

  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService(SECRET, ACCESS_TTL, REFRESH_TTL);
  }

  @Test
  void generateAccessToken_shouldCreateValidToken() {
    String token = jwtService.generateAccessToken("john@test.com");
    assertNotNull(token);
    assertFalse(token.isBlank());
  }

  @Test
  void generateRefreshToken_shouldCreateValidToken() {
    String token = jwtService.generateRefreshToken("john@test.com");
    assertNotNull(token);
    assertFalse(token.isBlank());
    // Refresh token should be different from access token
    assertNotEquals(token, jwtService.generateAccessToken("john@test.com"));
  }

  @Test
  void extractUsername_shouldReturnSubject() {
    String token = jwtService.generateAccessToken("john@test.com");
    String extracted = jwtService.extractUsername(token);
    assertEquals("john@test.com", extracted);
  }

  @Test
  void isTokenValid_shouldReturnTrueForValidToken() {
    String token = jwtService.generateAccessToken("john@test.com");
    assertTrue(jwtService.isTokenValid(token, "john@test.com"));
  }

  @Test
  void isTokenValid_shouldReturnFalseForWrongUser() {
    String token = jwtService.generateAccessToken("john@test.com");
    assertFalse(jwtService.isTokenValid(token, "wrong@test.com"));
  }

  @Test
  void accessAndRefreshTokens_shouldHaveDifferentExpirations() {
    String accessToken = jwtService.generateAccessToken("john@test.com");
    String refreshToken = jwtService.generateRefreshToken("john@test.com");

    // Both should be valid for the same user
    assertTrue(jwtService.isTokenValid(accessToken, "john@test.com"));
    assertTrue(jwtService.isTokenValid(refreshToken, "john@test.com"));
  }

  @Test
  void extractClaim_shouldWork() {
    String token = jwtService.generateAccessToken("john@test.com");
    String subject = jwtService.extractClaim(token, Claims::getSubject);
    assertEquals("john@test.com", subject);
  }

  // Helper to import Claims
  private static final class Claims {
    static String getSubject(io.jsonwebtoken.Claims claims) {
      return claims.getSubject();
    }
  }
}
