package com.ces.eos.dto.azure;

import java.util.List;

public record ChatCompletionResponse(List<Choice> choices) {
  public record Choice(ChatMessage message) {}
}
