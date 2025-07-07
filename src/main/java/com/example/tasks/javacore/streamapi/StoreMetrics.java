package com.example.tasks.javacore.streamapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreMetrics {

	public List<String> getUniqueCities(List<Order> orders) {
		return orders.stream()
				.map(Order::getCustomer)
				.map(Customer::getCity)
				.distinct()
				.toList();
	}

	public double getTotalPrice(List<Order> orders) {
		return orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.flatMap(o -> o.getItems().stream())
				.mapToDouble(i -> i.getQuantity() * i.getPrice())
				.sum();
	}

	public String getPopularProduct(List<Order> orders) {
		return orders.stream()
				.flatMap(order -> order.getItems().stream())
				.collect(Collectors.toMap(
						OrderItem::getProductName,
						OrderItem::getQuantity,
						Integer::sum
				))
				.entrySet()
				.stream()
				.max(Map.Entry.comparingByValue())
				.map(Map.Entry::getKey)
				.orElse("No data");
	}

	public double getAverageCheck(List<Order> orders) {
		return orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.mapToDouble(o -> o.getItems().stream()
						.mapToDouble(i -> i.getQuantity() * i.getPrice())
						.sum()
				)
				.average()
				.orElse(0.0);
	}

	public List<Customer> getTopNumberOfCustomer(List<Order> orders, int topNumber) {
		return orders.stream()
				.collect(Collectors.groupingBy(
						Order::getCustomer,
						Collectors.counting()
				))
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue() > topNumber)
				.map(Map.Entry::getKey)
				.toList();
	}
}
