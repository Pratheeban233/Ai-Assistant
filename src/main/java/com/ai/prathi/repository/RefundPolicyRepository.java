package com.ai.prathi.repository;

import com.ai.prathi.entity.RefundPolicy;
import com.ai.prathi.entity.RefundPolicyAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    Optional<RefundPolicy> findByActionAndOrderStatus(
            RefundPolicyAction action, String orderStatus);
}
