package com.ces.eos.security;

import com.ces.eos.constant.RequestAttributeKeys;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.repository.InvalidatedAccessTokenRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.util.CookieUtil;
import com.ces.eos.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final CookieUtil cookieUtil;
  private final UserRepository userRepository;
  private final InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    cookieUtil
        .extractAccessToken(request)
        .flatMap(token -> validateAndGetClaims(token, request))
        .filter(claims -> isTokenNotRevoked(claims.getId(), request))
        .flatMap(claims -> getActiveUser(claims.getSubject(), request))
        .ifPresent(user -> setAuthentication(request, user));

    filterChain.doFilter(request, response);
  }

  private Optional<Claims> validateAndGetClaims(String token, HttpServletRequest request) {
    try {
      return Optional.of(jwtUtil.parseAndValidateAccessToken(token));
    } catch (ExpiredJwtException e) {
      log.debug("Access token expired");
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.TOKEN_EXPIRED);
      return Optional.empty();
    } catch (JwtException e) {
      log.warn("Invalid JWT token received");
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return Optional.empty();
    }
  }

  private boolean isTokenNotRevoked(String jti, HttpServletRequest request) {
    if (invalidatedAccessTokenRepository.existsById(jti)) {
      log.warn("Token is revoked: jti: {}", jwtUtil.getTruncatedJti(jti));
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return false;
    }
    return true;
  }

  private Optional<User> getActiveUser(String userIdString, HttpServletRequest request) {
    try {
      UUID userId = UUID.fromString(userIdString);
      Optional<User> userOpt = userRepository.findActiveUser(userId);
      if (userOpt.isEmpty()) {
        log.warn("User not found or inactive for ID: {}", userIdString);
        request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.USER_INACTIVE);
      }
      return userOpt;
    } catch (IllegalArgumentException e) {
      log.warn("Invalid user ID format: {}", userIdString);
      request.setAttribute(RequestAttributeKeys.JWT_ERROR, ErrorCode.INVALID_TOKEN);
      return Optional.empty();
    }
  }

  private void setAuthentication(HttpServletRequest request, User user) {
    CustomUserDetails userDetails = CustomUserDetails.fromUser(user);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}
