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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.internal.runtime.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Implicit jobs are jobs that are running by virtue of a JobManager.begin/end
 * pair. They act like normal jobs, except they are tied to an arbitrary thread
 * of the client's choosing, and they can be nested.
 */
class ImplicitJobs {
	/**
	 * Captures the implicit job state for a given thread. */
	class ThreadJob extends Job {
		private RuntimeException lastPush = null;
		private ISchedulingRule[] ruleStack;
		protected boolean running = false;
		protected boolean queued = false;
		private int top;
		ThreadJob(ISchedulingRule rule) {
			super("Implicit job"); //$NON-NLS-1$
			setSystem(true);
			setPriority(INTERACTIVE);
			setRule(rule);
			ruleStack = new ISchedulingRule[2];
			top = -1;
		}
		/**
		 * Schedule the job and block the calling thread until the job starts running
		 */
		synchronized void joinRun(IProgressMonitor monitor) {
			if (monitor.isCanceled())
				throw new OperationCanceledException();
			running = false;
			//check if there is a blocking thread before trying to schedule
			InternalJob blockingJob = manager.findBlockingJob(this);
			Thread blocker = blockingJob == null ? null : blockingJob.getThread();
			//lock listener decided to grant immediate access
			if (!manager.getLockManager().aboutToWait(blocker)) {
				try {
					reportBlocked(monitor, blockingJob);
					queued = true;
					schedule();
					while (!running) {
						if (monitor.isCanceled()) {
							//cancel the running job
							running = !cancel();
							throw new OperationCanceledException();
						}
						blocker = manager.getBlockingThread(this);
						if (manager.getLockManager().aboutToWait(blocker))
							break;
						try {
							wait(250);
						} catch (InterruptedException e) {
						}
					}
				} finally {
					reportUnblocked(monitor);
				}
			}
			manager.getLockManager().aboutToRelease();
			running = true;
			setThread(Thread.currentThread());
		}
		private void reportUnblocked(IProgressMonitor monitor) {
			if (!(monitor instanceof IProgressMonitorWithBlocking))
				return;
			((IProgressMonitorWithBlocking)monitor).clearBlocked();
		}
		private void reportBlocked(IProgressMonitor monitor, InternalJob blockingJob) {
			if (!(monitor instanceof IProgressMonitorWithBlocking))
				return;
			String msg = (blockingJob == null || blockingJob instanceof ThreadJob)
				? Policy.bind("jobs.blocked0")  //$NON-NLS-1$
				: Policy.bind("jobs.blocked1", blockingJob.getName());  //$NON-NLS-1$
			IStatus reason = new Status(IStatus.INFO, Platform.PI_RUNTIME, 1, msg, null);
			((IProgressMonitorWithBlocking)monitor).setBlocked(reason);
		}
		/**
		 * Pops a rule. Returns true if it was the last rule for this thread
		 * job, and false otherwise.
		 */
		boolean pop(ISchedulingRule rule) {
			if (top < 0 || ruleStack[top] != rule)
				illegalPop(rule);
			ruleStack[top--] = null;
			return top < 0;
		}
		private void illegalPop(ISchedulingRule rule) {
			StringBuffer buf = new StringBuffer("Attempted to endRule: "); //$NON-NLS-1$
			buf.append(rule);
			if (top >= 0 && top < ruleStack.length) {
				buf.append(", does not match most recent begin: "); //$NON-NLS-1$
				buf.append(ruleStack[top]);
			} else {
				if (top < 0)
					buf.append(", but there was no matching beginRule"); //$NON-NLS-1$
				else
					buf.append(", but the rule stack was out of bounds: " + top); //$NON-NLS-1$
			}
			buf.append(".  See log for trace information if rule tracing is enabled."); //$NON-NLS-1$
			String msg = buf.toString();
			if (JobManager.DEBUG) {
				System.out.println(msg);
				IStatus error = new Status(IStatus.ERROR, Platform.PI_RUNTIME, 1, msg, lastPush);
				InternalPlatform.log(error);
			}
			Assert.isLegal(false, msg);
		}
		void push(ISchedulingRule rule) {
			if (++top >= ruleStack.length) {
				ISchedulingRule[] newStack = new ISchedulingRule[ruleStack.length * 2];
				System.arraycopy(ruleStack, 0, newStack, 0, ruleStack.length);
				ruleStack = newStack;
			}
			ruleStack[top] = rule;
			if (JobManager.DEBUG_BEGIN_END)
				lastPush = (RuntimeException) new RuntimeException().fillInStackTrace();
		}
		public void recycle() {
			//clear and reset all fields
			running = queued = false;
			setRule(null);
			setThread(null);
			if (ruleStack.length != 2)
				ruleStack = new ISchedulingRule[2];
			else
				ruleStack[0] = ruleStack[1] = null;
			top = -1;
		}
		public IStatus run(IProgressMonitor monitor) {
			synchronized (this) {
				running = true;
				notifyAll();
			}
			return Job.ASYNC_FINISH;
		}
	}
	/**
	 * Maps (Thread->ThreadJob), threads to the currently running job for that
	 * thread.
	 */
	private final Map threadJobs = new HashMap(20);
	/**
	 * Cached of unused instance that can be reused */
	private ThreadJob jobCache = null;
	protected JobManager manager;
	ImplicitJobs(JobManager manager) {
		this.manager = manager;
	}
	/* (Non-javadoc) 
	 * @see IJobManager#beginRule 
	 */
	void begin(ISchedulingRule rule, IProgressMonitor monitor) {
		boolean join = false;
		ThreadJob threadJob;
		synchronized (this) {
			Thread currentThread = Thread.currentThread();
			threadJob = (ThreadJob) threadJobs.get(currentThread);
			if (threadJob == null) {
				//no need to schedule a thread job for a null rule
				if (rule == null)
					return;
				//create a thread job for this thread, use the rule from the real job if it has one
				Job realJob = manager.currentJob();
				if (realJob != null && realJob.getRule() != null)
					threadJob = newThreadJob(realJob.getRule());
				else {
					threadJob = newThreadJob(rule);
					join = true;
					//if this job has a rule, then we are essentially acquiring a lock
					if (rule != null)
						manager.getLockManager().addLockThread(currentThread);
				}
				threadJob.setThread(currentThread);
				threadJobs.put(currentThread, threadJob);
			}
			threadJob.push(rule);
		}
		//join the thread job outside sync block
		if (join)
			if (!manager.runNow(threadJob))
				threadJob.joinRun(monitor);
	}

