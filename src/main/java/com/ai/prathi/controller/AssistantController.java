package com.ai.prathi.controller;

import com.ai.prathi.agent.AssistantAgent;
import com.ai.prathi.agent.OrderAgent;
import com.ai.prathi.agent.workflow.WorkflowSupportAgent;
import com.ai.prathi.request.ChatRequest;
import com.ai.prathi.request.PersonalizedChatRequest;
import dev.langchain4j.service.Result;
import jakarta.validation.Valid;
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
    private final WorkflowSupportAgent workflowSupportAgent;

    @PostMapping("/chat")
    public String chat(@Valid @RequestBody ChatRequest request) {
        return assistantAgent.chat(
                assistantConversationId("chat", request.conversationId()), request.message());
    }

    @PostMapping("/personalized-chat")
    public String personalizedChat(@Valid @RequestBody PersonalizedChatRequest request) {
        return assistantAgent.personalizedChat(
                assistantConversationId("personalized", request.conversationId()),
                request.message(),
                request.customerName());
    }

    @PostMapping("/order")
    public String order(@Valid @RequestBody ChatRequest request) {
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

    @PostMapping("/refund")
    public String refund(@Valid @RequestBody ChatRequest request) {
        return workflowSupportAgent.processRefund(
                assistantConversationId("refund", request.conversationId()), request.message());
    }

    private String assistantConversationId(String mode, String conversationId) {
        return "assistant:" + mode + ":" + conversationId;
    }

}