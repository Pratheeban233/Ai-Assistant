package com.ai.prathi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentDetails {

    @Column(name = "payment_method", nullable = false, length = 40)
    private String method;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String status;

    @Column(name = "payment_reference", length = 80)
    private String transactionReference;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public PaymentDetails(String method, String status, String transactionReference, LocalDateTime paidAt) {
        this.method = method;
        this.status = status;
        this.transactionReference = transactionReference;
        this.paidAt = paidAt;
    }

    void markRefunded() {
        this.status = "REFUNDED";
    }

}
