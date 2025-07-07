package com.example.tasks.javacore.streamapi;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItem {
	private String productName;
	private int quantity;
	private double price;
	private Category category;
}
