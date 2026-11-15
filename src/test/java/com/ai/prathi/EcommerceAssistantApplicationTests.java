package com.ai.prathi;

import com.ai.prathi.repository.CustomerOrderRepository;
import com.ai.prathi.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "langchain4j.google-ai-gemini.chat-model.api-key=test")
class EcommerceAssistantApplicationTests {

	@Autowired
	private CustomerOrderRepository orderRepository;

	@Autowired
	private OrderService orderService;

	@Test
	void contextLoads() {
		var order = orderRepository.findByOrderNumber("ORD-1001").orElseThrow();
		assertThat(order.getTotalAmount()).isEqualByComparingTo("89.99");
		assertThat(order.getPayment().getStatus()).isEqualTo("PAID");
		assertThat(order.getShipping().getTrackingNumber()).isEqualTo("TRACK-DEMO-1001");
	}

	@Test
	void createsOrdersAndPreventsCancellationAfterShipping() {
		var created = orderService.createOrder("Casey Example", "casey@example.com", "Demo item",
				2, new BigDecimal("15.00"), "CREDIT_CARD", "Casey Example",
				"1 Demo Street", "Springfield", "CA", "90001", "US");

		assertThat(created.getTotalAmount()).isEqualByComparingTo("30.00");
		assertThat(created.getOrderStatus()).isEqualTo("PROCESSING");
		assertThat(orderService.cancelOrder(created.getOrderNumber()))
				.isEqualTo("Order " + created.getOrderNumber() + " was cancelled.");
		assertThat(orderRepository.findByOrderNumber(created.getOrderNumber()).orElseThrow().getOrderStatus())
				.isEqualTo("CANCELLED");
		assertThat(orderService.cancelOrder("ORD-1002"))
				.contains("cannot be cancelled because it has already shipped");
		assertThat(orderRepository.findByOrderNumber("ORD-1002").orElseThrow().getOrderStatus())
				.isEqualTo("SHIPPED");
	}

}
