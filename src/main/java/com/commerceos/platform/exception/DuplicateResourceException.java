package com.commerceos.platform.exception;

public class DuplicateResourceException extends BusinessException {

  public DuplicateResourceException(String message) {
    super("DUPLICATE_RESOURCE", message, 400);
  }

  public DuplicateResourceException(String errorCode, String message) {
    super(errorCode, message, 400);
  }
}
