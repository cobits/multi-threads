package com.cobits.concurrent;

import com.cobits.concurrent.executors.RunnableFixedThreadPool;
import com.cobits.concurrent.tasks.RunnableTask;

import java.util.Random;

public class ExecutorChecker {
	public static void main(String[] args) throws InterruptedException {
		final RunnableFixedThreadPool simpleFixedThreadPool = new RunnableFixedThreadPool(10);
		for (int i = 1; i <= 100; i++) {
			RunnableTask runnableTask = new RunnableTask("Task " + i);
			System.out.println("Created : " + runnableTask.getName());
			simpleFixedThreadPool.submit(runnableTask);
			if(i == 50){
				Thread.sleep(new Random().nextInt(1000));
				simpleFixedThreadPool.dumpStats();
				System.out.println("Pending tasks which were not executed: " + simpleFixedThreadPool.shutdownNow());
			}
		}
		Thread.sleep(1000);
		simpleFixedThreadPool.dumpStats();
	}
}
