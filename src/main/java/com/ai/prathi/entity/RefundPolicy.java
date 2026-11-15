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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "refund_policy", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_refund_policy_action_status",
                columnNames = {"action_type", "order_status"})
})
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 40)
    private RefundPolicyAction action;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Column(nullable = false)
    private boolean eligible;

    @Column(nullable = false, length = 300)
    private String message;

    public RefundPolicy(
            RefundPolicyAction action, String orderStatus, boolean eligible, String message) {
        this.action = action;
        this.orderStatus = orderStatus;
        this.eligible = eligible;
        this.message = message;
    }
}
