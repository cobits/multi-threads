package com.cobits.concurrent;

import com.cobits.concurrent.executors.RunnableFixedThreadPool;
import com.cobits.concurrent.tasks.RunnableTask;

import java.util.Random;

public class ExecutorChecker {
	public static void main(String[] args) throws InterruptedException {
		final int THREAD_POOL_SIZE = 10;
		final int TASK_SIZE = 100;
		final RunnableFixedThreadPool simpleFixedThreadPool = new RunnableFixedThreadPool(THREAD_POOL_SIZE);
		for (int i = 1; i <= TASK_SIZE; i++) {
			final RunnableTask runnableTask = new RunnableTask("Task " + i);
			System.out.println("Created : " + runnableTask.getName());
			simpleFixedThreadPool.submit(runnableTask);
			if(i == TASK_SIZE / 2){ // Once we have submitted 50% of tasks, we call shutdown on our Executor
				Thread.sleep(new Random().nextInt(1000));// Random sleep so that we get different Stats below
				simpleFixedThreadPool.dumpStats();
				System.out.println("Pending tasks which were not executed: " + simpleFixedThreadPool.shutdownNow());
			}
		}
		Thread.sleep(1000);// Sleep and give time to check stats at a later point from Executor
		simpleFixedThreadPool.dumpStats();
	}
}
