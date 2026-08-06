package com.commerceos.platform.exception;

public class ResourceNotFoundException extends BusinessException {

  public ResourceNotFoundException(String message) {
    super("RESOURCE_NOT_FOUND", message, 404);
  }

  public ResourceNotFoundException(String errorCode, String message) {
    super(errorCode, message, 404);
  }
}