	/* (Non-javadoc) 
	 * @see IJobManager#endRule 
	 */
	synchronized void end(ISchedulingRule rule) {
		Thread currentThread = Thread.currentThread();
		ThreadJob threadJob = (ThreadJob) threadJobs.get(currentThread);
		if (threadJob == null)
			Assert.isLegal(rule == null, "endRule without matching beginRule: " + rule); //$NON-NLS-1$
		else if (threadJob.pop(rule)) {
			//clean up when last rule scope exits
			threadJobs.remove(currentThread);
			if (threadJob.running) {
				manager.endJob(threadJob, Status.OK_STATUS, threadJob.queued);
				//if this job had a rule, then we are essentially releasing a lock
				if (threadJob.getRule() != null)
					manager.getLockManager().removeLockThread(Thread.currentThread());
			}
			recycle(threadJob);
		}
	}
	/**
	 * Returns the currently running implicit job for the given thread, or null
	 * if there currently isn't one.
	 */
	Job jobForThread(Thread thread) {
		return (Job)threadJobs.get(thread);
	}
	/**
	 * Returns a new or reused ThreadJob instance. */
	private ThreadJob newThreadJob(ISchedulingRule rule) {
		if (jobCache != null) {
			ThreadJob job = jobCache;
			job.setRule(rule);
			jobCache = null;
			return job;
		}
		return new ThreadJob(rule);
	}
	/**
	 * Indicates that a thread job is no longer in use and can be reused. */
	private void recycle(ThreadJob job) {
		if (jobCache == null) {
			job.recycle();
			jobCache = job;
		}
	}
}