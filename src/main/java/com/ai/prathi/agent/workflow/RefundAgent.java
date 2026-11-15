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
            - Use the order details and the customer's explicit request to choose an action.
            - The action tools create an approval request; they do not execute the action.
            - Use order lookup and shipping results from the workflow only as context;
              the action tool rechecks the order and enforces the saved policy.
            - Request a delay refund only for a PAID order whose order status is DELAYED.
            - A DELIVERED order is not eligible for a delay refund; it may qualify for a
              delivered-order refund or replacement request under its separate policy.
            - Cancel only a PAID DELAYED order that has not shipped.
            - Process only an action the customer explicitly asks for.
            - If the order is missing or the request is unclear, do not call an action tool.
            - Never claim a refund, cancellation, or replacement was completed. Tell the customer
              when an eligible request is waiting for human approval and include its approval ID.
            - Do not describe a replacement request as a shipped replacement.
            """)
    @UserMessage("""
            Customer request:
            {{request}}

            Verified order and payment details:
            {{orderDetails}}

            Verified shipping details:
            {{shippingDetails}}
            """)
    @Agent(name = "refundAgent",
            description = "For explicit refund, cancellation, or replacement requests, invokes the matching action tool. The service enforces eligibility and records the result.")
    String process(
            @MemoryId String conversationId,
            @V("request") String request,
            @V("orderDetails") String orderDetails,
            @V("shippingDetails") String shippingDetails);
}
