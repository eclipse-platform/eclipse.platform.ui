/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.internal.runtime.RuntimeLog;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;

/**
 * Captures the implicit job state for a given thread. 
 */
class ThreadJob extends Job {

	/**
	 * Set to true if this thread job is running in a thread that did
	 * not own a rule already.  This means it needs to acquire the
	 * rule during beginRule, and must release the rule during endRule.
	 * @GuardedBy("JobManager.implicitJobs")
	 */
	protected boolean acquireRule = false;

	/**
	 * Indicates that this thread job did report to the progress manager
	 * that it will be blocked, and therefore when it begins it must
	 * be reported to the job manager when it is no longer blocked.
	 * @GuardedBy("JobManager.implicitJobs")
	 */
	boolean isBlocked = false;

	/**
	 * True if this ThreadJob has begun execution
	 * @GuardedBy("this")
	 */
	protected boolean isRunning = false;

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
	 * @GuardedBy("JobManager.implicitJobs")
	 */
	protected Job realJob;
	/**
	 * The stack of rules that have been begun in this thread, but not yet ended.
	 * 	@GuardedBy("JobManager.implicitJobs")
	 */
	private ISchedulingRule[] ruleStack;
	/**
	 * Rule stack pointer.
	 * INV: 0 <= top <= ruleStack.length
	 * 	@GuardedBy("JobManager.implicitJobs")
	 */
	private int top;

	/**
	 * Waiting state for thread jobs is independent of the internal state. When
	 * this variable is true, this ThreadJob is waiting in joinRun()
	 * 	@GuardedBy("jobStateLock")
	 */
	boolean isWaiting;

	ThreadJob(ISchedulingRule rule) {
		super("Implicit Job"); //$NON-NLS-1$
		setSystem(true);
		// calling setPriority will try to acquire JobManager.lock, breaking
		// lock acquisition protocol. Since we are constructing this thread,
		// we can call internalSetPriority
		((InternalJob) this).internalSetPriority(Job.INTERACTIVE);
		ruleStack = new ISchedulingRule[2];
		top = -1;
		((InternalJob) this).internalSetRule(rule);
	}

	/**
	 * An endRule was called that did not match the last beginRule in
	 * the stack.  Report and log a detailed informational message.
	 * @param rule The rule that was popped
	 * @GuardedBy("JobManager.implicitJobs")
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
			IStatus error = new Status(IStatus.ERROR, JobManager.PI_JOBS, 1, msg, t);
			RuntimeLog.log(error);
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
			IStatus error = new Status(IStatus.ERROR, JobManager.PI_JOBS, 1, msg, new IllegalArgumentException());
			RuntimeLog.log(error);
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
			//logged message should not be translated
			IStatus status = new Status(IStatus.ERROR, JobManager.PI_JOBS, JobManager.PLUGIN_ERROR, "ThreadJob.isCanceled", e); //$NON-NLS-1$
			RuntimeLog.log(status);
		}
		return false;
	}

	/**
	 * Returns true if this thread job was scheduled and actually started running.
	 * @GuardedBy("this")
	 */
	synchronized boolean isRunning() {
		return isRunning;
	}

