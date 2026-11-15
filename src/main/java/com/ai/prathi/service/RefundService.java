package com.ai.prathi.service;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.entity.RefundTransaction;
import com.ai.prathi.repository.CustomerOrderRepository;
import com.ai.prathi.repository.ReplacementRequestRepository;
import com.ai.prathi.repository.RefundTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefundService {

    private static final int MAX_REASON_LENGTH = 500;

    private final CustomerOrderRepository orderRepository;
    private final RefundTransactionRepository refundRepository;
    private final ReplacementRequestRepository replacementRepository;
    private final RefundPolicyService policyService;

    @Transactional
    public String issueRefund(String orderNumber, String reason) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return "Refund rejected: an order number is required.";
        }
        if (reason == null || reason.isBlank()) {
            return "Refund rejected: a reason is required.";
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            return "Refund rejected: the reason must be 500 characters or fewer.";
        }

        CustomerOrder order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return "Refund rejected: order " + orderNumber + " was not found.";
        }

        var existingRefund = refundRepository.findByOrderNumber(orderNumber);
        if (existingRefund.isPresent()) {
            return "Refund was already recorded for order " + orderNumber
                    + " for " + existingRefund.get().getAmount() + ".";
        }
        if (replacementRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "Refund rejected: a replacement request is already recorded for order "
                    + orderNumber + ".";
        }

        RefundPolicyAction action = "DELAYED".equalsIgnoreCase(order.getOrderStatus())
                ? RefundPolicyAction.DELAY_REFUND
                : RefundPolicyAction.DELIVERED_REFUND;
        var decision = policyService.evaluate(order, action);
        if (!decision.eligible()) {
            return "Refund rejected: " + decision.message();
        }

        order.markRefunded();
        refundRepository.save(new RefundTransaction(
                orderNumber,
                order.getTotalAmount(),
                reason.trim(),
                LocalDateTime.now()));

        return "Simulated refund recorded for order " + orderNumber
                + " for " + order.getTotalAmount()
                + ". No payment provider was contacted.";
    }
}
