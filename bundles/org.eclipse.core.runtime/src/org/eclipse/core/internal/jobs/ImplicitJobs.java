/**********************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved.   This
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
	 * Captures the implicit job state for a given thread. 
	 */
	class ThreadJob extends Job {
		/**
		 * Set to true if this thread job is running in a thread that did
		 * not own a rule already.  This means it needs to acquire the
		 * rule during beginRule, and must release the rule during endRule.
		 */
		protected boolean acquireRule = false;
		/**
		 * Used for diagnosing mismatched begin/end pairs. This field
		 * is only used when in debug mode, to capture the stack trace
		 * of the last call to beginRule.
		 */
		private RuntimeException lastPush = null;
		/**
		 * The actual job that is running in the thread that this 
		 * ThreadJob represents.  This will be null if this thread
		 * job is capturing a rule acquired outside of a job.
		 */
		protected Job realJob;
		/**
		 * The stack of rules that have been begun in this thread, but not yet ended.
		 */
		private ISchedulingRule[] ruleStack;
		/**
		 * True if this ThreadJob has begun execution
		 */
		protected boolean running = false;
		/**
		 * Rule stack pointer.
		 */
		private int top;

		ThreadJob(ISchedulingRule rule) {
			super("Implicit Job"); //$NON-NLS-1$
			setSystem(true);
			setPriority(Job.INTERACTIVE);
			ruleStack = new ISchedulingRule[2];
			top = -1;
			setRule(rule);
		}

		/**
		 * An endRule was called that did not match the last beginRule in
		 * the stack.  Report and log a detailed informational message.
		 * @param rule The rule that was popped
		 */
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
			if (JobManager.DEBUG || JobManager.DEBUG_BEGIN_END) {
				System.out.println(msg);
				Throwable t = lastPush == null ? new IllegalArgumentException() : lastPush;
				IStatus error = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, 1, msg, t);
				InternalPlatform.getDefault().log(error);
			}
			Assert.isLegal(false, msg);
		}

		/**
		 * Client has attempted to begin a rule that is not contained within
		 * the outer rule.
		 */
		private void illegalPush(ISchedulingRule pushRule, ISchedulingRule baseRule) {
			StringBuffer buf = new StringBuffer("Attempted to beginRule: "); //$NON-NLS-1$
			buf.append(pushRule);
			buf.append(", does not match outer scope rule: "); //$NON-NLS-1$
			buf.append(baseRule);
			String msg = buf.toString();
			if (JobManager.DEBUG) {
				System.out.println(msg);
				IStatus error = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, 1, msg, new IllegalArgumentException());
				InternalPlatform.getDefault().log(error);
			}
			Assert.isLegal(false, msg);

		}

		/**
		 * Returns true if the monitor is canceled, and false otherwise.
		 * Protects the caller from exception in the monitor implementation.
		 */
		private boolean isCanceled(IProgressMonitor monitor) {
			try {
				return monitor.isCanceled();
			} catch (RuntimeException e) {
				String msg = Policy.bind("jobs.internalError"); //$NON-NLS-1$
				IStatus status = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME, IPlatform.PLUGIN_ERROR, msg, e);
				InternalPlatform.getDefault().log(status);
			}
			return false;
		}

		/**
		 * Returns true if this thread job was scheduled and actually started running.
		 */
		synchronized boolean isRunning() {
			return running;
		}

		/**
		 * Schedule the job and block the calling thread until the job starts running
		 */
		void joinRun(IProgressMonitor monitor) {
			if (isCanceled(monitor))
				throw new OperationCanceledException();
			//check if there is a blocking thread before waiting
			InternalJob blockingJob = manager.findBlockingJob(this);
			Thread blocker = blockingJob == null ? null : blockingJob.getThread();
			//lock listener decided to grant immediate access
			if (!manager.getLockManager().aboutToWait(blocker)) {
				try {
					waitStart(monitor, blockingJob);
					while (true) {
						if (isCanceled(monitor))
							throw new OperationCanceledException();
						//try to run the job 
						if (manager.runNow(this))
							break;
						//update blocking job
						blockingJob = manager.findBlockingJob(this);
						blocker = blockingJob == null ? null : blockingJob.getThread();
						if (manager.getLockManager().aboutToWait(blocker))
							break;
						//must lock instance before calling wait
						synchronized (this) {
							try {
								wait(250);
							} catch (InterruptedException e) {
								//ignore
							}
						}
					}
				} finally {
					waitEnd(monitor);
				}
			}
			manager.getLockManager().aboutToRelease();
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

		/**
		 * Adds a new scheduling rule to the stack of rules for this thread. Throws
		 * a runtime exception if the new rule is not compatible with the base
		 * scheduling rule for this thread.
		 */
		void push(final ISchedulingRule rule) {
			final ISchedulingRule baseRule = getRule();
			if (++top >= ruleStack.length) {
				ISchedulingRule[] newStack = new ISchedulingRule[ruleStack.length * 2];
				System.arraycopy(ruleStack, 0, newStack, 0, ruleStack.length);
				ruleStack = newStack;
			}
			ruleStack[top] = rule;
			if (JobManager.DEBUG_BEGIN_END)
				lastPush = (RuntimeException) new RuntimeException().fillInStackTrace();
			//check for containment last because we don't want to fail again on endRule
			if (baseRule != null && rule != null && !baseRule.contains(rule))
				illegalPush(rule, baseRule);
		}

		/**
		 * Reset all of this job's fields so it can be reused.  Returns false if
		 * reuse is not possible
		 */
		boolean recycle() {
			//don't recycle if still running for any reason
			if (getState() != Job.NONE)
				return false;
			//clear and reset all fields
			acquireRule = running = false;
			realJob = null;
			setRule(null);
			setThread(null);
			if (ruleStack.length != 2)
				ruleStack = new ISchedulingRule[2];
			else
				ruleStack[0] = ruleStack[1] = null;
			top = -1;
			return true;
		}

		/** (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public IStatus run(IProgressMonitor monitor) {
			synchronized (this) {
				running = true;
			}
			return ASYNC_FINISH;
		}

		/**
		 * Records the job that is actually running in this thread, if any
		 * @param realJob The running job
		 */
		void setRealJob(Job realJob) {
			this.realJob = realJob;
		}

		/**
		 * Returns true if this job should cause a self-canceling job
		 * to cancel itself, and false otherwise.
		 */
		boolean shouldInterrupt() {
			return realJob == null ? true : !realJob.isSystem();
		}

		/**
		 * Reports that this thread was blocked, but is no longer blocked and is able
		 * to proceed.
		 * @param monitor The monitor to report unblocking to.
		 */
		private void waitEnd(IProgressMonitor monitor) {
			if (isRunning()) {
				manager.getLockManager().addLockThread(Thread.currentThread(), getRule());
				//need to reaquire any locks that were suspended while this thread was blocked on the rule
				manager.getLockManager().resumeSuspendedLocks(Thread.currentThread());
			} else {
				//tell lock manager that this thread gave up waiting
				manager.getLockManager().removeLockWaitThread(Thread.currentThread(), getRule());
			}
			manager.reportUnblocked(monitor);
		}

		/**
		 * Indicates the start of a wait on a scheduling rule. Report the
		 * blockage to the progress manager and update the lock manager.
		 * @param monitor The monitor to report blocking to
		 * @param blockingJob The job that is blocking this thread, or <code>null</code>
		 */
		private void waitStart(IProgressMonitor monitor, InternalJob blockingJob) {
			manager.getLockManager().addLockWaitThread(Thread.currentThread(), getRule());
			manager.reportBlocked(monitor, blockingJob);
		}
	}

	/**
	 * Cached of unused instance that can be reused 
	 */
	private ThreadJob jobCache = null;
	protected JobManager manager;

	/**
	 * Maps (Thread->ThreadJob), threads to the currently running job for that
	 * thread.
	 */
	private final Map threadJobs = new HashMap(20);

	ImplicitJobs(JobManager manager) {
		this.manager = manager;
	}

	/* (Non-javadoc) 
	 * @see IJobManager#beginRule 
	 */
	void begin(ISchedulingRule rule, IProgressMonitor monitor) {
		final Thread currentThread = Thread.currentThread();
		ThreadJob threadJob;
		synchronized (this) {
			threadJob = (ThreadJob) threadJobs.get(currentThread);
			if (threadJob != null) {
				//nested rule, just push on stack and return
				threadJob.push(rule);
				return;
			}
			//no need to schedule a thread job for a null rule
			if (rule == null)
				return;
			//create a thread job for this thread, use the rule from the real job if it has one
			Job realJob = manager.currentJob();
			if (realJob != null && realJob.getRule() != null)
				threadJob = newThreadJob(realJob.getRule());
			else {
				threadJob = newThreadJob(rule);
				threadJob.acquireRule = true;
			}
			//indicate if it is a system job to ensure isBlocking works correctly
			threadJob.setRealJob(realJob);
			threadJob.setThread(currentThread);
			threadJob.push(rule);
		}
		try {
			//join the thread job outside sync block
			if (threadJob.acquireRule) {
				//no need to reaquire any locks because the thread did not wait to get this lock
				if (manager.runNow(threadJob))
					manager.getLockManager().addLockThread(Thread.currentThread(), rule);
				else
					threadJob.joinRun(monitor);
			}
		} finally {
			//remember this thread job as the job for this thread - only do this
			//after the rule is acquired because it is ok for this thread to acquire
			//and release other rules while waiting.
			synchronized (this) {
				threadJobs.put(currentThread, threadJob);
			}
		}
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
			//if this job had a rule, then we are essentially releasing a lock
			//note it is safe to do this even if the acquire was aborted
			if (threadJob.acquireRule)
				manager.getLockManager().removeLockThread(Thread.currentThread(), threadJob.getRule());
			//if the job was started, we need to notify job manager to end it
			if (threadJob.isRunning())
				manager.endJob(threadJob, Status.OK_STATUS, false);
			recycle(threadJob);
		}
	}

	/**
	 * Returns the currently running implicit job for the given thread, or null
	 * if there currently isn't one.
	 */
	Job jobForThread(Thread thread) {
		return (Job) threadJobs.get(thread);
	}

	/**
	 * Returns a new or reused ThreadJob instance. 
	 */
	private ThreadJob newThreadJob(ISchedulingRule rule) {
		if (jobCache != null) {
			ThreadJob job = jobCache;
			job.setRule(rule);
			job.acquireRule = job.running = false;
			job.realJob = null;
			jobCache = null;
			return job;
		}
		return new ThreadJob(rule);
	}

	/**
	 * Indicates that a thread job is no longer in use and can be reused. 
	 */
	private void recycle(ThreadJob job) {
		if (jobCache == null && job.recycle())
			jobCache = job;
	}
}