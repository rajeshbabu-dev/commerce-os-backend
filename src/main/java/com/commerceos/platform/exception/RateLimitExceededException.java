package com.commerceos.platform.exception;

public class RateLimitExceededException extends BusinessException {

  public RateLimitExceededException(String errorCode, String message) {
    super(errorCode, message, 429);
  }

  public RateLimitExceededException(String message) {
    super("TOO_MANY_REQUESTS", message, 429);
  }
}
