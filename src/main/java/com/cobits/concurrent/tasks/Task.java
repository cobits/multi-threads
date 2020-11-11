package com.cobits.concurrent.tasks;

public class Task implements Runnable {
	private final String name;

	public Task(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void run() {
		try {
			Thread.sleep(100);
			System.out.println("Executing : " + name);
		} catch (InterruptedException e) {
			System.err.println(getName() + " was interrupted from sleep");
		}
	}
}
