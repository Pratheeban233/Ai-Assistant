package com.ai.prathi.config;

import com.ai.prathi.agent.workflow.OrderAgent;
import com.ai.prathi.agent.workflow.RefundAgent;
import com.ai.prathi.agent.workflow.RefundWorkflowListener;
import com.ai.prathi.agent.workflow.ResponseAgent;
import com.ai.prathi.agent.workflow.WorkflowSupportAgent;
import com.ai.prathi.tools.OrderCancellationTools;
import com.ai.prathi.tools.RefundOrderLookupTools;
import com.ai.prathi.tools.RefundTools;
import com.ai.prathi.tools.ReplacementTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentConfiguration {

    private static final int MAX_MESSAGES_PER_CONVERSATION = 20;

    @Bean
    OrderAgent refundOrderAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel,
            RefundOrderLookupTools orderLookupTools) {
        return AgenticServices.agentBuilder(OrderAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(orderLookupTools)
                .outputKey("orderDetails")
                .build();
    }

    @Bean
    RefundAgent refundAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel,
            RefundTools refundTools,
            OrderCancellationTools cancellationTools,
            ReplacementTools replacementTools) {
        return AgenticServices.agentBuilder(RefundAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(refundTools, cancellationTools, replacementTools)
                .outputKey("refundDecision")
                .build();
    }

    @Bean
    ResponseAgent responseAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel) {
        return AgenticServices.agentBuilder(ResponseAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .outputKey("finalResponse")
                .build();
    }

    @Bean
    WorkflowSupportAgent workflowSupportAgent(
            OrderAgent refundOrderAgent,
            RefundAgent refundAgent,
            ResponseAgent responseAgent,
            RefundWorkflowListener listener) {
        return AgenticServices.sequenceBuilder(WorkflowSupportAgent.class)
                .subAgents(refundOrderAgent, refundAgent, responseAgent)
                .outputKey("finalResponse")
                .listener(listener)
                .build();
    }

    private ChatMemoryProvider chatMemoryProvider() {
        return conversationId -> MessageWindowChatMemory.builder()
                .id(conversationId)
                .maxMessages(MAX_MESSAGES_PER_CONVERSATION)
                .build();
    }
}
