package com.commerceos.iam.exception;

/**
 * Base exception for all business-rule violations in the system.
 *
 * <p>Each exception carries an error code (machine-readable, e.g. {@code SELF_APPROVAL}) and an
 * HTTP status that the {@link GlobalExceptionHandler} uses to build RFC 7807 {@code ProblemDetail}
 * responses.
 */
public class BusinessException extends RuntimeException {

  private final String errorCode;
  private final int status;

  public BusinessException(String errorCode, String message, int status) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
  }

  public BusinessException(String errorCode, String message) {
    this(errorCode, message, 400);
  }

  /** Machine-readable error code suitable for client-side logic. */
  public String getErrorCode() {
    return errorCode;
  }

  /** HTTP status code to return to the client. */
  public int getStatus() {
    return status;
  }
}
