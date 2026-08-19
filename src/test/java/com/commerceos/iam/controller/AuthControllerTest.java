package com.commerceos.iam.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.commerceos.iam.dto.request.SignupRequestDto;
import com.commerceos.iam.entity.User;
import com.commerceos.iam.service.AuthService;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @InjectMocks private AuthController authController;

  @Test
  @DisplayName("signUp endpoint delegates to authService and returns 201 Created")
  void signUp_Success() {
    SignupRequestDto req = new SignupRequestDto("john_doe", "john@example.com", "Password123");
    User savedUser =
        User.builder()
            .id(UUID.randomUUID())
            .username("john_doe")
            .email("john@example.com")
            .roles(Set.of())
            .build();

    when(authService.signUp(eq(req), any())).thenReturn(savedUser);

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRemoteAddr("127.0.0.1");

    ResponseEntity<?> response = authController.signUp(req, request);

    org.junit.jupiter.api.Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
  }
}
