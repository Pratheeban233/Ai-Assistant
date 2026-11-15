package com.ai.prathi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "customer_orders", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customer_orders_order_number", columnNames = "order_number")
})
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Column(name = "customer_email", nullable = false, length = 180)
    private String customerEmail;

    @Column(name = "product_name", nullable = false, length = 180)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Embedded
    private PaymentDetails payment;

    @Embedded
    private ShippingDetails shipping;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CustomerOrder(String orderNumber, String customerName, String customerEmail,
                         String productName, int quantity, BigDecimal unitPrice,
                         String orderStatus, PaymentDetails payment, ShippingDetails shipping,
                         LocalDateTime createdAt) {
        this.orderNumber = orderNumber;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = unitPrice.multiply(BigDecimal.valueOf(quantity));
        this.orderStatus = orderStatus;
        this.payment = payment;
        this.shipping = shipping;
        this.createdAt = createdAt;
    }

    public void cancel() {
        if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException("Order " + orderNumber + " cannot be cancelled because it has been refunded.");
        }
        if (hasShipped()) {
            throw new IllegalStateException("Order " + orderNumber + " cannot be cancelled because it has already shipped.");
        }
        if ("CANCELLED".equalsIgnoreCase(orderStatus)) {
            throw new IllegalStateException("Order " + orderNumber + " is already cancelled.");
        }
        this.orderStatus = "CANCELLED";
    }

    public void markRefunded() {
        if (!"PAID".equalsIgnoreCase(payment.getStatus())) {
            throw new IllegalStateException("Only paid orders can be refunded.");
        }
        boolean delayed = "DELAYED".equalsIgnoreCase(orderStatus);
        boolean delivered = "DELIVERED".equalsIgnoreCase(orderStatus)
                && "DELIVERED".equalsIgnoreCase(shipping.getStatus());
        if (!delayed && !delivered) {
            throw new IllegalStateException("Only paid delayed or delivered orders can be refunded.");
        }
        payment.markRefunded();
    }

    public boolean hasShipped() {
        return shipping.getShippedAt() != null
                || "SHIPPED".equalsIgnoreCase(orderStatus)
                || "IN_TRANSIT".equalsIgnoreCase(orderStatus)
                || "DELIVERED".equalsIgnoreCase(orderStatus)
                || "SHIPPED".equalsIgnoreCase(shipping.getStatus())
                || "IN_TRANSIT".equalsIgnoreCase(shipping.getStatus())
                || "DELIVERED".equalsIgnoreCase(shipping.getStatus());
    }
}
