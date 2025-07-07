package com.example.tasks.javacore.streamapi;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderAnalyticsTest {

	private Customer customer1;
	private Customer customer2;
	private OrderItem itemA, itemB, itemC;
	private List<Order> orders;
	private static final StoreMetrics storeMetrics = new StoreMetrics();

	@BeforeEach
	void setUp() {
		customer1 = new Customer();
		customer1.setCustomerId("C001");
		customer1.setName("Alice");
		customer1.setCity("Moscow");
		customer1.setEmail("alice@example.com");
		customer1.setAge(30);
		customer1.setRegisteredAt(LocalDateTime.now().minusDays(100));

		customer2 = new Customer();
		customer2.setCustomerId("C002");
		customer2.setName("Bob");
		customer2.setCity("Berlin");
		customer2.setEmail("bob@example.com");
		customer2.setAge(40);
		customer2.setRegisteredAt(LocalDateTime.now().minusDays(50));

		itemA = new OrderItem();
		itemA.setProductName("Phone");
		itemA.setQuantity(2);
		itemA.setPrice(500.0);
		itemA.setCategory(Category.ELECTRONICS);

		itemB = new OrderItem();
		itemB.setProductName("Book");
		itemB.setQuantity(1);
		itemB.setPrice(100.0);
		itemB.setCategory(Category.BOOKS);

		itemC = new OrderItem();
		itemC.setProductName("Phone");
		itemC.setQuantity(3);
		itemC.setPrice(500.0);
		itemC.setCategory(Category.ELECTRONICS);

		orders = new ArrayList<>();
		orders.add(createOrder("O1", customer1, OrderStatus.DELIVERED, itemA, itemB));
		orders.add(createOrder("O2", customer1, OrderStatus.NEW, itemC));
		orders.add(createOrder("O3", customer2, OrderStatus.DELIVERED, itemB));
		orders.add(createOrder("O4", customer2, OrderStatus.CANCELLED, itemA));
	}

	private Order createOrder(String id, Customer customer, OrderStatus status, OrderItem... items) {
		Order order = new Order();
		order.setOrderId(id);
		order.setCustomer(customer);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(status);
		order.setItems(Arrays.asList(items));
		return order;
	}

	@Test
	void testUniqueCities() {
		List<String> uniqueCities = storeMetrics.getUniqueCities(orders);
		assertEquals(2, uniqueCities.size());
		assertTrue(uniqueCities.containsAll(List.of("Moscow", "Berlin")));
	}

	@Test
	void testTotalIncomeForDeliveredOrders() {
		double totalPrice = storeMetrics.getTotalPrice(orders);
		assertEquals(1200.0, totalPrice);
	}

	@Test
	void testMostPopularProduct() {
		String popularProduct = storeMetrics.getPopularProduct(orders);
		assertEquals("Phone", popularProduct);
	}

	@Test
	void testAverageCheckForDeliveredOrders() {
		double averageCheck = storeMetrics.getAverageCheck(orders);
		assertEquals(600.0, averageCheck);
	}

	@Test
	void testTopCustomersWithMoreThanFiveOrders() {
		for (int i = 0; i < 5; i++) {
			orders.add(createOrder("Extra" + i, customer1, OrderStatus.NEW, itemB));
		}
		List<Customer> topCustomers = storeMetrics.getTopNumberOfCustomer(orders, 5);
		assertEquals(1, topCustomers.size());
		assertEquals(customer1, topCustomers.get(0));
	}
}
