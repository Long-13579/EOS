package com.ces.eos.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "azure.openai")
public class AzureOpenAiConfig {
  private String endpoint;
  private String apiKey;
  private String deploymentId;
  private int timeout = 30;
  private int maxTokens = 1000;
  private double temperature = 0.7;

  @PostConstruct
  void cleanQuotes() {
    this.endpoint = stripQuotes(endpoint);
    this.apiKey = stripQuotes(apiKey);
    this.deploymentId = stripQuotes(deploymentId);
  }

  private static String stripQuotes(String s) {
    if (s == null) return null;
    if ((s.startsWith("'") && s.endsWith("'"))
        || (s.startsWith("\"") && s.endsWith("\""))) {
      return s.substring(1, s.length() - 1);
    }
    return s;
  }
}
