package com.ces.eos.service.impl;

import com.ces.eos.config.properties.GoogleOAuthProperties;
import com.ces.eos.constant.GoogleOAuthConstants;
import com.ces.eos.dto.google.GoogleUserInfo;
import com.ces.eos.exception.AuthException;
import com.ces.eos.service.GoogleOAuthService;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleOAuthServiceImpl implements GoogleOAuthService {

  private final GoogleOAuthProperties googleOAuthProperties;
  private final HttpTransport httpTransport;
  private final JsonFactory jsonFactory;
  private final GoogleIdTokenVerifier googleIdTokenVerifier;

  @Override
  public GoogleUserInfo exchangeCodeForUserInfo(String code, String redirectUri) {
    log.info("action=exchangeCodeForUserInfo.start");

    if (code == null || code.isBlank()) {
      log.warn("action=exchangeCodeForUserInfo.validationFailed reason=missingCode");
      throw AuthException.oauthError("Authorization code is required");
    }

    if (redirectUri == null || redirectUri.isBlank()) {
      log.warn("action=exchangeCodeForUserInfo.validationFailed reason=missingRedirectUri");
      throw AuthException.oauthError("Redirect URI is required");
    }

    log.debug("action=exchangeCodeForUserInfo.call.exchangeCodeForTokens");
    GoogleTokenResponse tokenResponse = exchangeCodeForTokens(code, redirectUri);
    log.debug("action=exchangeCodeForUserInfo.call.extractAndVerifyIdToken");
    GoogleIdToken idToken = extractAndVerifyIdToken(tokenResponse);
    GoogleUserInfo userInfo = extractUserInfo(idToken.getPayload());
    log.info("action=exchangeCodeForUserInfo.success");
    return userInfo;
  }

  private GoogleTokenResponse exchangeCodeForTokens(String code, String redirectUri) {
    try {
      log.debug("action=exchangeCodeForTokens.call");
      return new GoogleAuthorizationCodeTokenRequest(
              httpTransport,
              jsonFactory,
              GoogleOAuthConstants.TOKEN_SERVER_URL,
              googleOAuthProperties.clientId(),
              googleOAuthProperties.clientSecret(),
              code,
              redirectUri)
          .execute();
    } catch (TokenResponseException e) {
      if (e.getDetails() != null && "invalid_grant".equals(e.getDetails().getError())) {
        log.warn("action=exchangeCodeForTokens.validationFailed reason=invalidGrant");
        throw AuthException.oauthError("Authorization code is invalid or expired");
      }
      log.error("action=exchangeCodeForTokens.error", e);
      throw AuthException.oauthError("Failed to exchange code for token");
    } catch (IOException e) {
      log.error("action=exchangeCodeForTokens.error", e);
      throw AuthException.oauthError("Failed to exchange code for token");
    }
  }

  private GoogleIdToken extractAndVerifyIdToken(GoogleTokenResponse tokenResponse) {
    try {
      String rawIdToken = tokenResponse.getIdToken();

      if (rawIdToken == null || rawIdToken.isBlank()) {
        log.warn("action=extractAndVerifyIdToken.validationFailed reason=missingIdToken");
        throw AuthException.oauthError("Missing ID token in response");
      }

      GoogleIdToken idToken = tokenResponse.parseIdToken();

      if (idToken == null) {
        log.warn("action=extractAndVerifyIdToken.validationFailed reason=missingIdToken");
        throw AuthException.oauthError("Missing ID token in response");
      }

      if (!googleIdTokenVerifier.verify(idToken)) {
        log.warn("action=extractAndVerifyIdToken.validationFailed reason=invalidOrExpiredIdToken");
        throw AuthException.oauthError("Invalid or expired ID token");
      }

      return idToken;
    } catch (IOException | GeneralSecurityException e) {
      log.error("action=extractAndVerifyIdToken.error", e);
      throw AuthException.oauthError("Failed to verify ID token");
    }
  }

  private GoogleUserInfo extractUserInfo(GoogleIdToken.Payload payload) {
    String email = payload.getEmail();
    String name = (String) payload.get("name");
    Boolean emailVerified = payload.getEmailVerified();

    if (email == null || name == null || emailVerified == null) {
      log.error("action=extractUserInfo.validationFailed reason=incompleteUserInfo");
      throw AuthException.oauthError("Incomplete user information");
    }

    return new GoogleUserInfo(email, name, emailVerified);
  }
}
