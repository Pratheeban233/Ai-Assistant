package com.ai.prathi.controller;

import com.ai.prathi.agent.AssistantAgent;
import com.ai.prathi.agent.OrderAgent;
import dev.langchain4j.exception.InvalidRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ApiExceptionHandlerTest {

    private AssistantAgent assistantAgent;
    private OrderAgent orderAgent;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        assistantAgent = mock(AssistantAgent.class);
        orderAgent = mock(OrderAgent.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AssistantController(assistantAgent, orderAgent))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();
    }

    @Test
    void returnsStructuredErrorWhenAiProviderRejectsARequest() throws Exception {
        when(orderAgent.chat("check ORD-1001"))
                .thenThrow(new InvalidRequestException("Missing thought_signature from provider details"));

        mockMvc.perform(get("/api/assistant/order").param("message", "check ORD-1001"))
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
    void returnsStructuredErrorWhenMessageIsMissing() throws Exception {
        mockMvc.perform(get("/api/assistant/chat"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.instance").value("/api/assistant/chat"));
    }

    @Test
    void returnsStructuredErrorWhenMessageIsBlank() throws Exception {
        mockMvc.perform(get("/api/assistant/chat").param("message", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void hidesUnexpectedExceptionDetailsFromTheClient() throws Exception {
        doThrow(new IllegalStateException("internal database detail"))
                .when(assistantAgent).chat("hello");

        mockMvc.perform(get("/api/assistant/chat").param("message", "hello"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value(
                        "An unexpected error occurred. Please try again."));
    }
}
