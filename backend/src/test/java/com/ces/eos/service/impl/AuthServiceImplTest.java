package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ces.eos.dto.google.GoogleUserInfo;
import com.ces.eos.dto.request.GoogleAuthRequest;
import com.ces.eos.dto.token.RefreshTokenInfo;
import com.ces.eos.dto.token.TokenPair;
import com.ces.eos.entity.Role;
import com.ces.eos.entity.User;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.enums.UserRole;
import com.ces.eos.exception.AuthException;
import com.ces.eos.repository.InvalidatedAccessTokenRepository;
import com.ces.eos.repository.RefreshTokenRepository;
import com.ces.eos.repository.UserRepository;
import com.ces.eos.service.GoogleOAuthService;
import com.ces.eos.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

  @Mock private GoogleOAuthService googleOAuthService;

  @Mock private JwtUtil jwtUtil;

  @Mock private RefreshTokenRepository refreshTokenRepository;

  @Mock private InvalidatedAccessTokenRepository invalidatedAccessTokenRepository;

  @Mock private UserRepository userRepository;

  @InjectMocks private AuthServiceImpl authService;

  private User activeUser;
  private User inactiveUser;

  @BeforeEach
  void setUp() {
    activeUser = buildUser(true);
    inactiveUser = buildUser(false);
  }

  @Test
  void loginWithGoogle_success_returnsTokenPair_andSavesRefreshToken() {
    GoogleAuthRequest request = new GoogleAuthRequest("code", "http://localhost/callback");
    GoogleUserInfo googleUserInfo = new GoogleUserInfo(userEmail(activeUser), "Test User", true);
    RefreshTokenInfo refreshTokenInfo =
        new RefreshTokenInfo("new-refresh-token", "new-jti", Instant.now().plusSeconds(3600));

    when(googleOAuthService.exchangeCodeForUserInfo(request.code(), request.redirectUri()))
        .thenReturn(googleUserInfo);
    when(userRepository.findByEmail(userEmail(activeUser))).thenReturn(Optional.of(activeUser));
    when(jwtUtil.generateAccessToken(eq(activeUser), any(Instant.class)))
        .thenReturn("new-access-token");
    when(jwtUtil.generateRefreshToken(eq(activeUser), any(Instant.class)))
        .thenReturn(refreshTokenInfo);

    TokenPair result = authService.loginWithGoogle(request);

    assertThat(result.accessToken()).isEqualTo("new-access-token");
    assertThat(result.refreshToken()).isEqualTo("new-refresh-token");

    ArgumentCaptor<com.ces.eos.entity.RefreshToken> savedToken =
        ArgumentCaptor.forClass(com.ces.eos.entity.RefreshToken.class);
    verify(refreshTokenRepository).save(savedToken.capture());
    assertThat(savedToken.getValue().getUser()).isEqualTo(activeUser);
    assertThat(savedToken.getValue().getJti()).isEqualTo("new-jti");
  }

  @Test
  void loginWithGoogle_userNotRegistered_throwsUnauthorized() {
    GoogleAuthRequest request = new GoogleAuthRequest("code", "http://localhost/callback");
    GoogleUserInfo googleUserInfo = new GoogleUserInfo("not-found@example.com", "Unknown", true);

    when(googleOAuthService.exchangeCodeForUserInfo(request.code(), request.redirectUri()))
        .thenReturn(googleUserInfo);
    when(userRepository.findByEmail("not-found@example.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.loginWithGoogle(request))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.UNAUTHORIZED);

    verify(refreshTokenRepository, never()).save(any());
  }

  @Test
  void loginWithGoogle_userInactive_throwsUserInactive() {
    GoogleAuthRequest request = new GoogleAuthRequest("code", "http://localhost/callback");
    GoogleUserInfo googleUserInfo =
        new GoogleUserInfo(userEmail(inactiveUser), "Inactive User", true);

    when(googleOAuthService.exchangeCodeForUserInfo(request.code(), request.redirectUri()))
        .thenReturn(googleUserInfo);
    when(userRepository.findByEmail(userEmail(inactiveUser))).thenReturn(Optional.of(inactiveUser));

    assertThatThrownBy(() -> authService.loginWithGoogle(request))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.USER_INACTIVE);
  }

  @Test
  void loginWithGoogle_googleOAuthFails_propagatesException() {
    GoogleAuthRequest request = new GoogleAuthRequest("code", "http://localhost/callback");

    when(googleOAuthService.exchangeCodeForUserInfo(request.code(), request.redirectUri()))
        .thenThrow(new RuntimeException("google oauth down"));

    assertThatThrownBy(() -> authService.loginWithGoogle(request))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("google oauth down");
  }

  @Test
  void loginWithGoogle_nullRequest_throwsNullPointerException() {
    assertThatThrownBy(() -> authService.loginWithGoogle(null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  void refreshToken_success_rotatesToken_andReturnsNewPair() {
    Claims claims =
        buildClaims(
            userId(activeUser).toString(), "old-jti", Date.from(Instant.now().plusSeconds(1800)));
    RefreshTokenInfo refreshTokenInfo =
        new RefreshTokenInfo("new-refresh-token", "new-jti", Instant.now().plusSeconds(7200));

    when(jwtUtil.parseAndValidateRefreshToken("old-refresh-token")).thenReturn(claims);
    when(userRepository.findById(userId(activeUser))).thenReturn(Optional.of(activeUser));
    when(refreshTokenRepository.deleteByJti("old-jti")).thenReturn(1);
    when(jwtUtil.generateAccessToken(eq(activeUser), any(Instant.class)))
        .thenReturn("new-access-token");
    when(jwtUtil.generateRefreshToken(eq(activeUser), any(Instant.class)))
        .thenReturn(refreshTokenInfo);

    TokenPair result = authService.refreshToken("old-refresh-token");

    assertThat(result.accessToken()).isEqualTo("new-access-token");
    assertThat(result.refreshToken()).isEqualTo("new-refresh-token");
    verify(refreshTokenRepository).deleteByJti("old-jti");
    verify(refreshTokenRepository).save(any(com.ces.eos.entity.RefreshToken.class));
  }

  @Test
  void refreshToken_expiredJwt_throwsInvalidToken() {
    when(jwtUtil.parseAndValidateRefreshToken("expired-token"))
        .thenThrow(new ExpiredJwtException(null, null, "expired"));

    assertThatThrownBy(() -> authService.refreshToken("expired-token"))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  void refreshToken_malformedJwt_throwsInvalidToken() {
    when(jwtUtil.parseAndValidateRefreshToken("bad-token"))
        .thenThrow(new JwtException("bad token"));

    assertThatThrownBy(() -> authService.refreshToken("bad-token"))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  void refreshToken_userNotFoundFromSubject_throwsInvalidToken() {
    Claims claims =
        buildClaims(
            UUID.randomUUID().toString(), "old-jti", Date.from(Instant.now().plusSeconds(1800)));

    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(claims);
    when(userRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  void refreshToken_userInactive_throwsUserInactive() {
    Claims claims =
        buildClaims(
            userId(inactiveUser).toString(), "old-jti", Date.from(Instant.now().plusSeconds(1800)));

    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(claims);
    when(userRepository.findById(userId(inactiveUser))).thenReturn(Optional.of(inactiveUser));

    assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.USER_INACTIVE);
  }

  @Test
  void refreshToken_refreshTokenAlreadyRevoked_throwsInvalidToken() {
    Claims claims =
        buildClaims(
            userId(activeUser).toString(),
            "revoked-jti",
            Date.from(Instant.now().plusSeconds(1800)));

    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(claims);
    when(userRepository.findById(userId(activeUser))).thenReturn(Optional.of(activeUser));
    when(refreshTokenRepository.deleteByJti("revoked-jti")).thenReturn(0);

    assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
        .isInstanceOf(AuthException.class)
        .extracting(ex -> ((AuthException) ex).getErrorCode())
        .isEqualTo(ErrorCode.INVALID_TOKEN);
  }

  @Test
  void refreshToken_subjectNotUuid_throwsIllegalArgumentException() {
    Claims claims =
        buildClaims("not-a-uuid", "old-jti", Date.from(Instant.now().plusSeconds(1800)));
    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(claims);

    assertThatThrownBy(() -> authService.refreshToken("refresh-token"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void logout_bothTokensValid_revokesBoth_withoutThrowing() {
    Claims accessClaims =
        buildClaims(
            userId(activeUser).toString(),
            "access-jti",
            Date.from(Instant.now().plusSeconds(1800)));
    Claims refreshClaims =
        buildClaims(
            userId(activeUser).toString(),
            "refresh-jti",
            Date.from(Instant.now().plusSeconds(7200)));

    when(jwtUtil.parseAndValidateAccessToken("access-token")).thenReturn(accessClaims);
    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(refreshClaims);
    when(invalidatedAccessTokenRepository.insertIgnoreJti(
            "access-jti", accessClaims.getExpiration().toInstant()))
        .thenReturn(1);
    when(refreshTokenRepository.deleteByJti("refresh-jti")).thenReturn(1);

    authService.logout("access-token", "refresh-token");

    verify(invalidatedAccessTokenRepository)
        .insertIgnoreJti("access-jti", accessClaims.getExpiration().toInstant());
    verify(refreshTokenRepository).deleteByJti("refresh-jti");
  }

  @Test
  void logout_onlyAccessTokenProvided_revokesAccessOnly() {
    Claims accessClaims =
        buildClaims(
            userId(activeUser).toString(),
            "access-jti",
            Date.from(Instant.now().plusSeconds(1800)));
    when(jwtUtil.parseAndValidateAccessToken("access-token")).thenReturn(accessClaims);
    when(invalidatedAccessTokenRepository.insertIgnoreJti(
            "access-jti", accessClaims.getExpiration().toInstant()))
        .thenReturn(1);

    authService.logout("access-token", " ");

    verify(invalidatedAccessTokenRepository)
        .insertIgnoreJti("access-jti", accessClaims.getExpiration().toInstant());
    verify(refreshTokenRepository, never()).deleteByJti(anyString());
  }

  @Test
  void logout_onlyRefreshTokenProvided_revokesRefreshOnly() {
    Claims refreshClaims =
        buildClaims(
            userId(activeUser).toString(),
            "refresh-jti",
            Date.from(Instant.now().plusSeconds(7200)));
    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(refreshClaims);
    when(refreshTokenRepository.deleteByJti("refresh-jti")).thenReturn(1);

    authService.logout(" ", "refresh-token");

    verify(invalidatedAccessTokenRepository, never())
        .insertIgnoreJti(anyString(), any(Instant.class));
    verify(refreshTokenRepository).deleteByJti("refresh-jti");
  }

  @Test
  void logout_bothTokensMissing_noOp_noException() {
    authService.logout(null, " ");

    verifyNoInteractions(jwtUtil, invalidatedAccessTokenRepository, refreshTokenRepository);
  }

  @Test
  void logout_invalidAccessToken_isSwallowed_andRefreshStillProcessed() {
    Claims refreshClaims =
        buildClaims(
            userId(activeUser).toString(),
            "refresh-jti",
            Date.from(Instant.now().plusSeconds(7200)));

    when(jwtUtil.parseAndValidateAccessToken("bad-access-token"))
        .thenThrow(AuthException.invalidToken());
    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(refreshClaims);
    when(refreshTokenRepository.deleteByJti("refresh-jti")).thenReturn(1);

    authService.logout("bad-access-token", "refresh-token");

    verify(refreshTokenRepository).deleteByJti("refresh-jti");
  }

  @Test
  void logout_invalidRefreshToken_isSwallowed_andMethodStillSucceeds() {
    Claims accessClaims =
        buildClaims(
            userId(activeUser).toString(),
            "access-jti",
            Date.from(Instant.now().plusSeconds(1800)));

    when(jwtUtil.parseAndValidateAccessToken("access-token")).thenReturn(accessClaims);
    when(invalidatedAccessTokenRepository.insertIgnoreJti(
            "access-jti", accessClaims.getExpiration().toInstant()))
        .thenReturn(1);
    when(jwtUtil.parseAndValidateRefreshToken("bad-refresh-token"))
        .thenThrow(AuthException.invalidToken());

    authService.logout("access-token", "bad-refresh-token");

    verify(invalidatedAccessTokenRepository)
        .insertIgnoreJti("access-jti", accessClaims.getExpiration().toInstant());
  }

  @Test
  void logout_accessTokenAlreadyInvalidated_insertReturnsZero_stillSucceeds() {
    Claims accessClaims =
        buildClaims(
            userId(activeUser).toString(),
            "access-jti",
            Date.from(Instant.now().plusSeconds(1800)));
    when(jwtUtil.parseAndValidateAccessToken("access-token")).thenReturn(accessClaims);
    when(invalidatedAccessTokenRepository.insertIgnoreJti(
            "access-jti", accessClaims.getExpiration().toInstant()))
        .thenReturn(0);

    authService.logout("access-token", null);

    verify(invalidatedAccessTokenRepository)
        .insertIgnoreJti("access-jti", accessClaims.getExpiration().toInstant());
  }

  @Test
  void logout_refreshTokenAlreadyRevoked_deleteReturnsZero_isSwallowed() {
    Claims refreshClaims =
        buildClaims(
            userId(activeUser).toString(),
            "refresh-jti",
            Date.from(Instant.now().plusSeconds(7200)));
    when(jwtUtil.parseAndValidateRefreshToken("refresh-token")).thenReturn(refreshClaims);
    when(refreshTokenRepository.deleteByJti("refresh-jti")).thenReturn(0);

    authService.logout(null, "refresh-token");

    verify(refreshTokenRepository).deleteByJti("refresh-jti");
  }

  @Test
  void logout_unexpectedRepositoryRuntimeException_propagates() {
    Claims accessClaims =
        buildClaims(
            userId(activeUser).toString(),
            "access-jti",
            Date.from(Instant.now().plusSeconds(1800)));
    when(jwtUtil.parseAndValidateAccessToken("access-token")).thenReturn(accessClaims);
    when(invalidatedAccessTokenRepository.insertIgnoreJti(
            "access-jti", accessClaims.getExpiration().toInstant()))
        .thenThrow(new RuntimeException("db down"));

    assertThatThrownBy(() -> authService.logout("access-token", null))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("db down");
  }

  private static User buildUser(boolean isActive) {
    Role role = Role.builder().name(UserRole.USER).build();
    return User.builder()
        .id(UUID.randomUUID())
        .firstName("Test")
        .lastName("User")
        .email(isActive ? "active@example.com" : "inactive@example.com")
        .role(role)
        .isActive(isActive)
        .build();
  }

  private static UUID userId(User user) {
    return user.getId();
  }

  private static String userEmail(User user) {
    return user.getEmail();
  }

  private static Claims buildClaims(String subject, String jti, Date expiration) {
    Claims claims = org.mockito.Mockito.mock(Claims.class);
    lenient().when(claims.getSubject()).thenReturn(subject);
    lenient().when(claims.getId()).thenReturn(jti);
    lenient().when(claims.getExpiration()).thenReturn(expiration);
    return claims;
  }
}
