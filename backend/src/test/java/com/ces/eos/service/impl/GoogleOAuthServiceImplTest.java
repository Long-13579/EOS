package com.ces.eos.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ces.eos.config.properties.GoogleOAuthProperties;
import com.ces.eos.dto.google.GoogleUserInfo;
import com.ces.eos.enums.ErrorCode;
import com.ces.eos.exception.AuthException;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceImplTest {

  private static final String CLIENT_ID = "test-client-id";
  private static final String CLIENT_SECRET = "test-client-secret";

  @Mock private GoogleOAuthProperties googleOAuthProperties;

  @Spy private StubHttpTransport httpTransport = new StubHttpTransport();

  @Spy private JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

  @Mock private GoogleIdTokenVerifier googleIdTokenVerifier;

  @InjectMocks private GoogleOAuthServiceImpl googleOAuthService;

  @Nested
  class ExchangeCodeForUserInfo {

    @Test
    void exchangeCodeForUserInfo_validCodeAndValidIdToken_returnsGoogleUserInfo() throws Exception {
      // Given
      mockClientCredentials();

      String idToken = buildIdToken("john.doe@example.com", "John Doe", true);
      httpTransport.respondWith(200, tokenResponseJson(idToken));
      when(googleIdTokenVerifier.verify(any(GoogleIdToken.class))).thenReturn(true);

      // When
      GoogleUserInfo result =
          googleOAuthService.exchangeCodeForUserInfo("valid-code", "http://localhost/callback");

      // Then
      assertThat(result.email()).isEqualTo("john.doe@example.com");
      assertThat(result.name()).isEqualTo("John Doe");
      assertThat(result.emailVerified()).isTrue();
      verify(googleIdTokenVerifier).verify(any(GoogleIdToken.class));
    }

    @Test
    void exchangeCodeForUserInfo_invalidGrant_throwsOAuthErrorWithInvalidGrantMessage() {
      // Given
      mockClientCredentials();

      httpTransport.respondWith(400, "{\"error\":\"invalid_grant\"}");

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "expired-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage())
                    .isEqualTo("Authorization code is invalid or expired");
              });

      verifyNoInteractions(googleIdTokenVerifier);
    }

    @Test
    void exchangeCodeForUserInfo_tokenExchangeIoFailure_throwsOAuthError() {
      // Given
      mockClientCredentials();

      httpTransport.throwIo(new IOException("network timeout"));

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "any-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage())
                    .isEqualTo("Failed to exchange code for token");
              });

      verifyNoInteractions(googleIdTokenVerifier);
    }

    @Test
    void exchangeCodeForUserInfo_missingIdToken_throwsOAuthError() {
      // Given
      mockClientCredentials();

      httpTransport.respondWith(200, tokenResponseJson(null));

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "valid-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Missing ID token in response");
              });

      verifyNoInteractions(googleIdTokenVerifier);
    }

    @Test
    void exchangeCodeForUserInfo_verifierReturnsFalse_throwsOAuthError() throws Exception {
      // Given
      mockClientCredentials();

      String idToken = buildIdToken("john.doe@example.com", "John Doe", true);
      httpTransport.respondWith(200, tokenResponseJson(idToken));
      when(googleIdTokenVerifier.verify(any(GoogleIdToken.class))).thenReturn(false);

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "valid-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Invalid or expired ID token");
              });
    }

    @Test
    void exchangeCodeForUserInfo_verifierThrowsSecurityException_throwsOAuthError()
        throws Exception {
      // Given
      mockClientCredentials();

      String idToken = buildIdToken("john.doe@example.com", "John Doe", true);
      httpTransport.respondWith(200, tokenResponseJson(idToken));
      when(googleIdTokenVerifier.verify(any(GoogleIdToken.class)))
          .thenThrow(new GeneralSecurityException("signature error"));

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "valid-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Failed to verify ID token");
              });
    }

    @Test
    void exchangeCodeForUserInfo_incompletePayload_throwsOAuthError() throws Exception {
      // Given
      mockClientCredentials();

      String idToken = buildIdToken("john.doe@example.com", null, true);
      httpTransport.respondWith(200, tokenResponseJson(idToken));
      when(googleIdTokenVerifier.verify(any(GoogleIdToken.class))).thenReturn(true);

      // When / Then
      assertThatThrownBy(
              () ->
                  googleOAuthService.exchangeCodeForUserInfo(
                      "valid-code", "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Incomplete user information");
              });
    }

    @Test
    void exchangeCodeForUserInfo_nullCode_throwsOAuthErrorAndSkipsTokenVerification() {
      // When / Then
      assertThatThrownBy(
              () -> googleOAuthService.exchangeCodeForUserInfo(null, "http://localhost/callback"))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Authorization code is required");
              });

      verifyNoInteractions(googleIdTokenVerifier);
    }

    @Test
    void exchangeCodeForUserInfo_blankRedirectUri_throwsOAuthErrorAndSkipsTokenVerification() {
      assertThatThrownBy(() -> googleOAuthService.exchangeCodeForUserInfo("valid", " "))
          .isInstanceOf(AuthException.class)
          .satisfies(
              ex -> {
                AuthException authException = (AuthException) ex;
                assertThat(authException.getErrorCode()).isEqualTo(ErrorCode.OAUTH_ERROR);
                assertThat(authException.getMessage()).isEqualTo("Redirect URI is required");
              });

      verifyNoInteractions(googleIdTokenVerifier);
    }
  }

  private void mockClientCredentials() {
    when(googleOAuthProperties.clientId()).thenReturn(CLIENT_ID);
    when(googleOAuthProperties.clientSecret()).thenReturn(CLIENT_SECRET);
  }

  private static String tokenResponseJson(String idToken) {
    StringBuilder json = new StringBuilder();
    json.append("{")
        .append("\"access_token\":\"access-token\",")
        .append("\"token_type\":\"Bearer\",")
        .append("\"expires_in\":3599");

    if (idToken != null) {
      json.append(",\"id_token\":\"").append(idToken).append("\"");
    }

    json.append("}");
    return json.toString();
  }

  private static String buildIdToken(String email, String name, Boolean emailVerified) {
    String header = "{\"alg\":\"none\",\"typ\":\"JWT\"}";

    StringBuilder payload = new StringBuilder("{");
    List<String> fields = new ArrayList<>();
    if (email != null) {
      fields.add("\"email\":\"" + email + "\"");
    }
    if (name != null) {
      fields.add("\"name\":\"" + name + "\"");
    }
    if (emailVerified != null) {
      fields.add("\"email_verified\":" + emailVerified);
    }
    payload.append(String.join(",", fields)).append("}");

    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    String headerPart = encoder.encodeToString(header.getBytes(StandardCharsets.UTF_8));
    String payloadPart =
        encoder.encodeToString(payload.toString().getBytes(StandardCharsets.UTF_8));
    String signaturePart = encoder.encodeToString("signature".getBytes(StandardCharsets.UTF_8));
    return headerPart + "." + payloadPart + "." + signaturePart;
  }

  private static final class StubHttpTransport extends HttpTransport {

    private SimpleLowLevelHttpResponse response = new SimpleLowLevelHttpResponse(200, "{}");
    private IOException ioException;

    void respondWith(int statusCode, String content) {
      this.response = new SimpleLowLevelHttpResponse(statusCode, content);
      this.ioException = null;
    }

    void throwIo(IOException exception) {
      this.ioException = exception;
    }

    @Override
    protected LowLevelHttpRequest buildRequest(String method, String url) {
      return new LowLevelHttpRequest() {
        @Override
        public void addHeader(String name, String value) {
          // No-op for tests.
        }

        @Override
        public LowLevelHttpResponse execute() throws IOException {
          if (ioException != null) {
            throw ioException;
          }
          return response;
        }
      };
    }
  }

  private static final class SimpleLowLevelHttpResponse extends LowLevelHttpResponse {

    private final int statusCode;
    private final byte[] contentBytes;

    private SimpleLowLevelHttpResponse(int statusCode, String content) {
      this.statusCode = statusCode;
      this.contentBytes = content.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public InputStream getContent() {
      return new ByteArrayInputStream(contentBytes);
    }

    @Override
    public String getContentEncoding() {
      return null;
    }

    @Override
    public long getContentLength() {
      return contentBytes.length;
    }

    @Override
    public String getContentType() {
      return "application/json";
    }

    @Override
    public String getReasonPhrase() {
      return statusCode >= 400 ? "Bad Request" : "OK";
    }

    @Override
    public int getStatusCode() {
      return statusCode;
    }

    @Override
    public String getStatusLine() {
      return "HTTP/1.1 " + statusCode + " " + getReasonPhrase();
    }

    @Override
    public int getHeaderCount() {
      return 0;
    }

    @Override
    public String getHeaderName(int index) {
      return null;
    }

    @Override
    public String getHeaderValue(int index) {
      return null;
    }
  }
}
