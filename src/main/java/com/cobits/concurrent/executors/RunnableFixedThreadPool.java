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
	private final AtomicBoolean isActive = new AtomicBoolean(false);
	private final BlockingQueue<RunnableTask> queue = new LinkedBlockingQueue<>();
	private final ReentrantLock mainLock = new ReentrantLock();
	private WorkerThread[] workers;

	public RunnableFixedThreadPool(){
		this(DEFAULT_POOL_SIZE);
	}

	public RunnableFixedThreadPool(final int poolSize)
	{
		workers = new WorkerThread[poolSize];
		for (int i = 0; i < poolSize; i++) {
			workers[i] = new WorkerThread(i);
			workers[i].start();// Eager Start
		}
		this.isActive.set(true);// Setting Active state to true when Worker Threads have started, other states can be isIdle, isAwaitingTermination etc.
	}

	private class WorkerThread extends Thread {
		private int workerId;
		private AtomicBoolean isWorkerRunning;

		public WorkerThread(int workerId){
			this.workerId = workerId;
			this.isWorkerRunning = new AtomicBoolean(false);
		}

		public void run() {
			isWorkerRunning.set(true);
			try {
				while(isWorkerRunning.get()){
					final RunnableTask task = queue.take();
					taskInitiatedCount.incrementAndGet();
					System.out.println("Scheduling " + task.getName() + " on Worker Thread " + workerId);
					task.run();
					//TODO: How can we maintain stats for tasks that successfully finished(variable taskFinishedCount) without any interruptions, as run won't return anything ?
				}
			} catch (InterruptedException e) {
				System.out.println("Worker thread " + workerId + " is interrupted. Closing Worker.");
				isWorkerRunning.set(false);
			}
		}
	}

	public void submit(final RunnableTask runnableTask){
		mainLock.lock();
		taskSubmittedCount.getAndIncrement();
		try {
			if(isActive.get()) { // Add a task to queue only if it's not shutdown already
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

	/**
	 * Just shutdown the Executor, do not accept anymore requests, do not process unscheduled tasks
	 */
	public void shutdown() {
		System.out.println("Shutting down thread pool");
		this.isActive.set(false);
		queue.clear();
	}

	/**
	 * Shutdown the Executor, do not take any further requests in pool, do not execute any tasks enqueued already, and interrupt existing running tasks
	 * @return List<RunnableTask> which were not executed completely and were present in the queue when interruption happened
	 */
	public List<RunnableTask> shutdownNow() {
		System.out.println("Shutting down thread pool immediately");
		mainLock.lock();
		try {
			this.isActive.set(false); // Switch off flag to show active status of Executor
			for(final WorkerThread workerThread : workers) {
				if (!workerThread.isInterrupted()) {
					workerThread.interrupt();//Send interrupt to each worker thread
				}
			}
			return new ArrayList<>(queue);// Return contents of queue which won't get processed entirely due to interrupt on workers
		} finally {
			queue.clear();// Clean queue
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
		return isActive.get();
	}

	public void dumpStats() {
		System.out.println("Tasks submitted " + getTaskSubmittedCount());
		System.out.println("Tasks initiated " + getTaskInitiatedCount());
		System.out.println("Tasks rejected " + getTaskRejectedCount());
	}
}
