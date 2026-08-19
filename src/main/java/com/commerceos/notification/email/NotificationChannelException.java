package com.commerceos.notification.email;

/** Thrown when an outbound notification channel (e.g., SMTP) fails. */
public class NotificationChannelException extends RuntimeException {

  public NotificationChannelException(String message, Throwable cause) {
    super(message, cause);
  }
}
