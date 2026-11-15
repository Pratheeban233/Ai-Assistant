package com.ai.prathi.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT, chatModel = "googleAiGeminiChatModel", tools = {})
public interface AssistantAgent {

    @SystemMessage("You are a polite general-purpose assistant. For ecommerce order requests, direct the user to the order assistant.")
    String chat(String userMessage);
}