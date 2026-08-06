package com.commerceos.iam.service;

import com.commerceos.iam.dto.request.CreateUserRequestDto;
import com.commerceos.iam.dto.request.LoginRequestDto;
import com.commerceos.iam.dto.request.SignupRequestDto;
import com.commerceos.iam.dto.response.AuthResponseDto;
import com.commerceos.iam.entity.User;
import java.util.UUID;

public interface AuthService {

  User createUser(CreateUserRequestDto request);

  User signUp(SignupRequestDto request, String clientIp);

  AuthResponseDto login(LoginRequestDto request);

  AuthResponseDto refreshToken(String refreshToken);

  void logout(String refreshToken);

  void logoutAll(UUID userId);
}
