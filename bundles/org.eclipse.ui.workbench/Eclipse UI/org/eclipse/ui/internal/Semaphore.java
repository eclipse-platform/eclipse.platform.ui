/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

public class Semaphore {
    protected long notifications;

    protected Thread operation;

    protected Runnable runnable;

    public Semaphore(Runnable runnable) {
        this.runnable = runnable;
        notifications = 0;
    }

    /**
     * Attempts to acquire this semaphore.  Returns true if it was successfully acquired,
     * and false otherwise.
     */
    public synchronized boolean acquire(long delay) throws InterruptedException {
        if (Thread.interrupted()) {
			throw new InterruptedException();
		}
        long start = System.currentTimeMillis();
        long timeLeft = delay;
        while (true) {
            if (notifications > 0) {
                notifications--;
                return true;
            }
            if (timeLeft <= 0) {
				return false;
			}
            wait(timeLeft);
            timeLeft = start + delay - System.currentTimeMillis();
        }
    }

    public boolean equals(Object obj) {
        return (runnable == ((Semaphore) obj).runnable);
    }

    public Thread getOperationThread() {
        return operation;
    }

    public Runnable getRunnable() {
        return runnable;
    }

    public int hashCode() {
        return runnable == null ? 0 : runnable.hashCode();
    }

    public synchronized void release() {
        notifications++;
        notifyAll();
    }

    public void setOperationThread(Thread operation) {
        this.operation = operation;
    }

    // for debug only
    public String toString() {
        return "Semaphore(" + runnable + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
