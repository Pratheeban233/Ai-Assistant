package com.ai.prathi.tools;

import com.ai.prathi.service.OrderActionApprovalService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCancellationTools {

    private final OrderActionApprovalService approvalService;

    @Tool("Request human approval to cancel an eligible delayed order. Do not cancel directly.")
    public String requestDelayedCancellationApproval(
            @P("The order number to cancel") String orderNumber) {
        return approvalService.requestDelayedCancellationApproval(orderNumber);
    }
}
