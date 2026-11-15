package com.ai.prathi.controller;

import com.ai.prathi.agent.AssistantAgent;
import com.ai.prathi.agent.OrderAgent;
import com.ai.prathi.agent.workflow.RefundSupervisorAgent;
import com.ai.prathi.request.ApprovalDecisionRequest;
import com.ai.prathi.request.ChatRequest;
import com.ai.prathi.request.PersonalizedChatRequest;
import com.ai.prathi.response.ApprovalResponse;
import com.ai.prathi.service.OrderActionApprovalService;
import dev.langchain4j.service.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final AssistantAgent assistantAgent;
    private final OrderAgent orderAgent;
    private final RefundSupervisorAgent refundSupervisorAgent;
    private final OrderActionApprovalService approvalService;

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
        return refundSupervisorAgent.handleRequest(
                assistantConversationId("refund", request.conversationId()), request.message());
    }

    @GetMapping("/approvals/{approvalId}")
    public ApprovalResponse getApproval(@PathVariable String approvalId) {
        return approvalService.getApproval(approvalId);
    }

    @GetMapping("/approvals/pending")
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalService.getPendingApprovals();
    }

    @PostMapping("/approvals/{approvalId}/approve")
    public ResponseEntity<ApprovalResponse> approve(
            @PathVariable String approvalId,
            @Valid @RequestBody ApprovalDecisionRequest request) {
        ApprovalResponse response =
                approvalService.approve(approvalId, request.reviewerName(), request.note());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping("/approvals/{approvalId}/reject")
    public ApprovalResponse reject(
            @PathVariable String approvalId,
            @Valid @RequestBody ApprovalDecisionRequest request) {
        return approvalService.reject(approvalId, request.reviewerName(), request.note());
    }

    private String assistantConversationId(String mode, String conversationId) {
        return "assistant:" + mode + ":" + conversationId;
    }

}