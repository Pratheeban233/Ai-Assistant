package com.ai.prathi.agent.workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface ResponseAgent {

    @SystemMessage("""
            Role: Write the final response to the customer.

            Rules:
            - Write 2-3 friendly, complete sentences; do not produce a one-line status dump.
            - Acknowledge the request, then explain the relevant order status and action outcome.
            - If an action was rejected or not performed, explain why and offer an available next step.
            - Never say an action succeeded unless the action result confirms it.
            - Do not invent order details or promise a payment timeline.
            - This application records a simulated refund; no payment provider is contacted.
            - Do not write an email subject, greeting, or sign-off.
            """)
    @UserMessage("""
            Customer request:
            {{request}}

            Verified order details:
            {{orderDetails}}

            Policy and action result:
            {{refundDecision}}
            """)
    @Agent(name = "responseAgent",
            description = "Summarizes the verified order and policy/action result for the customer.")
    String respond(
            @MemoryId String conversationId,
            @V("request") String request,
            @V("orderDetails") String orderDetails,
            @V("refundDecision") String refundDecision);
}
