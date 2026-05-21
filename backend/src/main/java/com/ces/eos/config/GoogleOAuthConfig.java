package com.ces.eos.config;

import com.ces.eos.config.properties.GoogleOAuthProperties;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@EnableConfigurationProperties(GoogleOAuthProperties.class)
@RequiredArgsConstructor
public class GoogleOAuthConfig {

  private final GoogleOAuthProperties googleOAuthProperties;

  @Bean
  public HttpTransport httpTransport() {
    try {
      return GoogleNetHttpTransport.newTrustedTransport();
    } catch (GeneralSecurityException | IOException e) {
      log.error("Failed to create trusted HTTP transport", e);
      throw new IllegalStateException("Failed to create trusted HTTP transport", e);
    }
  }

  @Bean
  public JsonFactory jsonFactory() {
    return GsonFactory.getDefaultInstance();
  }

  @Bean
  public GoogleIdTokenVerifier googleIdTokenVerifier(
      HttpTransport httpTransport, JsonFactory jsonFactory) {
    return new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
        .setAudience(Collections.singletonList(googleOAuthProperties.clientId()))
        .build();
  }
}
