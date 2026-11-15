package com.ai.prathi.repository;

import com.ai.prathi.entity.RefundTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefundTransactionRepository extends JpaRepository<RefundTransaction, Long> {

    Optional<RefundTransaction> findByOrderNumber(String orderNumber);
}
