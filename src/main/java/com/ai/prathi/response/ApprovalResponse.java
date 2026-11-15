package com.ai.prathi.response;

import com.ai.prathi.entity.ApprovalStatus;
import com.ai.prathi.entity.ExecutionStatus;
import com.ai.prathi.entity.RefundPolicyAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ApprovalResponse(
        String approvalId,
        String orderNumber,
        RefundPolicyAction action,
        BigDecimal amount,
        String reason,
        ApprovalStatus approvalStatus,
        ExecutionStatus executionStatus,
        String reviewerName,
        String decisionNote,
        String executionResult,
        LocalDateTime createdAt,
        LocalDateTime reviewedAt,
        LocalDateTime executedAt) {
}
