package com.ai.prathi.service;

import com.ai.prathi.entity.ApprovalStatus;
import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.ExecutionStatus;
import com.ai.prathi.entity.OrderActionApproval;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.repository.CustomerOrderRepository;
import com.ai.prathi.repository.OrderActionApprovalRepository;
import com.ai.prathi.repository.ReplacementRequestRepository;
import com.ai.prathi.repository.RefundTransactionRepository;
import com.ai.prathi.response.ApprovalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderActionApprovalService {

    private static final int MAX_REASON_LENGTH = 500;

    private final CustomerOrderRepository orderRepository;
    private final OrderActionApprovalRepository approvalRepository;
    private final RefundTransactionRepository refundRepository;
    private final ReplacementRequestRepository replacementRepository;
    private final RefundPolicyService policyService;
    private final RefundService refundService;
    private final OrderService orderService;
    private final ReplacementService replacementService;

    @Transactional
    public String requestRefundApproval(String orderNumber, String reason) {
        validateRequest(orderNumber, reason, "Refund");
        CustomerOrder order = findOrder(orderNumber, "Refund");
        RefundPolicyAction action = refundAction(order);

        if (refundRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "Refund was already recorded for order " + orderNumber + ".";
        }
        if (replacementRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "Refund rejected: a replacement request is already recorded for order "
                    + orderNumber + ".";
        }

        var decision = policyService.evaluate(order, action);
        if (!decision.eligible()) {
            return "Refund rejected: " + decision.message();
        }
        return createApproval(order, action, reason);
    }

    @Transactional
    public String requestDelayedCancellationApproval(String orderNumber) {
        CustomerOrder order = findOrder(orderNumber, "Cancellation");
        RefundPolicyAction action = RefundPolicyAction.CANCEL_DELAYED_ORDER;
        var decision = policyService.evaluate(order, action);
        if (!decision.eligible()) {
            return "Cancellation rejected: " + decision.message();
        }
        return createApproval(order, action, "Customer requested cancellation.");
    }

    @Transactional
    public String requestCancellationApproval(String orderNumber) {
        CustomerOrder order = findOrder(orderNumber, "Cancellation");
        if ("REFUNDED".equalsIgnoreCase(order.getPayment().getStatus())) {
            return "Order " + orderNumber + " cannot be cancelled because it has been refunded.";
        }
        if (order.hasShipped()) {
            return "Order " + orderNumber + " cannot be cancelled because it has already shipped.";
        }
        if ("CANCELLED".equalsIgnoreCase(order.getOrderStatus())) {
            return "Order " + orderNumber + " is already cancelled.";
        }
        return createApproval(
                order, RefundPolicyAction.ORDER_CANCELLATION, "Customer requested cancellation.");
    }

    @Transactional
    public String requestReplacementApproval(String orderNumber, String reason) {
        validateRequest(orderNumber, reason, "Replacement");
        CustomerOrder order = findOrder(orderNumber, "Replacement");
        if (replacementRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "A replacement request already exists for order " + orderNumber + ".";
        }
        if (refundRepository.findByOrderNumber(orderNumber).isPresent()) {
            return "Replacement rejected: a refund is already recorded for order "
                    + orderNumber + ".";
        }

        RefundPolicyAction action = RefundPolicyAction.DELIVERED_REPLACEMENT;
        var decision = policyService.evaluate(order, action);
        if (!decision.eligible()) {
            return "Replacement rejected: " + decision.message();
        }
        return createApproval(order, action, reason);
    }

    @Transactional(readOnly = true)
    public ApprovalResponse getApproval(String approvalId) {
        return toResponse(findApproval(approvalId));
    }

    @Transactional(readOnly = true)
    public List<ApprovalResponse> getPendingApprovals() {
        return approvalRepository
                .findAllByApprovalStatusOrderByCreatedAtAsc(ApprovalStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ApprovalResponse approve(String approvalId, String reviewerName, String note) {
        OrderActionApproval approval = findApprovalForUpdate(approvalId);
        requirePending(approval);
        findOrder(approval.getOrderNumber(), "Approval");
        approval.approve(reviewerName.trim(), normalizeNote(note), LocalDateTime.now());

        String result = execute(approval);
        if (isSuccessful(approval.getAction(), result)) {
            approval.markExecutionSucceeded(result, LocalDateTime.now());
        } else {
            approval.markExecutionFailed(result, LocalDateTime.now());
        }
        return toResponse(approvalRepository.save(approval));
    }

    @Transactional
    public ApprovalResponse reject(String approvalId, String reviewerName, String note) {
        OrderActionApproval approval = findApprovalForUpdate(approvalId);
        requirePending(approval);
        approval.reject(reviewerName.trim(), normalizeNote(note), LocalDateTime.now());
        return toResponse(approvalRepository.save(approval));
    }

    private String execute(OrderActionApproval approval) {
        return switch (approval.getAction()) {
            case DELAY_REFUND, DELIVERED_REFUND ->
                    refundService.issueRefund(approval.getOrderNumber(), approval.getReason());
            case CANCEL_DELAYED_ORDER ->
                    orderService.cancelDelayedPaidOrder(approval.getOrderNumber());
            case ORDER_CANCELLATION ->
                    orderService.cancelOrder(approval.getOrderNumber());
            case DELIVERED_REPLACEMENT ->
                    replacementService.requestReplacement(
                            approval.getOrderNumber(), approval.getReason());
        };
    }

    private boolean isSuccessful(RefundPolicyAction action, String result) {
        return switch (action) {
            case DELAY_REFUND, DELIVERED_REFUND -> result.startsWith("Simulated refund recorded");
            case CANCEL_DELAYED_ORDER, ORDER_CANCELLATION ->
                    result.startsWith("Order ") && result.contains(" was cancelled.");
            case DELIVERED_REPLACEMENT -> result.startsWith("Replacement request recorded");
        };
    }

    private String createApproval(
            CustomerOrder order, RefundPolicyAction action, String reason) {
        var existing = approvalRepository.findByOrderNumberAndActionAndApprovalStatus(
                order.getOrderNumber(), action, ApprovalStatus.PENDING);
        if (existing.isPresent()) {
            return pendingMessage(existing.get());
        }

        OrderActionApproval approval = approvalRepository.save(new OrderActionApproval(
                order.getOrderNumber(),
                action,
                action == RefundPolicyAction.DELAY_REFUND
                        || action == RefundPolicyAction.DELIVERED_REFUND
                        ? order.getTotalAmount()
                        : null,
                reason.trim(),
                LocalDateTime.now()));
        return pendingMessage(approval);
    }

    private String pendingMessage(OrderActionApproval approval) {
        return "approvalStatus=PENDING; executionStatus=NOT_STARTED; "
                + approval.getAction() + " for order " + approval.getOrderNumber()
                + " is eligible and awaiting human approval. Approval ID: "
                + approval.getApprovalId()
                + (approval.getAmount() == null ? "." : "; amount: " + approval.getAmount() + ".");
    }

    private RefundPolicyAction refundAction(CustomerOrder order) {
        return "DELAYED".equalsIgnoreCase(order.getOrderStatus())
                ? RefundPolicyAction.DELAY_REFUND
                : RefundPolicyAction.DELIVERED_REFUND;
    }

    private CustomerOrder findOrder(String orderNumber, String actionName) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException(actionName + " requires an order number.");
        }
        return orderRepository.findByOrderNumberForUpdate(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException(
                        actionName + " rejected: order " + orderNumber + " was not found."));
    }

    private void validateRequest(String orderNumber, String reason, String actionName) {
        if (orderNumber == null || orderNumber.isBlank()) {
            throw new IllegalArgumentException(actionName + " requires an order number.");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(actionName + " requires a reason.");
        }
        if (reason.length() > MAX_REASON_LENGTH) {
            throw new IllegalArgumentException(
                    actionName + " reason must be 500 characters or fewer.");
        }
    }

    private OrderActionApproval findApproval(String approvalId) {
        if (approvalId == null || approvalId.isBlank()) {
            throw new IllegalArgumentException("Approval ID is required.");
        }
        return approvalRepository.findByApprovalId(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval " + approvalId + " was not found."));
    }

    private OrderActionApproval findApprovalForUpdate(String approvalId) {
        if (approvalId == null || approvalId.isBlank()) {
            throw new IllegalArgumentException("Approval ID is required.");
        }
        return approvalRepository.findByApprovalIdForUpdate(approvalId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Approval " + approvalId + " was not found."));
    }

    private void requirePending(OrderActionApproval approval) {
        if (approval.getApprovalStatus() != ApprovalStatus.PENDING) {
            throw new ApprovalConflictException(
                    "Approval " + approval.getApprovalId() + " has already been reviewed.");
        }
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }

    private ApprovalResponse toResponse(OrderActionApproval approval) {
        return new ApprovalResponse(
                approval.getApprovalId(),
                approval.getOrderNumber(),
                approval.getAction(),
                approval.getAmount(),
                approval.getReason(),
                approval.getApprovalStatus(),
                approval.getExecutionStatus(),
                approval.getReviewerName(),
                approval.getDecisionNote(),
                approval.getExecutionResult(),
                approval.getCreatedAt(),
                approval.getReviewedAt(),
                approval.getExecutedAt());
    }
}