	/**
	 * A reentrant method which will run this <code>ThreadJob</code> immediately if there 
	 * are no existing jobs with conflicting rules, or block until the rule can be acquired. If this 
	 * job must block, the <code>LockListener</code> is given a chance to override.
	 * If override is not granted, then this method will block until the rule is available. If 
	 * <code>LockListener#canBlock</code> returns <tt>true</tt>, then the <code>monitor</code>
	 * <i>will not</i> be periodically checked for cancellation. It will only be rechecked if this 
	 * thread is interrupted. If <code>LockListener#canBlock</code> returns <tt>false</tt> The 
	 * <code>monitor</code> <i>will</i> be checked periodically for cancellation.
	 * 
	 * When a UI is present, it is recommended that the <code>LockListener</code> 
	 * should not allow the UI thread to block without checking the <code>monitor</code>. This 
	 * ensures that the UI remains responsive. 
	 * 
	 * @see LockListener#aboutToWait(Thread)
	 * 	@see LockListener#canBlock()
	 * @see JobManager#transferRule(ISchedulingRule, Thread)
	
	 * @return <tt>this</tt>, or the <code>ThreadJob</code> instance that was 
	 * unblocked (due to transferRule) in the case of reentrant invocations of this method.
	 * 
	 * @param monitor - The <code>IProgressMonitor</code> used to report blocking status and 
	 * cancellation.
	 * 
	 * @throws OperationCanceledException if this job was canceled before it was started. 
	 */
	ThreadJob joinRun(final IProgressMonitor monitor) {
		if (isCanceled(monitor))
			throw new OperationCanceledException();
		final Thread currentThread = Thread.currentThread();
		// check if there is a blocking thread before waiting
		InternalJob blockingJob = manager.findBlockingJob(this);
		Thread blocker = blockingJob == null ? null : blockingJob.getThread();
		boolean interrupted = false;
		try {
			// just return if lock listener decided to grant immediate access
			if (manager.getLockManager().aboutToWait(blocker))
				return this;
			// Ask lock manager if it safe to block this thread
			if (manager.getLockManager().canBlock())
				return waitForRun(monitor, blockingJob, blocker, false);
			WaitForRunThread runner = new WaitForRunThread(this, blockingJob, blocker);
			runner.start();
			try {
				manager.reportBlocked(monitor, blockingJob);
				while (true) {
					if (isCanceled(monitor))
						throw new OperationCanceledException();
					// just return if lock listener decided to grant immediate access
					if (manager.getLockManager().aboutToWait(blocker))
						return this;
					synchronized (runner.getNotifier()) {
						if (runner.isFinished())
							break;
						try {
							runner.getNotifier().wait(250);
						} catch (InterruptedException e) {
							interrupted = true;
						}
						// check to see if the thread runner was aborted early first
						if (runner.isFinished())
							break;
					}
					blockingJob = manager.findBlockingJob(this);
					// the rule could have been transferred to *this* thread while we were waiting
					blocker = blockingJob == null ? null : blockingJob.getThread();
					if (blocker == currentThread && blockingJob instanceof ThreadJob) {
						// so abort our thread, but only return if the runner wasn't already done
						if (runner.shutdown()) {
							// now we are just the nested acquire case
							ThreadJob result = (ThreadJob) blockingJob;
							result.push(getRule());
							result.isBlocked = this.isBlocked;
							return result;
						}
					}
				}
				// if thread is still running, shut it down
				runner.shutdown();
				if (runner.getException() != null)
					throw runner.getException();
				return runner.getResult();
			} finally {
				manager.reportUnblocked(monitor);
				//runner may already be shutdown, but make sure it shuts down in all cases
				runner.shutdown();
			}
		} finally {
			manager.getLockManager().aboutToRelease();
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	ThreadJob waitForRun(IProgressMonitor monitor, InternalJob blockingJob, Thread blocker, boolean forked) {
		ThreadJob result = this;
		boolean interrupted = false;
		try {
			waitStart(monitor, blockingJob);
			// if we didn't fork, we need have the manager monitor our progress
			// monitor. The manager will interrupt us when cancellation status
			// changes.
			if (!forked)
				manager.beginMonitoring(this, monitor);
			final Thread currentThread = Thread.currentThread();

			// Check all conditions under the manager.lock. Anything that can
			// cause a change in any condition must also notify() the lock.
			// Invoke aboutToWait() once per newly discovered blocker. 
			// aboutToWait() must be called without holding any locks.
			while (true) {
				//inner loop until interrupted or the blocking job changes
				while (true) {
					// try to run the job
					blockingJob = manager.runNow(this);
					if (blockingJob == null)
						return this;
					Thread newBlocker = blockingJob == null ? null : blockingJob.getThread();
					// Rules are never transferred to us if we're inside a WaitForRunThread
					if (!forked) {
						// the rule could have been transferred to this thread while we were waiting
						if (newBlocker == currentThread && blockingJob instanceof ThreadJob) {
							// now we are just the nested acquire case
							result = (ThreadJob) blockingJob;
							result.push(getRule());
							result.isBlocked = this.isBlocked;
							return result;
						}
					}
					if (blocker == newBlocker) {
						synchronized (blockingJob.jobStateLock) {
							try {
								//blocking job may have finished before we entered sync block
								if (blockingJob.getState() == Job.RUNNING)
									blockingJob.jobStateLock.wait();
							} catch (InterruptedException e) {
								interrupted = true;
								// Must break here to ensure aboutToWait() is called outside the lock.
								break;
							}
						}
					} else {
						// we got a different blocker
						blocker = newBlocker;
						break;
					}
				}
				// monitor is foreign code, do not hold locks while calling into monitor
				if (isCanceled(monitor))
					throw new OperationCanceledException();

				if (manager.getLockManager().aboutToWait(blocker))
					return this;
				// if we don't return immediately, we need to re-acquire the
				// lock and re-check all conditions before waiting
			}

		} finally {
			if (this == result)
				waitEnd(monitor);
			if (!forked)
				manager.endMonitoring(this);
			if (interrupted)
				Thread.currentThread().interrupt();
		}
	}

	/**
	 * Pops a rule. Returns true if it was the last rule for this thread
	 * job, and false otherwise.
	 * 	@GuardedBy("JobManager.implicitJobs")
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
	 * 	@GuardedBy("JobManager.implicitJobs")
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
	 * 	@GuardedBy("JobManager.implicitJobs")
	 */
	boolean recycle() {
		//don't recycle if still running for any reason
		if (getState() != Job.NONE)
			return false;
		//clear and reset all fields
		acquireRule = isRunning = isBlocked = false;
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
			isRunning = true;
		}
		return ASYNC_FINISH;
	}

	/**
	 * Records the job that is actually running in this thread, if any
	 * @param realJob The running job
	 * 	@GuardedBy("JobManager.implicitJobs")
	 */
	void setRealJob(Job realJob) {
		this.realJob = realJob;
	}

	/**
	 * Returns true if this job should cause a self-canceling job
	 * to cancel itself, and false otherwise.
	 * @GuardedBy("JobManager.implicitJobs")
	 */
	boolean shouldInterrupt() {
		return realJob == null ? true : !realJob.isSystem();
	}

	/* (non-javadoc)
	 * For debugging purposes only
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ThreadJob"); //$NON-NLS-1$
		buf.append('(').append(realJob).append(',').append(getRuleStack()).append(')');
		return buf.toString();
	}

	String getRuleStack() {
		StringBuffer buf = new StringBuffer();
		buf.append('[');
		for (int i = 0; i <= top && i < ruleStack.length; i++)
			buf.append(ruleStack[i]).append(',');
		buf.append(']');
		return buf.toString();
	}

	/**
	 * Reports that this thread was blocked, but is no longer blocked and is able
	 * to proceed.
	 * @param monitor The monitor to report unblocking to.
	 */
	private void waitEnd(IProgressMonitor monitor) {
		final LockManager lockManager = manager.getLockManager();
		final Thread currentThread = Thread.currentThread();
		if (isRunning()) {
			lockManager.addLockThread(currentThread, getRule());
			//need to re-acquire any locks that were suspended while this thread was blocked on the rule
			lockManager.resumeSuspendedLocks(currentThread);
		} else {
			//tell lock manager that this thread gave up waiting
			lockManager.removeLockWaitThread(currentThread, getRule());
		}
		manager.implicitJobs.removeWaiting(this);
	}

	/**
	 * Indicates the start of a wait on a scheduling rule. Report the
	 * blockage to the progress manager and update the lock manager.
	 * @param monitor The monitor to report blocking to
	 * @param blockingJob The job that is blocking this thread, or <code>null</code>
	 */
	private void waitStart(IProgressMonitor monitor, InternalJob blockingJob) {
		manager.getLockManager().addLockWaitThread(Thread.currentThread(), getRule());
		isBlocked = true;
		manager.reportBlocked(monitor, blockingJob);
		manager.implicitJobs.addWaiting(this);
	}
}