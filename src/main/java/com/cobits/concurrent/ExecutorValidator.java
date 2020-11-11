package com.cobits.concurrent;

import com.cobits.concurrent.executors.SimpleFixedThreadPool;
import com.cobits.concurrent.tasks.Task;

public class ExecutorValidator {
	public static void main(String[] args) throws InterruptedException {
		final SimpleFixedThreadPool simpleFixedThreadPool = new SimpleFixedThreadPool(10);
		for (int i = 1; i <= 100; i++) {
			Task task = new Task("Task " + i);
			System.out.println("Created : " + task.getName());
			simpleFixedThreadPool.submit(task);
			if(i == 50){
				System.out.println("Task submitted so far " + simpleFixedThreadPool.getTasksSubmittedCount());
				System.out.println("Task executed so far " + simpleFixedThreadPool.getTasksExecutedCount());
				simpleFixedThreadPool.shutdown();
			}
		}
		Thread.sleep(1000);
		System.out.println("Task submitted so far " + simpleFixedThreadPool.getTasksSubmittedCount());
		System.out.println("Task executed so far " + simpleFixedThreadPool.getTasksExecutedCount());
		System.out.println("Task discarded " + (simpleFixedThreadPool.getTasksSubmittedCount() -
				simpleFixedThreadPool.getTasksExecutedCount()));
	}
}
