package com.ai.prathi.service;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.PaymentDetails;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.entity.ShippingDetails;
import com.ai.prathi.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CustomerOrderRepository orderRepository;
    private final RefundPolicyService policyService;

    public Optional<CustomerOrder> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Transactional
    public CustomerOrder createOrder(String customerName, String customerEmail, String productName,
                                     int quantity, BigDecimal unitPrice, String paymentMethod,
                                     String recipientName, String addressLine1, String city,
                                     String region, String postalCode, String country) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero.");
        }

        LocalDateTime now = LocalDateTime.now();
        CustomerOrder order = new CustomerOrder(
                "ORD-" + UUID.randomUUID(),
                customerName,
                customerEmail,
                productName,
                quantity,
                unitPrice,
                "PROCESSING",
                new PaymentDetails(paymentMethod, "PENDING", null, null),
                new ShippingDetails(recipientName, addressLine1, null, city, region, postalCode,
                        country, null, null, "NOT_SHIPPED", null, null),
                now
        );
        return orderRepository.save(order);
    }

    @Transactional
    public String cancelOrder(String orderNumber) {
        CustomerOrder order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return "Order " + orderNumber + " was not found.";
        }
        try {
            order.cancel();
            return "Order " + orderNumber + " was cancelled.";
        } catch (IllegalStateException exception) {
            return exception.getMessage();
        }
    }

    @Transactional
    public String cancelDelayedPaidOrder(String orderNumber) {
        CustomerOrder order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return "Order " + orderNumber + " was not found.";
        }
        var decision = policyService.evaluate(
                order, RefundPolicyAction.CANCEL_DELAYED_ORDER);
        if (!decision.eligible()) {
            return decision.message();
        }
        try {
            order.cancel();
            return "Order " + orderNumber + " was cancelled.";
        } catch (IllegalStateException exception) {
            return exception.getMessage();
        }
    }
}
