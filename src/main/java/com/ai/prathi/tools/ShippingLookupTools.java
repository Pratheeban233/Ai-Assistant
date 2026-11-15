package com.ai.prathi.tools;

import com.ai.prathi.service.OrderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShippingLookupTools {

    private final OrderService orderService;

    @Tool("Look up verified shipping and delivery information by order number.")
    public String getShippingDetails(@P("The order number to look up") String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(order -> "Verified shipping details for order " + order.getOrderNumber()
                        + "; shipping status: " + order.getShipping().getStatus()
                        + "; carrier: " + order.getShipping().getCarrier()
                        + "; tracking number: " + order.getShipping().getTrackingNumber()
                        + "; shipped at: " + order.getShipping().getShippedAt()
                        + "; delivered at: " + order.getShipping().getDeliveredAt() + ".")
                .orElse("Shipping details are unavailable because order "
                        + orderNumber + " was not found.");
    }
}
