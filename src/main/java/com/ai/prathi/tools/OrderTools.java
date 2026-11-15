package com.ai.prathi.tools;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.service.OrderActionApprovalService;
import com.ai.prathi.service.OrderService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("orderTools")
@RequiredArgsConstructor
public class OrderTools {

    private final OrderService orderService;
    private final OrderActionApprovalService approvalService;

    @Tool("Create an ecommerce order using the customer, product, payment, and shipping details provided.")
    public String createOrder(
            @P("Customer's full name") String customerName,
            @P("Customer's email address") String customerEmail,
            @P("Product name") String productName,
            @P("Number of units to order") int quantity,
            @P("Price per unit as a decimal amount") BigDecimal unitPrice,
            @P("Payment method, such as CREDIT_CARD or PAYPAL") String paymentMethod,
            @P("Name of the shipping recipient") String recipientName,
            @P("First line of the shipping address") String addressLine1,
            @P("Shipping city") String city,
            @P("Shipping state, province, or region") String region,
            @P("Shipping postal code") String postalCode,
            @P("Shipping country") String country) {
        CustomerOrder order = orderService.createOrder(customerName, customerEmail, productName,
                quantity, unitPrice, paymentMethod, recipientName, addressLine1, city, region,
                postalCode, country);
        return "Order created: " + order.getOrderNumber() + ", status " + order.getOrderStatus()
                + ", total " + order.getTotalAmount() + ".";
    }

    @Tool("Get the current status and total for an ecommerce order by its order number.")
    public String getOrderStatus(@P("Order number to look up") String orderNumber) {
        return orderService.findByOrderNumber(orderNumber)
                .map(order -> "Order " + order.getOrderNumber() + " status: " + order.getOrderStatus()
                        + "; shipping status: " + order.getShipping().getStatus()
                        + "; total: " + order.getTotalAmount() + ".")
                .orElse("Order " + orderNumber + " was not found.");
    }

    @Tool("Request human approval to cancel an ecommerce order. Orders that have shipped cannot be cancelled.")
    public String cancelOrder(@P("Order number to cancel") String orderNumber) {
        return approvalService.requestCancellationApproval(orderNumber);
    }
}
