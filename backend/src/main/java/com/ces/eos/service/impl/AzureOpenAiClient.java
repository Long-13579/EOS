package com.ces.eos.service.impl;

import com.ces.eos.config.AzureOpenAiConfig;
import com.ces.eos.dto.azure.ChatCompletionRequest;
import com.ces.eos.dto.azure.ChatCompletionResponse;
import com.ces.eos.dto.azure.ChatMessage;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AzureOpenAiClient {

  private final WebClient azureOpenAiWebClient;
  private final AzureOpenAiConfig azureOpenAiConfig;

  public String generateSummary(String prompt) {
    String deploymentId = azureOpenAiConfig.getDeploymentId();
    String url = "/openai/deployments/%s/chat/completions?api-version=2024-02-15-preview"
        .formatted(deploymentId);

    ChatCompletionRequest request = new ChatCompletionRequest(
        List.of(new ChatMessage("user", prompt)),
        azureOpenAiConfig.getMaxTokens(),
        azureOpenAiConfig.getTemperature());

    log.info("Calling Azure OpenAI for summary generation, deploymentId={}", deploymentId);

    ChatCompletionResponse response = azureOpenAiWebClient.post()
        .uri(url)
        .bodyValue(request)
        .retrieve()
        .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
            clientResponse.bodyToMono(String.class).flatMap(body -> {
              log.error("Azure OpenAI 4xx error: status={} body={}", clientResponse.statusCode(), body);
              return Mono.error(new RuntimeException("Azure OpenAI API error: " + clientResponse.statusCode()));
            }))
        .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
            clientResponse.bodyToMono(String.class).flatMap(body -> {
              log.error("Azure OpenAI 5xx error: status={} body={}", clientResponse.statusCode(), body);
              return Mono.error(new RuntimeException("Azure OpenAI server error: " + clientResponse.statusCode()));
            }))
        .bodyToMono(ChatCompletionResponse.class)
        .timeout(Duration.ofSeconds(azureOpenAiConfig.getTimeout()))
        .block();

    if (response == null || response.choices() == null || response.choices().isEmpty()) {
      throw new RuntimeException("Azure OpenAI returned empty response");
    }

    String content = response.choices().getFirst().message().content();
    log.info("Azure OpenAI summary generated successfully, length={}", content.length());
    return content;
  }
}
