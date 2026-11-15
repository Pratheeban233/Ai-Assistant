package com.ai.prathi.tools;

import com.ai.prathi.service.OrderActionApprovalService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReplacementTools {

    private final OrderActionApprovalService approvalService;

    @Tool("Request human approval for a replacement request on an eligible paid delivered order. Do not create the replacement directly.")
    public String requestReplacementApproval(
            @P("Verified order number") String orderNumber,
            @P("Customer's reason for requesting a replacement") String reason) {
        return approvalService.requestReplacementApproval(orderNumber, reason);
    }
}
