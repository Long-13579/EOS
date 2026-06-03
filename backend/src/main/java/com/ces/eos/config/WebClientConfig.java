package com.ces.eos.config;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

  private final AzureOpenAiConfig azureOpenAiConfig;

  @Bean
  public WebClient azureOpenAiWebClient() {
    return WebClient.builder()
        .baseUrl(azureOpenAiConfig.getEndpoint())
        .defaultHeader("api-key", azureOpenAiConfig.getApiKey())
        .defaultHeader("Content-Type", "application/json")
        .build();
  }
}
