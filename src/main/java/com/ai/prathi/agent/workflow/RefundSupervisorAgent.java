package com.ai.prathi.agent.workflow;

import dev.langchain4j.agentic.Agent;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.V;

public interface RefundSupervisorAgent {

    @Agent
    String handleRequest(
            @MemoryId String conversationId,
            @V("request") String request);
}
