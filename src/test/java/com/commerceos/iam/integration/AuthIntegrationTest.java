package com.commerceos.iam.integration;

import static org.junit.jupiter.api.Assertions.*;

import com.commerceos.iam.dto.request.LoginRequestDto;
import com.commerceos.iam.dto.response.AuthResponseDto;
import com.commerceos.iam.service.AuthService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires local PostgreSQL and Redis containers running")
class AuthIntegrationTest {

  @Autowired private AuthService authService;

  @Test
  @DisplayName("Default admin can log in")
  void defaultAdminLogin() {
    LoginRequestDto loginRequest = new LoginRequestDto("admin@commerceos.com", "admin123");
    AuthResponseDto response = authService.login(loginRequest);

    assertNotNull(response.accessToken());
    assertNotNull(response.refreshToken());
    assertTrue(response.roles().contains("ADMIN"));
  }
}
