package com.cobits.concurrent.tasks;

public class RunnableTask {
	private final String name;

	public RunnableTask(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void run() throws InterruptedException{
		try {
			System.out.println("Executing : " + name);
			Thread.sleep(100);
			System.out.println("Finished : " + name);
		} catch (InterruptedException e) {
			System.err.println(getName() + " was interrupted from sleep. Exiting without finishing.");
			throw new InterruptedException(getName() + " was interrupted from sleep. Exiting without finishing.");
		}
	}

	@Override
	public String toString() {
		return "RunnableTask{" +
				"name='" + name + '\'' +
				'}';
	}
}
