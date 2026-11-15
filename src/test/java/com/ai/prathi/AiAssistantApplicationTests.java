package com.ai.prathi;

import com.ai.prathi.agent.workflow.WorkflowSupportAgent;
import com.ai.prathi.entity.CustomerOrder;
import com.ai.prathi.entity.PaymentDetails;
import com.ai.prathi.entity.ShippingDetails;
import com.ai.prathi.repository.CustomerOrderRepository;
import com.ai.prathi.repository.RefundPolicyRepository;
import com.ai.prathi.repository.ReplacementRequestRepository;
import com.ai.prathi.repository.RefundTransactionRepository;
import com.ai.prathi.entity.RefundPolicyAction;
import com.ai.prathi.service.OrderService;
import com.ai.prathi.service.ReplacementService;
import com.ai.prathi.service.RefundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = "langchain4j.google-ai-gemini.chat-model.api-key=test")
class AiAssistantApplicationTests {

	@Autowired
	private CustomerOrderRepository orderRepository;

	@Autowired
	private OrderService orderService;

	@Autowired
	private RefundService refundService;

	@Autowired
	private RefundTransactionRepository refundRepository;

	@Autowired
	private RefundPolicyRepository refundPolicyRepository;

	@Autowired
	private ReplacementRequestRepository replacementRepository;

	@Autowired
	private ReplacementService replacementService;

