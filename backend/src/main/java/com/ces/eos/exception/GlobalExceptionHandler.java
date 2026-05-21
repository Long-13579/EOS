package com.ces.eos.exception;

import com.ces.eos.dto.response.ErrorResponse;
import com.ces.eos.enums.ErrorCode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
  public ResponseEntity<ErrorResponse> handleValidationException(BindException ex) {
    Map<String, List<String>> details =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.groupingBy(
                    FieldError::getField,
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));

    ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
    return buildErrorResponse(errorCode, null, details);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
    log.debug("Authentication failed: {}", ex.getMessage());
    return buildErrorResponse(ErrorCode.UNAUTHORIZED, null, null);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
    log.warn("Access denied: {}", ex.getMessage());
    return buildErrorResponse(ErrorCode.FORBIDDEN, null, null);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ErrorResponse> handleMissingParamException(
      MissingServletRequestParameterException ex) {

    Map<String, List<String>> details = new HashMap<>();
    details.put(ex.getParameterName(), List.of(ex.getMessage()));

    return buildErrorResponse(ErrorCode.VALIDATION_FAILED, null, details);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException ex) {

    String requiredType =
        ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown";
    String message = String.format("Invalid type. Expected: %s", requiredType);

    Map<String, List<String>> details = new HashMap<>();
    details.put(ex.getName(), List.of(message));

    return buildErrorResponse(ErrorCode.VALIDATION_FAILED, null, details);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
    log.debug("Invalid argument: {}", ex.getMessage());
    return buildErrorResponse(ErrorCode.BAD_REQUEST, ex.getMessage(), null);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
    return buildErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getDetails());
  }

  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException ex) {
    log.error("Application state/configuration failure: {}", ex.getMessage());
    return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex) {
    log.error("Unexpected error: {}", ex);
    return buildErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR, null, null);
  }

  private ResponseEntity<ErrorResponse> buildErrorResponse(
      ErrorCode errorCode, String customMessage, Map<String, List<String>> details) {

    String message = customMessage != null ? customMessage : errorCode.getMessage();

    ErrorResponse response = new ErrorResponse(errorCode.name(), message, details);

    return ResponseEntity.status(errorCode.getStatus()).body(response);
  }
}
