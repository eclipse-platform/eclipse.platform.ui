/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public final class PendingSyncExec {
	private final Semaphore semaphore = new Semaphore(0);

	private Thread operation;

	private final Runnable runnable;

	// Accessed by multiple threads. Synchronize on "this" before accessing.
	private boolean hasFinishedRunning;

    public PendingSyncExec(Runnable runnable) {
        this.runnable = runnable;
    }

    /**
     * Attempts to acquire this semaphore.  Returns true if it was successfully acquired,
     * and false otherwise.
     */
	private boolean acquire(long delay) throws InterruptedException {
		return semaphore.tryAcquire(delay, TimeUnit.MILLISECONDS);
    }

    @Override
	public boolean equals(Object obj) {
        return (runnable == ((PendingSyncExec) obj).runnable);
    }

    public Thread getOperationThread() {
        return operation;
    }

	public void run() {
		// Clear the interrupted flag. The blocked thread may have been
		// periodically interrupting the UI thread in
		// order to interrupt other tasks in the queue and cause this job to
		// start execution. (Note that it will
		// continue to try to interrupt this runnable if it takes too long to
		// run, until we set the hasFinishedRunning
		// flag).
		Thread.interrupted();
		try {
			if (runnable != null) {
				runnable.run();
			}
		} finally {
			// Record the fact that this pending syncExec has finished
			// execution, to prevent the calling thread from
			// interrupting this thread.
			synchronized (this) {
				hasFinishedRunning = true;
			}
			// The calling thread may have still interrupted this operation up
			// until the point where we flipped the
			// hasFinishedRunning flag.
			Thread.interrupted();
			semaphore.release();
		}
	}

	public void waitUntilExecuted(UILockListener lockListener) throws InterruptedException {
		// even if the UI was not blocked earlier, it might become blocked
		// before it can serve the asyncExec to do the pending work
		while (!acquire(1000)) {
			if (lockListener.isUIWaiting()) {
				synchronized (this) {
					if (!hasFinishedRunning) {
						lockListener.interruptUI(runnable);
					}
				}
			}
		}
	}

    @Override
	public int hashCode() {
        return runnable == null ? 0 : runnable.hashCode();
    }

    public void setOperationThread(Thread operation) {
        this.operation = operation;
    }

    // for debug only
    @Override
	public String toString() {
		return "PendingSyncExec(" + runnable + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
