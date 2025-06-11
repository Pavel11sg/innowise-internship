package com.example.tasks.javacore.streamapi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StoreMetrics {
	public static void main(String[] args) {

		List<Order> orders = new ArrayList<>();

		List<String> uniqueCities = orders.stream()
				.map(Order::getCustomer)
				.map(Customer::getCity)
				.distinct()
				.toList();

		double totalPrice = orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.flatMap(o -> o.getItems().stream())
				.mapToDouble(i -> i.getQuantity() * i.getPrice())
				.sum();

		String popularProduct = orders.stream()
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

		double averageCheck = orders.stream()
				.filter(o -> o.getStatus() == OrderStatus.DELIVERED)
				.mapToDouble(o -> o.getItems().stream()
						.mapToDouble(i -> i.getQuantity() * i.getPrice())
						.sum()
				)
				.average()
				.orElse(0.0);

		List<Customer> topCustomers = orders.stream()
				.collect(Collectors.groupingBy(
						Order::getCustomer,
						Collectors.counting()
				))
				.entrySet()
				.stream()
				.filter(entry -> entry.getValue() > 5)
				.map(Map.Entry::getKey)
				.toList();
	}
}
