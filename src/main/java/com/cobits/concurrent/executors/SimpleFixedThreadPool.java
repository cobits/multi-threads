package com.cobits.concurrent.executors;



import com.cobits.concurrent.tasks.Task;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleFixedThreadPool {
	private boolean isActive;
	private final AtomicLong taskExecutedCount;
	private final AtomicLong taskSubmittedCount;
	private final WorkerThread[] workers;
	private final BlockingQueue<Runnable> queue;
	private final ReentrantLock mainLock = new ReentrantLock();

	public SimpleFixedThreadPool(final int poolSize)
	{
		this.isActive = true;
		this.taskExecutedCount = new AtomicLong(0);
		this.taskSubmittedCount = new AtomicLong(0);
		queue = new LinkedBlockingQueue<>();
		workers = new WorkerThread[poolSize];

		for (int i = 0; i < poolSize; i++) {
			workers[i] = new WorkerThread();
			workers[i].start();//Eager Start
		}
	}

	public void submit(final Task task){
		mainLock.lock();
		try {
			if(isActive) {
				taskSubmittedCount.getAndIncrement();
				queue.put(task);
			} else {
				System.out.println(task.getName() + " can not be accepted as the Executor is shutdown");
			}
		} catch (InterruptedException e) {
			System.err.println("Thread is interrupted due to an issue during put on queue: " + e.getMessage());
		} finally {
			mainLock.unlock();
		}
	}

	private class WorkerThread extends Thread {
		public void run() {
			try {
				while(true){
					final Runnable task = queue.take();
					task.run();
					taskExecutedCount.incrementAndGet();
				}
			} catch (InterruptedException e) {
				System.err.println("Thread is interrupted due to an issue during take on queue: " + e.getMessage());
			}
		}
	}

	public long getTasksExecutedCount() {
		return taskExecutedCount.get();
	}

	public long getTasksSubmittedCount() {
		return taskSubmittedCount.get();
	}

	public void shutdown() {
		System.out.println("Shutting down thread pool");
		mainLock.lock();
		try {
			this.isActive = false;
			for (WorkerThread w : workers) {
				if (!w.isInterrupted()) {
					w.interrupt();
				}
			}
		} finally {
			mainLock.unlock();
		}
	}
}
