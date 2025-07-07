package com.example.tasks.javacore.multithreading;

import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

class Factory implements Runnable {
	private final BlockingQueue<RobotPart> storage;
	private final int partsCreatedPerDay;
	private final int workingDays;
	private final CyclicBarrier barrier;

	public Factory(BlockingQueue<RobotPart> storage, CyclicBarrier barrier, int partsCreatedPerDay, int workingDays) {
		this.storage = storage;
		this.barrier = barrier;
		this.partsCreatedPerDay = partsCreatedPerDay;
		this.workingDays = workingDays;
	}

	@Override
	public void run() {
		Random random = new Random();
		for (int day = 1; day <= workingDays; day++) {
			int produced = random.nextInt(partsCreatedPerDay) + 1;
			for (int i = 0; i < produced; i++) {
				try {
					RobotPart part = RobotPart.getRandomPart();
					storage.put(part);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			System.out.println("Day " + day + ": Factory produced " + produced + " part(s)");
			try {
				barrier.await();
				Thread.sleep(100);
			} catch (InterruptedException | BrokenBarrierException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
