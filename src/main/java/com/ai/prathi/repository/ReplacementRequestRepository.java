package com.ai.prathi.repository;

import com.ai.prathi.entity.ReplacementRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplacementRequestRepository extends JpaRepository<ReplacementRequest, Long> {

    Optional<ReplacementRequest> findByOrderNumber(String orderNumber);
}
