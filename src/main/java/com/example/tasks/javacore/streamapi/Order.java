package com.example.tasks.javacore.streamapi;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class Order {
	private String orderId;
	private LocalDateTime orderDate;
	private Customer customer;
	private List<OrderItem> items;
	private OrderStatus status;
}
