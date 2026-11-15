package com.ai.prathi.service;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.entity.ReplacementRequest;
import com.ai.prathi.repository.CustomerOrderRepository;
import com.ai.prathi.repository.RefundTransactionRepository;
import com.ai.prathi.repository.ReplacementRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReplacementService {

    private static final int MAX_REASON_LENGTH = 500;

    private final CustomerOrderRepository orderRepository;
    private final ReplacementRequestRepository replacementRepository;
    private final RefundTransactionRepository refundRepository;
    private final RefundPolicyService policyService;

    @Transactional
    public String requestReplacement(String orderNumber, String reason) {
        if (orderNumber == null || orderNumber.isBlank()) {
            return "Replacement rejected: an order number is required.";
        }
        if (reason == null || reason.isBlank()) {
            return "Replacement rejected: a reason is required.";
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            return "Replacement rejected: the reason must be 500 characters or fewer.";
        }

        CustomerOrder order = orderRepository.findByOrderNumber(orderNumber).orElse(null);
        if (order == null) {
            return "Replacement rejected: order " + orderNumber + " was not found.";
        }

        var existingRequest = replacementRepository.findByOrderNumber(orderNumber);
        if (existingRequest.isPresent()) {
            return "A replacement request already exists for order " + orderNumber
                    + " with status " + existingRequest.get().getStatus() + ".";
        }
        if (refundRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "Replacement rejected: a refund is already recorded for order "
                    + orderNumber + ".";
        }

        var decision = policyService.evaluate(
                order, RefundPolicyAction.DELIVERED_REPLACEMENT);
        if (!decision.eligible()) {
            return "Replacement rejected: " + decision.message();
        }

        replacementRepository.save(new ReplacementRequest(
                orderNumber, reason.trim(), LocalDateTime.now()));
        return "Replacement request recorded for order " + orderNumber
                + ". This is a request only; no replacement shipment was created.";
    }
}
