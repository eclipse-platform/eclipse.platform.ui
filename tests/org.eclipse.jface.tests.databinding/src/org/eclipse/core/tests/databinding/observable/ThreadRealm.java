/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bugs 118516, 281723, 286533
 *******************************************************************************/
package org.eclipse.core.tests.databinding.observable;

import java.util.LinkedList;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;

/**
 * {@link Realm} that enforces execution to be within a specific
 * {@link Thread}.
 *
 * @since 3.2
 */
public class ThreadRealm extends Realm {
    private Thread thread;

    private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

    private volatile boolean block;

    /**
     * Initializes the realm.
     *
     * @param thread
     */
    public synchronized void init(Thread thread) {
        if (thread == null) {
            throw new IllegalArgumentException("Parameter thread was null."); //$NON-NLS-1$
        }
        Assert.isTrue(this.thread == null, "Realm can only be initialized once.");

        this.thread = thread;
    }

    /**
     * @return <code>true</code> if the current thread is the thread for
     *         the realm
     */
    @Override
	public boolean isCurrent() {
        return Thread.currentThread() == thread;
    }

    /**
     * @return thread, <code>null</code> if not
     *         {@link #init(Thread) initialized}
     */
    public Thread getThread() {
        return thread;
    }

    /**
     * Queues the provided <code>runnable</code>.
     *
     * @param runnable
     */
    @Override
	public void asyncExec(Runnable runnable) {
        synchronized (queue) {
            queue.add(runnable);
            queue.notifyAll();
        }
    }

    /**
     * Returns after the realm has completed all runnables currently on its
     * queue.  Do not call from the realm's thread.
     *
     * @throws IllegalStateException
     *             if the ThreadRealm is not blocking on its thread.
     * @throws IllegalStateException
     *             if invoked from the realm's own thread.
     */
    public void processQueue() {
        if (Thread.currentThread() == thread) {
            throw new IllegalStateException(
                    "Cannot execute this method in the realm's own thread");
        }

        try {
            synchronized (queue) {
                while (!queue.isEmpty()) {
                    if (!block)
                        throw new IllegalStateException(
                                "Cannot process queue, ThreadRealm is not blocking on its thread");
                    queue.wait();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isBlocking() {
        return block;
    }

    /**
     * Blocks the current thread invoking runnables.
     */
    public void block() {
        if (block) {
            throw new IllegalStateException("Realm is already blocking.");
        }

        if (Thread.currentThread() != thread) {
            throw new IllegalStateException("The current thread is not the correct thread.");
        }

        try {
            block = true;

            synchronized (queue) {
                queue.notifyAll(); // so waitUntilBlocking can return
            }

            while (block) {
                Runnable runnable = null;
                synchronized (queue) {
                    if (queue.isEmpty()) {
                        queue.wait();
                    } else {
                        runnable = queue.getFirst();
                    }
                }

                if (runnable != null) {
                    safeRun(runnable);
                    synchronized (queue) {
                        // Don't remove the runnable from the queue until after
                        // it has run, or else processQueue() may return before
                        // the last runnable has finished
                        queue.removeFirst();
                        queue.notifyAll();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            block = false;
        }
    }

    /**
     * Unblocks the thread.
     */
    public void unblock() {
        block = false;

        // Awaken the thread if waiting.
        synchronized (queue) {
            queue.notifyAll();
        }
    }

    /**
     * Blocks until the ThreadRealm is blocking on its own thread.
     */
    public void waitUntilBlocking() {
        if (Thread.currentThread() == thread) {
            throw new IllegalStateException(
                    "Cannot execute this method in the realm's own thread");
        }

        while (!block) {
            synchronized (queue) {
                try {
                    queue.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
