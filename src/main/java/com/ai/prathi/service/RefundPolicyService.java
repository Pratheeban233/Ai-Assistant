package com.ai.prathi.service;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.repository.RefundPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefundPolicyService {

    private final RefundPolicyRepository policyRepository;

    public PolicyDecision evaluate(CustomerOrder order, RefundPolicyAction action) {
        var policy = policyRepository.findByActionAndOrderStatus(
                        action, order.getOrderStatus())
                .orElseThrow(() -> new IllegalStateException(
                        "No refund policy configured for action " + action
                                + " and order status " + order.getOrderStatus() + "."));

        if (!policy.isEligible()) {
            return new PolicyDecision(false, policy.getMessage());
        }
        if (!"PAID".equalsIgnoreCase(order.getPayment().getStatus())) {
            return new PolicyDecision(false,
                    "Order " + order.getOrderNumber()
                            + " is not eligible because its payment status is "
                            + order.getPayment().getStatus() + "; payment must be PAID.");
        }
        if ((action == RefundPolicyAction.DELIVERED_REFUND
                || action == RefundPolicyAction.DELIVERED_REPLACEMENT)
                && !"DELIVERED".equalsIgnoreCase(order.getShipping().getStatus())) {
            return new PolicyDecision(false,
                    "Order " + order.getOrderNumber()
                            + " has order status DELIVERED but shipping status "
                            + order.getShipping().getStatus() + ".");
        }
        if (action == RefundPolicyAction.CANCEL_DELAYED_ORDER && order.hasShipped()) {
            return new PolicyDecision(false,
                    "Order " + order.getOrderNumber()
                            + " cannot be cancelled because it has already shipped.");
        }

        return new PolicyDecision(true, policy.getMessage());
    }
}
