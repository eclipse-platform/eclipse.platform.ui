/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.jobs;

import org.eclipse.core.runtime.*;

/**
 * A thread that is used to wait for a scheduling rule so that the blocked thread
 * can remain responsive to cancellation.
 */
class WaitForRunThread extends Thread {

	/**
	 * @GuardedBy("itself")
	 */
	private final Object notifier = new Object();
	private final ThreadJob threadJob;
	private final IProgressMonitor monitor;
	private final InternalJob blockingJob;
	private final Thread blocker;
	private RuntimeException exception;
	private ThreadJob result;
	/**
	 * @GuardedBy("notifier")
	 */
	private boolean finished;

	WaitForRunThread(ThreadJob threadJob, InternalJob blockingJob, Thread blocker) {
		super("Worker-[" + Thread.currentThread().getName() + " waiting for " + threadJob.getRuleStack() + ']'); //$NON-NLS-1$ //$NON-NLS-2$
		this.threadJob = threadJob;
		this.monitor = new NullProgressMonitor();
		this.blockingJob = blockingJob;
		this.blocker = blocker;
	}

	public void run() {
		try {
			result = threadJob.waitForRun(monitor, blockingJob, blocker, true);
		} catch (Exception e) {
			exception = createException(e);
		} catch (LinkageError e) {
			exception = createException(e);
		} finally {
			synchronized (getNotifier()) {
				finished = true;
				getNotifier().notifyAll();
			}
		}
	}

	private RuntimeException createException(Throwable e) {
		if (e instanceof OperationCanceledException)
			return (OperationCanceledException) e;
		return new RuntimeException("An internal error occured while Waiting to run [" + threadJob.toString() + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
	}

	ThreadJob getResult() {
		return result;
	}

	RuntimeException getException() {
		return exception;
	}

	Object getNotifier() {
		return notifier;
	}

	boolean isFinished() {
		synchronized (getNotifier()) {
			return finished;
		}
	}

	boolean shutdown() {
		boolean interrupted = false;
		synchronized (getNotifier()) {
			if (isFinished())
				return false;
			monitor.setCanceled(true);
			interrupt();
			while (!isFinished()) {
				try {
					getNotifier().wait();
				} catch (InterruptedException e) {
					interrupted = true;
				}
			}
		}
		if (interrupted)
			Thread.currentThread().interrupt();
		return true;
	}
}
