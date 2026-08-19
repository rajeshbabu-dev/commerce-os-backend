package com.commerceos.platform.exception;

public class InvalidCredentialsException extends BusinessException {

  public InvalidCredentialsException(String message) {
    super("INVALID_CREDENTIALS", message, 401);
  }
}
