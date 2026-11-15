package com.ai.prathi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "order_action_approvals", uniqueConstraints = {
        @UniqueConstraint(name = "uk_order_action_approval_id", columnNames = "approval_id")
})
public class OrderActionApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "approval_id", nullable = false, length = 36)
    private String approvalId;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 40)
    private RefundPolicyAction action;

    @Column(precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_status", nullable = false, length = 20)
    private ApprovalStatus approvalStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_status", nullable = false, length = 20)
    private ExecutionStatus executionStatus;

    @Column(name = "reviewer_name", length = 120)
    private String reviewerName;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "execution_result", length = 1000)
    private String executionResult;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    public OrderActionApproval(
            String orderNumber,
            RefundPolicyAction action,
            BigDecimal amount,
            String reason,
            LocalDateTime createdAt) {
        this.approvalId = UUID.randomUUID().toString();
        this.orderNumber = orderNumber;
        this.action = action;
        this.amount = amount;
        this.reason = reason;
        this.approvalStatus = ApprovalStatus.PENDING;
        this.executionStatus = ExecutionStatus.NOT_STARTED;
        this.createdAt = createdAt;
    }

    public void approve(String reviewerName, String decisionNote, LocalDateTime reviewedAt) {
        requirePending();
        this.approvalStatus = ApprovalStatus.APPROVED;
        this.reviewerName = reviewerName;
        this.decisionNote = decisionNote;
        this.reviewedAt = reviewedAt;
    }

    public void reject(String reviewerName, String decisionNote, LocalDateTime reviewedAt) {
        requirePending();
        this.approvalStatus = ApprovalStatus.REJECTED;
        this.reviewerName = reviewerName;
        this.decisionNote = decisionNote;
        this.reviewedAt = reviewedAt;
    }

    public void markExecutionSucceeded(String result, LocalDateTime executedAt) {
        this.executionStatus = ExecutionStatus.SUCCEEDED;
        this.executionResult = result;
        this.executedAt = executedAt;
    }

    public void markExecutionFailed(String result, LocalDateTime executedAt) {
        this.executionStatus = ExecutionStatus.FAILED;
        this.executionResult = result;
        this.executedAt = executedAt;
    }

    private void requirePending() {
        if (approvalStatus != ApprovalStatus.PENDING) {
            throw new IllegalStateException(
                    "Approval " + approvalId + " has already been reviewed.");
        }
    }
}
