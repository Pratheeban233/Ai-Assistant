package com.ai.prathi.request;

import jakarta.validation.constraints.NotBlank;

public record PersonalizedChatRequest(
        @NotBlank(message = "must not be blank") String conversationId,
        @NotBlank(message = "must not be blank") String message,
        @NotBlank(message = "must not be blank") String customerName) {
}
