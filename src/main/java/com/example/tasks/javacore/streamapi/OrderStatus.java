package com.example.tasks.javacore.streamapi;

import lombok.Getter;

@Getter
public enum OrderStatus {
	NEW, PROCESSING, SHIPPED, DELIVERED, CANCELLED
}
