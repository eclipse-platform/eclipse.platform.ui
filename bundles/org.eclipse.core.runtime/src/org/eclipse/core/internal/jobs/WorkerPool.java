/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.jobs;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Maintains a pool of worker threads.  Threads are constructed lazily as required,
 * and are eventually discarded if not in use for awhile.  This class maintains the
 * thread creation/destruction policies for the job manager.
 */
class WorkerPool {
	private static final int MIN_THREADS = 1;
	private static final int MAX_THREADS = 10;
	private boolean running = false;
	private ArrayList threads = new ArrayList();
	/**
	 * The number of threads that are currently sleeping
	 */
	private int sleepingThreads = 0;
	/**
	 * Use the busy thread count to avoid starting new threads when a living
	 * thread is just doing house cleaning (notifying listeners, etc).
	 */
	private int busyThreads = 0;
	/**
	 * Threads not used by their best before timestamp are destroyed.
	 */
	private static final int BEST_BEFORE = 60000;

	private JobManager manager;

	protected WorkerPool(JobManager manager) {
		this.manager = manager;
		running = true;
	}

	protected synchronized void endJob(Job job, IStatus result) {
		busyThreads--;
		manager.endJob(job, result, true);
		//remove any locks this thread may be owning
		manager.getLockManager().removeAllLocks(Thread.currentThread());
	}
	protected synchronized void endWorker(Worker worker) {
		threads.remove(worker);
		if (JobManager.DEBUG)
			JobManager.debug("worker removed from pool: " + worker); //$NON-NLS-1$
	}
	/**
	 * Notfication that a job has been added to the queue.  Wake a worker,
	 * creating a new worker if necessary.  The provided job may be null.
	 */
	protected synchronized void jobQueued(InternalJob job) {
		//if there is a sleeping thread, wake it up
		if (sleepingThreads > 0) {
			if (JobManager.DEBUG)
				JobManager.debug("notifiying a worker"); //$NON-NLS-1$
			notify();
			return;
		}
		int threadCount = threads.size();
		//create a thread if all threads are busy and we're under the max size
		//if the job is high priority, we start a thread no matter what
		if (busyThreads >= threadCount && (threadCount < MAX_THREADS || (job != null && job.getPriority() == Job.INTERACTIVE))) {
			Worker worker = new Worker(this);
			threads.add(worker);
			if (JobManager.DEBUG)
				JobManager.debug("worker added to pool: " + worker); //$NON-NLS-1$
			worker.start();
			return;
		}
	}
	protected synchronized void shutdown() {
		running = false;
		notifyAll();
	}
	/**
	 * Sleep for the given duration or until woken.
	 */
	private synchronized void sleep(long duration) {
		sleepingThreads++;
		if (JobManager.DEBUG)
			JobManager.debug("worker sleeping for: " + duration + "ms"); //$NON-NLS-1$ //$NON-NLS-2$ 
		try {
			wait(duration);
		} catch (InterruptedException e) {
			if (JobManager.DEBUG)
				JobManager.debug("worker interrupted while waiting... :-|"); //$NON-NLS-1$
		} finally {
			sleepingThreads--;
		}
	}
	/**
	 * Returns a new job to run.  Returns null if the thread should die.
	 */
	protected synchronized Job startJob() {
		//if we're above capacity, kill the thread
		if (!running || threads.size() > MAX_THREADS)
			return null;
		Job job = manager.startJob();
		//spin until a job is found or until we have been idle for too long
		long idleStart = System.currentTimeMillis();
		while (running && job == null) {
			long hint = manager.sleepHint();
			if (hint > 0)
				sleep(Math.min(hint, BEST_BEFORE));
			job = manager.startJob();
			//if we were already idle, and there are still no new jobs, then the thread can expire
			if (job == null && (System.currentTimeMillis() - idleStart > BEST_BEFORE) && threads.size() > MIN_THREADS)
				break;
		}
		if (job != null) {
			busyThreads++;
			//if this job has a rule, then we are essentially acquiring a lock
			if (job.getRule() != null)
				manager.getLockManager().addLockThread(Thread.currentThread());
			//see if we need to wake another worker
			if (manager.sleepHint() <= 0)
				jobQueued(null);
		}
		return job;
	}
}