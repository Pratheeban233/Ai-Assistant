package com.ai.prathi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "replacement_requests", uniqueConstraints = {
        @UniqueConstraint(name = "uk_replacement_requests_order_number", columnNames = "order_number")
})
public class ReplacementRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ReplacementRequest(String orderNumber, String reason, LocalDateTime createdAt) {
        this.orderNumber = orderNumber;
        this.reason = reason;
        this.status = "REQUESTED";
        this.createdAt = createdAt;
    }
}
