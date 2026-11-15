package com.ai.prathi.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatMemoryConfigurationTest {

    private final ChatMemoryConfiguration configuration = new ChatMemoryConfiguration();

    @Test
    void createsIndependentMemoryForEachConversationAndAgent() {
        var assistantProvider = configuration.assistantChatMemoryProvider();
        var orderProvider = configuration.orderChatMemoryProvider();

        var assistantMemory = assistantProvider.get("conversation-1");
        var anotherConversationMemory = assistantProvider.get("conversation-2");
        var orderMemory = orderProvider.get("conversation-1");

        assertThat(assistantMemory).isInstanceOf(MessageWindowChatMemory.class);
        assertThat(assistantMemory.id()).isEqualTo("conversation-1");
        assertThat(anotherConversationMemory).isNotSameAs(assistantMemory);
        assertThat(orderMemory).isNotSameAs(assistantMemory);
        assertThat(orderMemory.id()).isEqualTo("conversation-1");
    }
}
