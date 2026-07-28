package com.commerceos.iam.exception;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Translates every exception into an RFC 7807 {@code ProblemDetail} response. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
  private static final URI DEFAULT_TYPE = URI.create("about:blank");

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(ex.getStatus()), ex.getMessage());
    detail.setType(DEFAULT_TYPE);
    detail.setTitle(HttpStatusCode.valueOf(ex.getStatus()).toString());
    detail.setProperty("errorCode", ex.getErrorCode());
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidationError(MethodArgumentNotValidException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Validation failed for the request body");
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Validation Error");
    detail.setProperty("errorCode", "VALIDATION_ERROR");

    var fieldErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(
                e -> new FieldErrorEntry(e.getField(), e.getRejectedValue(), e.getDefaultMessage()))
            .toList();
    detail.setProperty("fieldErrors", fieldErrors);
    return detail;
  }

  private record FieldErrorEntry(String field, Object rejectedValue, String message) {}

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Bad Request");
    return detail;
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Unauthorized");
    return detail;
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthentication(AuthenticationException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Authentication failed");
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Unauthorized");
    return detail;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Forbidden");
    return detail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    log.error("Unexpected error", ex);
    ProblemDetail detail =
        ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong on our end, please try again");
    detail.setType(DEFAULT_TYPE);
    detail.setTitle("Internal Server Error");
    return detail;
  }
}
