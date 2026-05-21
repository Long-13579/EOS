package com.ces.eos.security;

import com.ces.eos.constant.RequestAttributeKeys;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.AuthException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final HandlerExceptionResolver resolver;

  public CustomAuthenticationEntryPoint(
      @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
    this.resolver = resolver;
  }

  @Override
  public void commence(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {

    ErrorCode errorCode = (ErrorCode) request.getAttribute(RequestAttributeKeys.JWT_ERROR);
    Exception exceptionToResolve = createExceptionFromErrorCode(errorCode, authException);

    // Delegate to GlobalExceptionHandler for consistent error response format
    resolver.resolveException(request, response, null, exceptionToResolve);
  }

  private Exception createExceptionFromErrorCode(
      ErrorCode errorCode, AuthenticationException fallback) {
    if (errorCode == null) {
      return fallback;
    }

    return switch (errorCode) {
      case TOKEN_EXPIRED -> AuthException.tokenExpired("Access token has expired");
      case INVALID_TOKEN -> AuthException.invalidToken("Access token is invalid");
      case USER_INACTIVE -> AuthException.userInactive();
      default -> fallback;
    };
  }
}
