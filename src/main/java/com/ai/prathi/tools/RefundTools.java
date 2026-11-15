package com.ai.prathi.tools;

import com.ai.prathi.service.OrderActionApprovalService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundTools {

    private final OrderActionApprovalService approvalService;

    @Tool("Request human approval for a simulated refund. Never execute the refund directly.")
    public String requestRefundApproval(
            @P("Verified order number") String orderNumber,
            @P("Customer's reason for requesting the refund") String reason) {
        return approvalService.requestRefundApproval(orderNumber, reason);
    }
}
