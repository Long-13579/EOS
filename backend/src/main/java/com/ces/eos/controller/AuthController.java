package com.ces.eos.controller;

import com.ces.eos.dto.request.GoogleAuthRequest;
import com.ces.eos.dto.response.MessageResponse;
import com.ces.eos.dto.token.TokenPair;
import com.ces.eos.exception.AuthException;
import com.ces.eos.service.AuthService;
import com.ces.eos.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final CookieUtil cookieUtil;

  @PostMapping("/google")
  public ResponseEntity<MessageResponse> loginWithGoogle(
      @Valid @RequestBody GoogleAuthRequest request, HttpServletResponse response) {
    TokenPair tokens = authService.loginWithGoogle(request);
    cookieUtil.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());

    return ResponseEntity.ok(new MessageResponse("Login successfully"));
  }

  @PostMapping("/refresh")
  public ResponseEntity<MessageResponse> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    try {
      String refreshToken =
          cookieUtil
              .extractRefreshToken(request)
              .orElseThrow(() -> AuthException.invalidToken("Refresh token is invalid"));

      TokenPair tokens = authService.refreshToken(refreshToken);
      cookieUtil.setAuthCookies(response, tokens.accessToken(), tokens.refreshToken());
      return ResponseEntity.ok(new MessageResponse("Token refreshed successfully"));

    } catch (AuthException e) {
      cookieUtil.clearAuthCookies(response);
      throw e;
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<MessageResponse> logout(
      HttpServletRequest request, HttpServletResponse response) {
    String accessToken = cookieUtil.extractAccessToken(request).orElse(null);
    String refreshToken = cookieUtil.extractRefreshToken(request).orElse(null);

    // 1. Guarantee local logout
    cookieUtil.clearAuthCookies(response);
    // 2. Try DB sync, but don't fail the request if the DB crashes
    try {
      authService.logout(accessToken, refreshToken);
    } catch (Exception e) {
      log.warn("Error during logout, but local cookies were cleared.", e);
    }
    // 3. Always tell the FE it succeeded so it can clear its state
    return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
  }
}
