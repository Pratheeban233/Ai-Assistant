package com.ai.prathi.config;

import com.ai.prathi.agent.workflow.OrderAgent;
import com.ai.prathi.agent.workflow.RefundAgent;
import com.ai.prathi.agent.workflow.RefundSupervisorAgent;
import com.ai.prathi.agent.workflow.RefundWorkflowListener;
import com.ai.prathi.agent.workflow.ResponseAgent;
import com.ai.prathi.agent.workflow.ShippingAgent;
import com.ai.prathi.agent.workflow.WorkflowSupportAgent;
import com.ai.prathi.tools.OrderCancellationTools;
import com.ai.prathi.tools.RefundOrderLookupTools;
import com.ai.prathi.tools.RefundTools;
import com.ai.prathi.tools.ReplacementTools;
import com.ai.prathi.tools.ShippingLookupTools;
import dev.langchain4j.agentic.AgenticServices;
import dev.langchain4j.agentic.scope.AgenticScope;
import dev.langchain4j.agentic.supervisor.SupervisorContextStrategy;
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy;
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
    OrderAgent orderLookupAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel,
            RefundOrderLookupTools orderLookupTools) {
        return AgenticServices.agentBuilder(OrderAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(orderLookupTools)
                .description("Looks up verified order status, payment status, and total.")
                .outputKey("orderDetails")
                .build();
    }

    @Bean
    ShippingAgent shippingAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel,
            ShippingLookupTools shippingLookupTools) {
        return AgenticServices.agentBuilder(ShippingAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .tools(shippingLookupTools)
                .description("Looks up verified shipping status, carrier, tracking number, and delivery dates.")
                .outputKey("shippingDetails")
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
                .description("Handles an explicitly requested refund, cancellation, or replacement request. Service rules decide eligibility.")
                .outputKey("refundDecision")
                .build();
    }

    @Bean
    ResponseAgent responseAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel) {
        return AgenticServices.agentBuilder(ResponseAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .description("Summarizes verified order, shipping, and action results in a concise natural response for the customer.")
                .outputKey("finalResponse")
                .build();
    }

    @Bean
    RefundSupervisorAgent refundSupervisorAgent(
            @Qualifier("googleAiGeminiChatModel") ChatModel chatModel,
            OrderAgent orderLookupAgent,
            ShippingAgent shippingAgent,
            RefundAgent refundAgent,
            ResponseAgent responseAgent,
            RefundWorkflowListener listener) {
        return AgenticServices.supervisorBuilder(RefundSupervisorAgent.class)
                .chatModel(chatModel)
                .chatMemoryProvider(chatMemoryProvider())
                .name("refundSupervisor")
                .description("Coordinates order lookup, shipping lookup, and requested order actions, then returns a concise customer-facing summary.")
                .supervisorContext("""
                        Coordinate the ecommerce order workflow with the available subagents.
                        For order-related requests, use OrderAgent for verified order/payment facts and
                        ShippingAgent for verified shipping facts. Invoke RefundAgent only when the
                        customer explicitly requests a refund, cancellation, or replacement; it will
                        enforce policy using application services. Do not claim an action succeeded
                        unless RefundAgent confirms it. Invoke ResponseAgent last with all results
                        collected so far. Do not invent missing information or repeat a completed action.
                        """)
                .contextGenerationStrategy(SupervisorContextStrategy.CHAT_MEMORY)
                .responseStrategy(SupervisorResponseStrategy.LAST)
                .maxAgentsInvocations(8)
                .subAgents(orderLookupAgent, shippingAgent, refundAgent, responseAgent)
                .outputKey("finalResponse")
                .listener(listener)
                .beforeCall(this::initializeWorkflowState)
                .build();
    }

    /*
     * Fixed sequence workflow retained as a beginner reference.
     * Enable this bean and inject WorkflowSupportAgent to run the same agents in a fixed order.
     *
     * @Bean
     * WorkflowSupportAgent workflowSupportAgent(
     *         OrderAgent orderLookupAgent,
     *         ShippingAgent shippingAgent,
     *         RefundAgent refundAgent,
     *         ResponseAgent responseAgent,
     *         RefundWorkflowListener listener) {
     *     return AgenticServices.sequenceBuilder(WorkflowSupportAgent.class)
     *             .subAgents(orderLookupAgent, shippingAgent, refundAgent, responseAgent)
     *             .outputKey("finalResponse")
     *             .listener(listener)
     *             .build();
     * }
     */

    private void initializeWorkflowState(AgenticScope scope) {
        scope.writeStateIfAbsent("orderDetails", "Order lookup was not performed.");
        scope.writeStateIfAbsent("shippingDetails", "Shipping lookup was not performed.");
        scope.writeStateIfAbsent("refundDecision", "No refund, cancellation, or replacement was requested.");
    }

    private ChatMemoryProvider chatMemoryProvider() {
        return conversationId -> MessageWindowChatMemory.builder()
                .id(conversationId)
                .maxMessages(MAX_MESSAGES_PER_CONVERSATION)
                .build();
    }
}
