package com.cobits.concurrent.executors;

import com.cobits.concurrent.tasks.RunnableTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class RunnableFixedThreadPool {
	private static final int DEFAULT_POOL_SIZE = 10;
	private final AtomicLong taskInitiatedCount = new AtomicLong(0);
	private final AtomicLong taskSubmittedCount = new AtomicLong(0);
	private final AtomicLong taskRejectedCount = new AtomicLong(0);
	private final BlockingQueue<RunnableTask> queue = new LinkedBlockingQueue<>();
	private final ReentrantLock mainLock = new ReentrantLock();
	private boolean isActive = true;
	private WorkerThread[] workers;

	public RunnableFixedThreadPool(){
		this(DEFAULT_POOL_SIZE);
	}

	public RunnableFixedThreadPool(final int poolSize)
	{
		workers = new WorkerThread[poolSize];
		for (int i = 0; i < poolSize; i++) {
			workers[i] = new WorkerThread(i);
			workers[i].start();//Eager Start
		}
	}

	private class WorkerThread extends Thread {
		private int workerId;
		private AtomicBoolean workerRunning;

		public WorkerThread(int workerId){
			this.workerId = workerId;
			this.workerRunning = new AtomicBoolean(false);
		}


		public void run() {
			workerRunning.set(true);
			try {
				while(workerRunning.get()){
					final RunnableTask task = queue.take();
					taskInitiatedCount.incrementAndGet();
					System.out.println("Scheduling " + task.getName() + " on Worker Thread " + workerId);
					task.run();
					//How can we maintain stats for tasks that successfully finished without any interruptions, as run won't return anything ?
				}
			} catch (InterruptedException e) {
				System.out.println("Worker thread " + workerId + " is interrupted. Closing Worker.");
				workerRunning.set(false);
			}
		}
	}

	public void submit(final RunnableTask runnableTask){
		mainLock.lock();
		taskSubmittedCount.getAndIncrement();
		try {
			if(isActive) {
				queue.put(runnableTask);
			} else {
				taskRejectedCount.getAndIncrement();
				System.out.println(runnableTask.getName() + " can not be accepted as the Executor is shutdown");
			}
		} catch (InterruptedException e) {
			System.err.println("Worker thread is interrupted " + e.getMessage());
		} finally {
			mainLock.unlock();
		}
	}

	public void shutdown() {
		System.out.println("Shutting down thread pool");
		this.isActive = false;
		queue.clear();
	}

	public List<RunnableTask> shutdownNow() {
		System.out.println("Shutting down thread pool immediately");
		mainLock.lock();
		try {
			this.isActive = false;
			for(final WorkerThread workerThread : workers) {
				if (!workerThread.isInterrupted()) {
					workerThread.interrupt();//Send interrupt
				}
			}
			return new ArrayList<>(queue);
		} finally {
			queue.clear();
			mainLock.unlock();
		}
	}

	public long getTaskInitiatedCount() {
		return taskInitiatedCount.get();
	}

	public long getTaskSubmittedCount() {
		return taskSubmittedCount.get();
	}

	public long getTaskRejectedCount() {
		return taskRejectedCount.get();
	}

	public boolean isActive(){
		return isActive;
	}

	public void dumpStats() {
		System.out.println("Tasks submitted " + getTaskSubmittedCount());
		System.out.println("Tasks initiated " + getTaskInitiatedCount());
		System.out.println("Tasks rejected " + getTaskRejectedCount());
	}
}
