package com.ces.eos.util;

import com.ces.eos.config.properties.JwtProperties;
import com.ces.eos.dto.token.RefreshTokenInfo;
import com.ces.eos.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

  private static final int JTI_LOG_PREFIX_LENGTH = 8;
  private static final String TOKEN_TYPE_CLAIM = "token_type";
  private static final String TOKEN_TYPE_ACCESS = "access";
  private static final String TOKEN_TYPE_REFRESH = "refresh";

  private final JwtProperties jwtProperties;
  private final SecretKey signingKey;

  public JwtUtil(JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
    this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user, Instant now) {
    Instant expiryDate = now.plusMillis(jwtProperties.accessTokenExpiration());
    String jti = UUID.randomUUID().toString();

    return Jwts.builder()
        .id(jti)
        .subject(user.getId().toString())
        .claim("role", user.getRole().getName())
        .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiryDate))
        .signWith(signingKey)
        .compact();
  }

  public RefreshTokenInfo generateRefreshToken(User user, Instant now) {
    Instant expiryDate = now.plusMillis(jwtProperties.refreshTokenExpiration());
    String jti = UUID.randomUUID().toString();

    String token =
        Jwts.builder()
            .id(jti)
            .subject(user.getId().toString())
            .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiryDate))
            .signWith(signingKey)
            .compact();

    return new RefreshTokenInfo(token, jti, expiryDate); // RToken stores jti and expiry for DB
  }

  public Claims parseAndValidateAccessToken(String token) {
    return parseAndValidateClaims(token, TOKEN_TYPE_ACCESS);
  }

  public Claims parseAndValidateRefreshToken(String token) {
    return parseAndValidateClaims(token, TOKEN_TYPE_REFRESH);
  }

  private Claims parseAndValidateClaims(String token, String expectedTokenType) {
    return Jwts.parser()
        .verifyWith(signingKey)
        .require(TOKEN_TYPE_CLAIM, expectedTokenType)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public String getTruncatedJti(String jti) {
    int prefixLength = Math.min(JTI_LOG_PREFIX_LENGTH, jti.length());
    return jti.substring(0, prefixLength) + "...";
  }
}
