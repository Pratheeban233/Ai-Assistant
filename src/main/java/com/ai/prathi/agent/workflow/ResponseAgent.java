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
            - Summarize the relevant verified information in 2-3 friendly, natural sentences.
            - Do not provide headings, bullet points, or a step-by-step account of agent work.
            - Mention order or shipping details only when they help answer the customer's request.
            - If an action was rejected or not performed, briefly explain why and give a useful next step.
            - If an action is awaiting human approval, clearly say it has not been executed yet and
              include the approval ID when available.
            - If verified information is missing, say so plainly instead of guessing.
            - Never say an action succeeded unless the action result confirms it.
            - Do not invent order details or promise a payment timeline.
            - Approved refunds are simulated; no payment provider is contacted.
            - Do not add an email subject or sign-off.
            """)
    @UserMessage("""
            Customer request:
            {{request}}

            Verified order details:
            {{orderDetails}}

            Verified shipping details:
            {{shippingDetails}}

            Policy and action result (or the default if no action was requested):
            {{refundDecision}}
            """)
    @Agent(name = "responseAgent",
            description = "Summarizes the verified order and policy/action result for the customer.")
    String respond(
            @MemoryId String conversationId,
            @V("request") String request,
            @V("orderDetails") String orderDetails,
            @V("shippingDetails") String shippingDetails,
            @V("refundDecision") String refundDecision);
}
