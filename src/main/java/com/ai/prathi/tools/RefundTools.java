package com.ai.prathi.tools;

import com.ai.prathi.service.RefundService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundTools {

    private final RefundService refundService;

    @Tool("Record a simulated refund for an eligible paid delayed or delivered order. This does not contact a payment provider.")
    public String issueRefund(
            @P("Verified order number") String orderNumber,
            @P("Customer's reason for requesting the refund") String reason) {
        return refundService.issueRefund(orderNumber, reason);
    }
}
