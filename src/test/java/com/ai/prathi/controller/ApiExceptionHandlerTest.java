package com.ai.prathi.controller;

import com.ai.prathi.agent.AssistantAgent;
import com.ai.prathi.agent.OrderAgent;
import com.ai.prathi.agent.workflow.RefundSupervisorAgent;
import com.ai.prathi.entity.ApprovalStatus;
import com.ai.prathi.entity.ExecutionStatus;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.request.ApprovalDecisionRequest;
import com.ai.prathi.response.ApprovalResponse;
import com.ai.prathi.service.ApprovalConflictException;
import com.ai.prathi.service.OrderActionApprovalService;
import dev.langchain4j.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class ApiExceptionHandlerTest {

    private AssistantAgent assistantAgent;
    private OrderAgent orderAgent;
    private RefundSupervisorAgent refundSupervisorAgent;
    private OrderActionApprovalService approvalService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        assistantAgent = mock(AssistantAgent.class);
        orderAgent = mock(OrderAgent.class);
        refundSupervisorAgent = mock(RefundSupervisorAgent.class);
        approvalService = mock(OrderActionApprovalService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AssistantController(
                        assistantAgent,
                        orderAgent,
                        refundSupervisorAgent,
                        approvalService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsStructuredErrorWhenAiProviderRejectsARequest() throws Exception {
        when(orderAgent.chat("conversation-1", "check ORD-1001"))
                .thenThrow(new InvalidRequestException("Missing thought_signature from provider details"));

        mockMvc.perform(post("/api/assistant/order")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"check ORD-1001"}
                                """))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.title").value("Bad Gateway"))
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.code").value("AI_PROVIDER_ERROR"))
                .andExpect(jsonPath("$.detail").value(
                        "The AI provider could not complete the request. Please try again."))
                .andExpect(jsonPath("$.instance").value("/api/assistant/order"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void returnsStructuredErrorWhenRequestBodyIsMissing() throws Exception {
        mockMvc.perform(post("/api/assistant/chat").contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.instance").value("/api/assistant/chat"));
    }

    @Test
    void forwardsConversationIdAndMessageToAssistant() throws Exception {
        when(assistantAgent.chat("assistant:chat:conversation-1", "Hello")).thenReturn("Hi!");

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"Hello"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Hi!"));

        verify(assistantAgent).chat("assistant:chat:conversation-1", "Hello");
    }

    @Test
    void returnsStructuredErrorWhenMessageIsBlank() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("message: must not be blank"));
    }

    @Test
    void rejectsBlankConversationId() throws Exception {
        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"  ","message":"Hello"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("conversationId: must not be blank"));
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromTheClient() throws Exception {
        doThrow(new IllegalStateException("internal database detail"))
                .when(assistantAgent).chat("assistant:chat:conversation-1", "hello");

        mockMvc.perform(post("/api/assistant/chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"hello"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value(
                        "An unexpected error occurred. Please try again."));
    }

    @Test
    void forwardsCustomerNameToPersonalizedAssistant() throws Exception {
        when(assistantAgent.personalizedChat(
                "assistant:personalized:conversation-1", "How is my day?", "Avery"))
                .thenReturn("Hello, Avery!");

        mockMvc.perform(post("/api/assistant/personalized-chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"How is my day?","customerName":"Avery"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello, Avery!"));

        verify(assistantAgent).personalizedChat(
                "assistant:personalized:conversation-1", "How is my day?", "Avery");
    }

    @Test
    void rejectsBlankCustomerName() throws Exception {
        mockMvc.perform(post("/api/assistant/personalized-chat")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"Hello","customerName":"  "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value("customerName: must not be blank"));
    }

    @Test
    void rejectsMalformedJsonAsStructuredBadRequest() throws Exception {
        mockMvc.perform(post("/api/assistant/order")
                        .contentType(APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.detail").value(
                        "The request body is missing, malformed, or invalid."));
    }

    @Test
    void sendsRefundRequestThroughTheSupervisor() throws Exception {
        when(refundSupervisorAgent.handleRequest(
                "assistant:refund:conversation-1", "Please refund order ORD-1001"))
                .thenReturn("Your simulated refund has been recorded.");

        mockMvc.perform(post("/api/assistant/refund")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"conversationId":"conversation-1","message":"Please refund order ORD-1001"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Your simulated refund has been recorded."));

        verify(refundSupervisorAgent).handleRequest(
                "assistant:refund:conversation-1", "Please refund order ORD-1001");
    }

    @Test
    void approvesPendingActionThroughHumanApprovalEndpoint() throws Exception {
        when(approvalService.approve(
                "approval-123", "Alex Reviewer", "Verified receipt"))
                .thenReturn(new ApprovalResponse(
                        "approval-123",
                        "ORD-1001",
                        RefundPolicyAction.DELIVERED_REFUND,
                        null,
                        "Item arrived damaged",
                        ApprovalStatus.APPROVED,
                        ExecutionStatus.SUCCEEDED,
                        "Alex Reviewer",
                        "Verified receipt",
                        "Simulated refund recorded.",
                        null,
                        null,
                        null));

        mockMvc.perform(post("/api/assistant/approvals/approval-123/approve")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"reviewerName":"Alex Reviewer","note":"Verified receipt"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalId").value("approval-123"))
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"))
                .andExpect(jsonPath("$.executionStatus").value("SUCCEEDED"))
                .andExpect(jsonPath("$.executionResult").value("Simulated refund recorded."));

        verify(approvalService).approve("approval-123", "Alex Reviewer", "Verified receipt");
    }

    @Test
    void listsPendingApprovals() throws Exception {
        when(approvalService.getPendingApprovals()).thenReturn(List.of());

        mockMvc.perform(get("/api/assistant/approvals/pending"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(approvalService).getPendingApprovals();
    }

    @Test
    void returnsConflictWhenAnApprovalWasAlreadyReviewed() throws Exception {
        doThrow(new ApprovalConflictException("Approval approval-123 has already been reviewed."))
                .when(approvalService).approve("approval-123", "Alex Reviewer", null);

        mockMvc.perform(post("/api/assistant/approvals/approval-123/approve")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {"reviewerName":"Alex Reviewer"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("APPROVAL_ALREADY_REVIEWED"));
    }
}
