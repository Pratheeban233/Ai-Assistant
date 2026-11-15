package com.ai.prathi.config;

import com.ai.prathi.entity.RefundPolicy;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.repository.RefundPolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RefundPolicyDataInitializer implements CommandLineRunner {

    private static final List<String> ORDER_STATUSES =
            List.of("DELIVERED", "SHIPPED", "PROCESSING", "DELAYED", "CANCELLED");

    private final RefundPolicyRepository policyRepository;

    @Override
    public void run(String... args) {
        if (policyRepository.count() > 0) {
            return;
        }

        List<RefundPolicy> policies = new ArrayList<>();
        addPolicies(policies, RefundPolicyAction.DELAY_REFUND,
                Map.of("DELAYED", "Delayed orders are eligible for a refund."),
                Map.of(
                        "DELIVERED", "Delivered orders are not eligible for a delay refund.",
                        "SHIPPED", "The order is already shipped. Wait until the promised delivery date.",
                        "PROCESSING", "Processing orders are not eligible for a delay refund.",
                        "CANCELLED", "Cancelled orders are already closed."));
        addPolicies(policies, RefundPolicyAction.CANCEL_DELAYED_ORDER,
                Map.of("DELAYED", "A paid delayed order can be cancelled if it has not shipped."),
                Map.of(
                        "DELIVERED", "Delivered orders cannot be cancelled.",
                        "SHIPPED", "The order is already shipped and cannot be cancelled.",
                        "PROCESSING", "Only delayed orders can be cancelled by this workflow.",
                        "CANCELLED", "Cancelled orders are already closed."));
        addPolicies(policies, RefundPolicyAction.DELIVERED_REFUND,
                Map.of("DELIVERED", "Paid delivered orders are eligible for a standard refund."),
                Map.of(
                        "DELAYED", "Delayed orders use the delay-refund policy.",
                        "SHIPPED", "Shipped orders are not eligible for a delivered-order refund.",
                        "PROCESSING", "Processing orders are not eligible for a delivered-order refund.",
                        "CANCELLED", "Cancelled orders are already closed."));
        addPolicies(policies, RefundPolicyAction.DELIVERED_REPLACEMENT,
                Map.of("DELIVERED", "Paid delivered orders are eligible for a replacement request."),
                Map.of(
                        "DELAYED", "Only delivered orders are eligible for a replacement request.",
                        "SHIPPED", "Shipped orders are not eligible for a replacement request.",
                        "PROCESSING", "Processing orders are not eligible for a replacement request.",
                        "CANCELLED", "Cancelled orders are already closed."));

        policyRepository.saveAll(policies);
    }

    private void addPolicies(
            List<RefundPolicy> policies,
            RefundPolicyAction action,
            Map<String, String> eligibleMessages,
            Map<String, String> ineligibleMessages) {
        Set<String> eligibleStatuses = eligibleMessages.keySet();
        ORDER_STATUSES.forEach(status -> {
            boolean eligible = eligibleStatuses.contains(status);
            String message = eligible
                    ? eligibleMessages.get(status)
                    : ineligibleMessages.get(status);
            policies.add(new RefundPolicy(action, status, eligible, message));
        });
    }
}
