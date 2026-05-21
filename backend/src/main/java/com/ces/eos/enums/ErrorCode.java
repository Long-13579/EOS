package com.ces.eos.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
  // 400 Bad Request
  BAD_REQUEST(HttpStatus.BAD_REQUEST, "Bad request"),
  VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "Validation failed"),
  OAUTH_ERROR(HttpStatus.BAD_REQUEST, "OAuth authentication failed"),

  // 401 Unauthorized
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "Unauthorized"),
  INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "Invalid token"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "Token expired"),

  // 403 Forbidden
  FORBIDDEN(HttpStatus.FORBIDDEN, "Access forbidden"),
  USER_INACTIVE(HttpStatus.FORBIDDEN, "User account is inactive"),

  // 404 Not Found
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Resource not found"),

  // 409 Conflict
  RESOURCE_ALREADY_EXISTS(HttpStatus.CONFLICT, "Resource already exists"),
  CONFLICT(HttpStatus.CONFLICT, "Resource is in an invalid state for this operation"),

  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

  private final HttpStatus status;
  private final String message;

  ErrorCode(HttpStatus status, String message) {
    this.status = status;
    this.message = message;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }
}
