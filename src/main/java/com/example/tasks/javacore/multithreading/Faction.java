package com.example.tasks.javacore.multithreading;

import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

@Getter
class Faction implements Runnable {
	private final String name;
	private final Map<RobotPart, Integer> partsGrabbed = new EnumMap<>(RobotPart.class);
	private final BlockingQueue<RobotPart> storage;
	private final CyclicBarrier barrier;
	private final int maxPartsPerNight;
	private final int workingDays;

	public Faction(String name, BlockingQueue<RobotPart> storage, CyclicBarrier barrier, int maxPartsPerNight, int workingDays) {
		this.name = name;
		this.storage = storage;
		this.barrier = barrier;
		this.maxPartsPerNight = maxPartsPerNight;
		this.workingDays = workingDays;
	}

	@Override
	public void run() {
		for (int day = 1; day <= workingDays; day++) {
			try {
				barrier.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				Thread.currentThread().interrupt();
			}
			int collectedParts = 0;
			while (collectedParts < maxPartsPerNight && !storage.isEmpty()) {
				RobotPart part = storage.poll();
				if (part != null) {
					partsGrabbed.put(part, partsGrabbed.getOrDefault(part, 0) + 1);
					collectedParts++;
				}
			}
			System.out.println(name + " collectedParts " + collectedParts + " part(s)");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		printStats();
	}


	public void printStats() {
		System.out.println();
		System.out.println(getName() + " collected: " + getPartsGrabbed());
		System.out.println(getName() + " built " + getRobotCount() + " robots.");
	}
	public int getRobotCount() {
		return partsGrabbed.values().stream().min(Integer::compareTo).orElse(0);
	}
}
