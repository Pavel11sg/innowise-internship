package com.example.tasks.javacore.multithreading;

import java.util.Random;

public enum RobotPart {
	HEAD, TORSO, HAND, FEET;
	private static final RobotPart[] VALUES = values();
	private static final Random RANDOM = new Random();

	public static RobotPart getRandomPart() {
		return VALUES[RANDOM.nextInt(VALUES.length)];
	}
}
