package com.example.tasks.javacore.streamapi;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
public class Customer {
	private String customerId;
	private String name;
	private String email;
	private LocalDateTime registeredAt;
	private int age;
	private String city;
}
