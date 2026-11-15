package com.ai.prathi.repository;

import com.ai.prathi.entity.ApprovalStatus;
import com.ai.prathi.entity.OrderActionApproval;
import com.ai.prathi.entity.RefundPolicyAction;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderActionApprovalRepository extends JpaRepository<OrderActionApproval, Long> {

    Optional<OrderActionApproval> findByApprovalId(String approvalId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select approval from OrderActionApproval approval where approval.approvalId = :approvalId")
    Optional<OrderActionApproval> findByApprovalIdForUpdate(@Param("approvalId") String approvalId);

    Optional<OrderActionApproval> findByOrderNumberAndActionAndApprovalStatus(
            String orderNumber, RefundPolicyAction action, ApprovalStatus approvalStatus);

    List<OrderActionApproval> findAllByApprovalStatusOrderByCreatedAtAsc(
            ApprovalStatus approvalStatus);
}
