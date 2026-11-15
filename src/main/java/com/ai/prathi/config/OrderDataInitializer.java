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
        LocalDateTime now = LocalDateTime.now();
        List<CustomerOrder> sampleOrders = List.of(
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
                        now.minusHours(6)),
                new CustomerOrder(
                        "ORD-1004", "Morgan Patel", "morgan.patel@example.com",
                        "Smart Speaker", 1, new BigDecimal("59.99"), "DELAYED",
                        new PaymentDetails("CREDIT_CARD", "PAID", "PAY-DEMO-1004", now.minusDays(3)),
                        new ShippingDetails("Morgan Patel", "101 Example Lane", null,
                                "Denver", "CO", "80014", "US", null,
                                null, "DELAYED", null, null),
                        now.minusDays(3)),
                new CustomerOrder(
                        "ORD-1005", "Riley Chen", "riley.chen@example.com",
                        "Portable Charger", 1, new BigDecimal("39.95"), "DELAYED",
                        new PaymentDetails("CREDIT_CARD", "PENDING", null, null),
                        new ShippingDetails("Riley Chen", "202 Sample Street", null,
                                "Seattle", "WA", "98101", "US", null,
                                null, "DELAYED", null, null),
                        now.minusDays(2)),
                new CustomerOrder(
                        "ORD-1006", "Jordan Rivera", "jordan.rivera@example.com",
                        "Bluetooth Speaker", 1, new BigDecimal("64.99"), "DELAYED",
                        new PaymentDetails("PAYPAL", "PAID", "PAY-DEMO-1006", now.minusDays(4)),
                        new ShippingDetails("Jordan Rivera", "303 Sample Avenue", null,
                                "Phoenix", "AZ", "85001", "US", "DemoShip",
                                "TRACK-DEMO-1006", "IN_TRANSIT", now.minusDays(3), null),
                        now.minusDays(4)),
                new CustomerOrder(
                        "ORD-1007", "Casey Brooks", "casey.brooks@example.com",
                        "Desk Lamp", 1, new BigDecimal("48.00"), "DELIVERED",
                        new PaymentDetails("CREDIT_CARD", "PENDING", null, null),
                        new ShippingDetails("Casey Brooks", "404 Example Road", null,
                                "Chicago", "IL", "60601", "US", "DemoShip",
                                "TRACK-DEMO-1007", "DELIVERED", now.minusDays(2), now.minusDays(1)),
                        now.minusDays(3)),
                new CustomerOrder(
                        "ORD-1008", "Sam Taylor", "sam.taylor@example.com",
                        "Wireless Mouse", 2, new BigDecimal("24.50"), "PROCESSING",
                        new PaymentDetails("CREDIT_CARD", "PAID", "PAY-DEMO-1008", now.minusHours(2)),
                        new ShippingDetails("Sam Taylor", "505 Demo Boulevard", null,
                                "Boston", "MA", "02108", "US", null,
                                null, "NOT_SHIPPED", null, null),
                        now.minusHours(3))
        );

        sampleOrders.stream()
                .filter(order -> orderRepository.findByOrderNumber(order.getOrderNumber()).isEmpty())
                .forEach(orderRepository::save);
    }
}
