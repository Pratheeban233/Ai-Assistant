package com.ai.prathi.tools;

import com.ai.prathi.service.OrderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCancellationTools {

    private final OrderService orderService;

    @Tool("Cancel an order that is delayed and has not shipped.")
    public String cancelDelayedOrder(@P("The order number to cancel") String orderNumber) {
        return orderService.cancelDelayedPaidOrder(orderNumber);
    }
}
