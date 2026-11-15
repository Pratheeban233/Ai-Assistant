package com.ai.prathi.request;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank(message = "must not be blank") String conversationId,
        @NotBlank(message = "must not be blank") String message) {
}
