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
public class ShippingDetails {

    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Column(name = "shipping_address_line1", nullable = false, length = 180)
    private String addressLine1;

    @Column(name = "shipping_address_line2", length = 180)
    private String addressLine2;

    @Column(name = "shipping_city", nullable = false, length = 100)
    private String city;

    @Column(name = "shipping_region", length = 100)
    private String region;

    @Column(name = "shipping_postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(name = "shipping_country", nullable = false, length = 80)
    private String country;

    @Column(name = "shipping_carrier", length = 80)
    private String carrier;

    @Column(name = "tracking_number", length = 80)
    private String trackingNumber;

    @Column(name = "shipping_status", nullable = false, length = 30)
    private String status;

    @Column(name = "shipped_at")
    private LocalDateTime shippedAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    public ShippingDetails(String recipientName, String addressLine1, String addressLine2,
                           String city, String region, String postalCode, String country,
                           String carrier, String trackingNumber, String status,
                           LocalDateTime shippedAt, LocalDateTime deliveredAt) {
        this.recipientName = recipientName;
        this.addressLine1 = addressLine1;
        this.addressLine2 = addressLine2;
        this.city = city;
        this.region = region;
        this.postalCode = postalCode;
        this.country = country;
        this.carrier = carrier;
        this.trackingNumber = trackingNumber;
        this.status = status;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

}
