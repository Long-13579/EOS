package com.ces.eos.dto.azure;

import java.util.List;

public record ChatCompletionRequest(
    List<ChatMessage> messages,
    int max_completion_tokens,
    double temperature) {}
