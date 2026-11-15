package com.ai.prathi.config;

import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.PaymentDetails;
import com.ai.prathi.entity.ShippingDetails;
import com.ai.prathi.repository.CustomerOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderDataInitializer implements CommandLineRunner {

    private final CustomerOrderRepository orderRepository;

    @Override
    public void run(String... args) {
        if (orderRepository.count() > 0) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        orderRepository.saveAll(List.of(
                new CustomerOrder(
                        "ORD-1001", "Alex Morgan", "alex.morgan@example.com",
                        "Wireless Headphones", 1, new BigDecimal("89.99"), "DELIVERED",
                        new PaymentDetails("CREDIT_CARD", "PAID", "PAY-DEMO-1001", now.minusDays(5)),
                        new ShippingDetails("Alex Morgan", "123 Example Street", "Apt 4",
                                "Springfield", "CA", "90001", "US", "DemoShip",
                                "TRACK-DEMO-1001", "DELIVERED", now.minusDays(4), now.minusDays(2)),
                        now.minusDays(5)),
                new CustomerOrder(
                        "ORD-1002", "Jamie Lee", "jamie.lee@example.com",
                        "Mechanical Keyboard", 2, new BigDecimal("74.50"), "SHIPPED",
                        new PaymentDetails("PAYPAL", "PAID", "PAY-DEMO-1002", now.minusDays(2)),
                        new ShippingDetails("Jamie Lee", "456 Sample Avenue", null,
                                "Portland", "OR", "97035", "US", "DemoShip",
                                "TRACK-DEMO-1002", "IN_TRANSIT", now.minusDays(1), null),
                        now.minusDays(2)),
                new CustomerOrder(
                        "ORD-1003", "Taylor Kim", "taylor.kim@example.com",
                        "USB-C Dock", 1, new BigDecimal("129.00"), "PROCESSING",
                        new PaymentDetails("CREDIT_CARD", "PENDING", null, null),
                        new ShippingDetails("Taylor Kim", "789 Demo Road", null,
                                "Austin", "TX", "73301", "US", null,
                                null, "NOT_SHIPPED", null, null),
                        now.minusHours(6))
        ));
    }
}
