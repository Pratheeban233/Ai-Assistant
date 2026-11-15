package com.ai.prathi.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "googleAiGeminiChatModel",
        tools = "orderTools")
public interface OrderAgent {

    @SystemMessage("""
            You are a polite ecommerce order assistant.
            Use the order tools to create orders, check order status, and cancel orders.
            Ask the user for any required details that are missing before creating an order.
            Never claim an order was created, found, or cancelled unless a tool confirms it.
            Orders that have shipped cannot be cancelled.
            """)
    String chat(String userMessage);
}
