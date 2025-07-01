package com.example.tasks.javacore.multithreading;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;

public class RobotFactorySimulation {

	public static void main(String[] args) {
		BlockingQueue<RobotPart> storage = new LinkedBlockingQueue<>();
		CyclicBarrier barrier = new CyclicBarrier(3);

		Factory factory = new Factory(storage, barrier,10, 100);
		Faction faction1 = new Faction("World", storage, barrier, 5, 100);
		Faction faction2 = new Faction("Wednesday", storage, barrier, 5, 100);

		Thread factoryThread = new Thread(factory);
		Thread factionThread1 = new Thread(faction1);
		Thread factionThread2 = new Thread(faction2);


		factoryThread.start();
		factionThread1.start();
		factionThread2.start();
	}
}