package com.ces.eos.service.impl;

import com.ces.eos.dto.google.GoogleUserInfo;
import com.ces.eos.dto.request.GoogleAuthRequest;
import com.ces.eos.dto.token.RefreshTokenInfo;
import com.ces.eos.dto.token.TokenPair;
import com.ces.eos.entity.User;
import com.ces.eos.exception.AuthException;
import com.ces.eos.repository.InvalidatedAccessTokenRepository;
import com.ces.eos.repository.RefreshTokenRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.AuthService;
import com.ces.eos.service.GoogleOAuthService;
import com.ces.eos.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final GoogleOAuthService googleOAuthService;

  private final JwtUtil jwtUtil;

  private final RefreshTokenRepository refreshTokenRepository;
  private final InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public TokenPair loginWithGoogle(GoogleAuthRequest request) {
    log.info("action=loginWithGoogle.start");
    log.debug("action=loginWithGoogle.service.exchangeCodeForUserInfo");
    GoogleUserInfo googleUserInfo =
        googleOAuthService.exchangeCodeForUserInfo(request.code(), request.redirectUri());

    log.debug("action=loginWithGoogle.repo.findByEmail");
    User user =
        userRepository
            .findByEmail(googleUserInfo.email())
            .orElseThrow(
                () -> {
                  log.warn("action=loginWithGoogle.validationFailed reason=userNotRegistered");
                  return AuthException.unauthorized("User is not registered in the system");
                });

    validateUserActive(user);

    TokenPair tokenPair = generateTokens(user);
    log.info("action=loginWithGoogle.success userId={}", user.getId());
    return tokenPair;
  }

  @Override
  @Transactional
  public TokenPair refreshToken(String refreshToken) {
    log.info("action=refreshToken.start");
    Claims claims = extractRefreshTokenClaims(refreshToken);
    User user = getUserFromClaims(claims);
    validateUserActive(user);
    revokeRefreshToken(claims);
    TokenPair tokenPair = generateTokens(user);
    log.info("action=refreshToken.success userId={}", user.getId());
    return tokenPair;
  }

  @Override
  @Transactional
  public void logout(String accessToken, String refreshToken) {
    log.info("action=logout.start");
    if (accessToken != null && !accessToken.isBlank()) {
      try {
        Claims accessTokenClaims = extractAccessTokenClaims(accessToken);
        invalidateAccessToken(accessTokenClaims);
      } catch (AuthException e) {
        log.debug("action=logout.branch.accessTokenSkip reason={}", e.getMessage());
      }
    }
    if (refreshToken != null && !refreshToken.isBlank()) {
      try {
        Claims refreshTokenClaims = extractRefreshTokenClaims(refreshToken);
        revokeRefreshToken(refreshTokenClaims);
      } catch (AuthException e) {
        log.debug("action=logout.branch.refreshTokenSkip reason={}", e.getMessage());
      }
    }
    log.info("action=logout.success");
  }

  private User getUserFromClaims(Claims claims) {
    UUID userId = UUID.fromString(claims.getSubject());
    return userRepository
        .findById(userId)
        .orElseThrow(
            () -> {
              log.warn("User not found for token - userId: {}", userId);
              return AuthException.invalidToken();
            });
  }

  private void invalidateAccessToken(Claims claims) {
    String jti = claims.getId();
    Instant expiresAt = claims.getExpiration().toInstant();
    int inserted = invalidatedAccessTokenRepository.insertIgnoreJti(jti, expiresAt);
    if (inserted == 0) {
      log.warn("Access token already used or revoked - jti: {}", jwtUtil.getTruncatedJti(jti));
    }
  }

  private void revokeRefreshToken(Claims claims) {
    String jti = claims.getId();
    int deleted = refreshTokenRepository.deleteByJti(jti);
    if (deleted == 0) {
      log.warn("Refresh token already used or revoked - jti: {}", jwtUtil.getTruncatedJti(jti));
      throw AuthException.invalidToken("Refresh token is invalid");
    }
  }

  private Claims extractClaims(String token, Function<String, Claims> parser, String tokenType) {
    try {
      return parser.apply(token);
    } catch (ExpiredJwtException e) {
      log.debug("{} token expired", tokenType);
      throw AuthException.invalidToken();
    } catch (JwtException e) {
      log.warn("Invalid JWT {} token received", tokenType);
      throw AuthException.invalidToken();
    }
  }

  private Claims extractAccessTokenClaims(String token) {
    return extractClaims(token, jwtUtil::parseAndValidateAccessToken, "Access");
  }

  private Claims extractRefreshTokenClaims(String token) {
    return extractClaims(token, jwtUtil::parseAndValidateRefreshToken, "Refresh");
  }

  private TokenPair generateTokens(User user) {
    Instant now = Instant.now();
    String accessToken = jwtUtil.generateAccessToken(user, now);
    RefreshTokenInfo refreshToken = jwtUtil.generateRefreshToken(user, now);
    refreshTokenRepository.save(refreshToken.toEntity(user));
    return new TokenPair(accessToken, refreshToken.token());
  }

  private void validateUserActive(User user) {
    if (!user.getIsActive()) {
      log.warn("Login attempt with inactive account - userId: {}", user.getId());
      throw AuthException.userInactive();
    }
  }
}
