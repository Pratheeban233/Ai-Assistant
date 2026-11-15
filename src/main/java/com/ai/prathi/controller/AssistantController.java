package com.ai.prathi.controller;

import com.ai.prathi.agent.AssistantAgent;
import com.ai.prathi.agent.OrderAgent;
import com.ai.prathi.request.ChatRequest;
import com.ai.prathi.request.PersonalizedChatRequest;
import dev.langchain4j.service.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantAgent assistantAgent;
    private final OrderAgent orderAgent;

    @PostMapping("/chat")
    public String chat(@RequestBody ChatRequest request) {
        requireConversationId(request.conversationId());
        requireMessage(request.message());
        return assistantAgent.chat(
                assistantConversationId("chat", request.conversationId()), request.message());
    }

    @PostMapping("/personalized-chat")
    public String personalizedChat(@RequestBody PersonalizedChatRequest request) {
        requireConversationId(request.conversationId());
        requireMessage(request.message());
        requireCustomerName(request.customerName());
        return assistantAgent.personalizedChat(
                assistantConversationId("personalized", request.conversationId()),
                request.message(),
                request.customerName());
    }

    @PostMapping("/order")
    public String order(@RequestBody ChatRequest request) {
        requireConversationId(request.conversationId());
        requireMessage(request.message());
        Result<String> result =
                orderAgent.chat(request.conversationId(), request.message());

        System.out.println("Final response: " + result.content());

        result.toolExecutions().forEach(tool -> {
            System.out.println("Tool: >>> " + tool.request().name());
            System.out.println("Arguments: >> " + tool.request().arguments());
            System.out.println("Result: > " + tool.result());
        });

        return result.content();
    }

    private void requireConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            throw new IllegalArgumentException("The conversationId field must not be blank.");
        }
    }

    private String assistantConversationId(String mode, String conversationId) {
        return "assistant:" + mode + ":" + conversationId;
    }

    private void requireMessage(String message) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("The message field must not be blank.");
        }
    }

    private void requireCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            throw new IllegalArgumentException("The customerName field must not be blank.");
        }
    }

}