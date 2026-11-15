package com.ai.prathi.agent.workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface WorkflowSupportAgent {

    @Agent
    String processRefund(
            @MemoryId String conversationId,
            @V("request") String request);
}
