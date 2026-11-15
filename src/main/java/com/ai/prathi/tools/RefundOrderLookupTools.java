package com.ai.prathi.tools;

import com.ai.prathi.service.OrderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefundOrderLookupTools {

    private final OrderService orderService;

    @Tool("Look up verified order, payment, and shipping details by order number.")
    public String getRefundOrderDetails(@P("The order number to verify") String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(order -> "Verified order " + order.getOrderNumber()
                        + "; order status: " + order.getOrderStatus()
                        + "; payment status: " + order.getPayment().getStatus()
                        + "; shipping status: " + order.getShipping().getStatus()
                        + "; shipped at: " + order.getShipping().getShippedAt()
                        + "; total: " + order.getTotalAmount() + ".")
                .orElse("Order " + orderNumber + " was not found.");
    }
}
