package com.commerceos.platform.exception;

public class InsufficientStockException extends BusinessException {

  public InsufficientStockException(String message) {
    super("INSUFFICIENT_STOCK", message, 400);
  }
}
