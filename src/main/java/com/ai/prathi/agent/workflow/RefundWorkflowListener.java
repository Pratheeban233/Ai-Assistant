package com.ai.prathi.agent.workflow;

import dev.langchain4j.agentic.observability.AgentListener;
import dev.langchain4j.agentic.observability.AgentRequest;
import dev.langchain4j.agentic.observability.AgentResponse;
import dev.langchain4j.agentic.observability.AgentInvocationError;
import dev.langchain4j.agentic.scope.AgenticScope;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefundWorkflowListener implements AgentListener {

    private static final int MAX_RESULT_LENGTH = 240;

    @Override
    public boolean inheritedBySubagents() {
        return true;
    }

    @Override
    public void afterAgenticScopeCreated(AgenticScope scope) {
        log.info("[refund-supervisor] START conversation={}", scope.memoryId());
    }

    @Override
    public void beforeAgentInvocation(AgentRequest request) {
        log.info("[refund-supervisor] START agent={}", request.agentName());
    }

    @Override
    public void afterAgentInvocation(AgentResponse response) {
        log.info("[refund-supervisor] DONE agent={} outputKey={} result={}",
                response.agentName(),
                response.agent().outputKey(),
                summarize(response.output()));
    }

    @Override
    public void onAgentInvocationError(AgentInvocationError error) {
        log.error("[refund-supervisor] FAILED agent={}", error.agentName(), error.error());
    }

    @Override
    public void beforeAgenticScopeDestroyed(AgenticScope scope) {
        log.info("[refund-supervisor] END conversation={}", scope.memoryId());
    }

    private String summarize(Object output) {
        if (output == null) {
            return "<no output>";
        }

        String result = output.toString().replaceAll("\\s+", " ").trim();
        return result.length() <= MAX_RESULT_LENGTH
                ? result
                : result.substring(0, MAX_RESULT_LENGTH) + "...";
    }
}
