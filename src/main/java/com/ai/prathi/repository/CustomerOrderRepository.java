package com.ai.prathi.repository;

import com.ai.prathi.entity.CustomerOrder;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select order from CustomerOrder order where order.orderNumber = :orderNumber")
    Optional<CustomerOrder> findByOrderNumberForUpdate(@Param("orderNumber") String orderNumber);
}
