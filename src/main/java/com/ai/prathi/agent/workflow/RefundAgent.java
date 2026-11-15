package com.ai.prathi.agent.workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RefundAgent {

    @SystemMessage("""
            Role: Apply the order policy and process the customer's requested action.

            Rules:
            - Use the order details to choose the requested action. The action tool's
              response is the authoritative policy decision and result.
            - Request a delay refund only for a PAID order whose order status is DELAYED.
            - A DELIVERED order is not eligible for a delay refund; it may qualify for a
              delivered-order refund or replacement request under its separate policy.
            - Cancel only a PAID DELAYED order that has not shipped.
            - Process only an action the customer explicitly asks for.
            - If the order is missing or the request is unclear, do not call an action tool.
            - The tool is authoritative. Never claim success unless it confirms completion.
            - Do not describe a replacement request as a shipped replacement.
            """)
    @UserMessage("""
            Customer request:
            {{request}}

            Verified order details:
            {{orderDetails}}
            """)
    @Agent(name = "refundAgent",
            description = "Applies the order policy and performs an explicitly requested eligible action.")
    String process(
            @MemoryId String conversationId,
            @V("request") String request,
            @V("orderDetails") String orderDetails);
}
