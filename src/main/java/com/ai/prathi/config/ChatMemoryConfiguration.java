package com.ai.prathi.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfiguration {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 20;

    @Bean
    ChatMemoryProvider assistantChatMemoryProvider() {
        return conversationId -> newMemory(conversationId);
    }

    @Bean
    ChatMemoryProvider orderChatMemoryProvider() {
        return conversationId -> newMemory(conversationId);
    }

    private MessageWindowChatMemory newMemory(Object conversationId) {
        return MessageWindowChatMemory.builder()
                .id(conversationId)
                .maxMessages(MAX_MESSAGES_PER_CONVERSATION)
                .build();
    }
}