	@Autowired
	private WorkflowSupportAgent workflowSupportAgent;

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
		assertThat(orderService.cancelDelayedPaidOrder("ORD-1002"))
				.contains("cannot be cancelled");
		assertThat(orderService.cancelDelayedPaidOrder("ORD-1003"))
				.contains("Only delayed orders can be cancelled");
		assertThat(orderRepository.findByOrderNumber("ORD-1002").orElseThrow().getOrderStatus())
				.isEqualTo("SHIPPED");
	}

	@Test
	void configuresRefundAgenticWorkflow() {
		assertThat(workflowSupportAgent).isNotNull();
	}

	@Test
	void recordsRefundOnlyForEligiblePaidOrdersAndPreventsDuplicates() {
		String orderNumber = "ORD-REFUND-" + UUID.randomUUID().toString().substring(0, 8);
		LocalDateTime now = LocalDateTime.now();
		orderRepository.save(new CustomerOrder(
				orderNumber,
				"Refund Customer",
				"refund@example.com",
				"Refund Test Product",
				1,
				new BigDecimal("25.00"),
				"DELIVERED",
				new PaymentDetails("CREDIT_CARD", "PAID", "PAY-REFUND-TEST", now),
				new ShippingDetails("Refund Customer", "1 Test Street", null,
						"Test City", "CA", "90001", "US", "DemoShip",
						"TRACK-REFUND-TEST", "DELIVERED", now.minusDays(2), now.minusDays(1)),
				now));

		assertThat(refundService.issueRefund(orderNumber, "Product arrived damaged"))
				.contains("Simulated refund recorded");
		assertThat(refundService.issueRefund(orderNumber, "Duplicate request"))
				.contains("already recorded");
		assertThat(orderRepository.findByOrderNumber(orderNumber).orElseThrow()
				.getPayment().getStatus()).isEqualTo("REFUNDED");
		assertThat(refundRepository.findByOrderNumber(orderNumber)).isPresent();

		assertThat(refundService.issueRefund("ORD-1002", "Arrived damaged"))
				.contains("not eligible");
		assertThat(refundRepository.findByOrderNumber("ORD-1002")).isEmpty();
		assertThat(refundService.issueRefund("ORD-1003", "Payment issue"))
				.contains("not eligible");
		assertThat(refundRepository.findByOrderNumber("ORD-1003")).isEmpty();

		String delayedOrderNumber = saveDelayedPaidOrder();
		assertThat(refundService.issueRefund(delayedOrderNumber, "Delivery was delayed"))
				.contains("Simulated refund recorded");
		assertThat(refundRepository.findByOrderNumber(delayedOrderNumber)).isPresent();
		assertThat(refundService.issueRefund(saveDelayedOrder("PENDING"), "Delivery was delayed"))
				.contains("payment status is PENDING");
	}

	@Test
	void recordsReplacementRequestOnlyForPaidDeliveredOrders() {
		assertThat(replacementService.requestReplacement("ORD-1001", "Item arrived damaged"))
				.contains("request recorded");
		assertThat(replacementService.requestReplacement("ORD-1001", "Duplicate request"))
				.contains("already exists");
		assertThat(replacementRepository.findByOrderNumber("ORD-1001"))
				.get()
				.extracting("status")
				.isEqualTo("REQUESTED");
		assertThat(refundService.issueRefund("ORD-1001", "Also refund it"))
				.contains("replacement request is already recorded");

		assertThat(replacementService.requestReplacement("ORD-1002", "Item damaged"))
				.contains("not eligible");
		assertThat(replacementRepository.findByOrderNumber("ORD-1002")).isEmpty();
	}

	@Test
	void rejectsDeliveredActionsWhenShippingStatusIsNotDelivered() {
		String orderNumber = "ORD-SHIP-" + UUID.randomUUID().toString().substring(0, 8);
		LocalDateTime now = LocalDateTime.now();
		orderRepository.save(new CustomerOrder(
				orderNumber,
				"Shipping Check Customer",
				"shipping-check@example.com",
				"Shipping Check Product",
				1,
				new BigDecimal("25.00"),
				"DELIVERED",
				new PaymentDetails("CREDIT_CARD", "PAID", "PAY-SHIP-CHECK", now),
				new ShippingDetails("Shipping Check Customer", "1 Test Street", null,
						"Test City", "CA", "90001", "US", "DemoShip",
						"TRACK-SHIP-CHECK", "SHIPPED", now.minusDays(1), null),
				now));

		assertThat(refundService.issueRefund(orderNumber, "Item arrived damaged"))
				.contains("shipping status SHIPPED");
		assertThat(replacementService.requestReplacement(orderNumber, "Item arrived damaged"))
				.contains("shipping status SHIPPED");
	}

	@Test
	void seedsDelayRefundPolicyForEveryOrderStatus() {
		var delivered = refundPolicyRepository.findByActionAndOrderStatus(
				RefundPolicyAction.DELAY_REFUND, "DELIVERED").orElseThrow();
		var shipped = refundPolicyRepository.findByActionAndOrderStatus(
				RefundPolicyAction.DELAY_REFUND, "SHIPPED").orElseThrow();
		var processing = refundPolicyRepository.findByActionAndOrderStatus(
				RefundPolicyAction.DELAY_REFUND, "PROCESSING").orElseThrow();
		var delayed = refundPolicyRepository.findByActionAndOrderStatus(
				RefundPolicyAction.DELAY_REFUND, "DELAYED").orElseThrow();
		var cancelled = refundPolicyRepository.findByActionAndOrderStatus(
				RefundPolicyAction.DELAY_REFUND, "CANCELLED").orElseThrow();

		assertThat(delivered.isEligible()).isFalse();
		assertThat(delivered.getMessage())
				.isEqualTo("Delivered orders are not eligible for a delay refund.");
		assertThat(shipped.isEligible()).isFalse();
		assertThat(shipped.getMessage())
				.isEqualTo("The order is already shipped. Wait until the promised delivery date.");
		assertThat(processing.isEligible()).isFalse();
		assertThat(processing.getMessage())
				.isEqualTo("Processing orders are not eligible for a delay refund.");
		assertThat(delayed.isEligible()).isTrue();
		assertThat(delayed.getMessage()).isEqualTo("Delayed orders are eligible for a refund.");
		assertThat(cancelled.isEligible()).isFalse();
		assertThat(cancelled.getMessage()).isEqualTo("Cancelled orders are already closed.");
	}

	@Test
	void allowsCancellationOfPaidDelayedOrderBeforeShipment() {
		String delayedOrderNumber = saveDelayedPaidOrder();
		assertThat(orderService.cancelDelayedPaidOrder(delayedOrderNumber))
				.isEqualTo("Order " + delayedOrderNumber + " was cancelled.");
		assertThat(orderRepository.findByOrderNumber(delayedOrderNumber).orElseThrow().getOrderStatus())
				.isEqualTo("CANCELLED");
	}

	private String saveDelayedPaidOrder() {
		return saveDelayedOrder("PAID");
	}

	private String saveDelayedOrder(String paymentStatus) {
		String orderNumber = "ORD-DEL-" + UUID.randomUUID().toString().substring(0, 8);
		LocalDateTime now = LocalDateTime.now();
		orderRepository.save(new CustomerOrder(
				orderNumber,
				"Delayed Customer",
				"delayed@example.com",
				"Delayed Test Product",
				1,
				new BigDecimal("25.00"),
				"DELAYED",
				new PaymentDetails(
						"CREDIT_CARD",
						paymentStatus,
						"PAID".equals(paymentStatus) ? "PAY-DEL-TEST" : null,
						"PAID".equals(paymentStatus) ? now : null),
				new ShippingDetails("Delayed Customer", "1 Test Street", null,
						"Test City", "CA", "90001", "US", null,
						null, "DELAYED", null, null),
				now));
		return orderNumber;
	}

}
