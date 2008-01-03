/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 118516
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

    private final LinkedList queue = new LinkedList();

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
    public void asyncExec(Runnable runnable) {
        synchronized (queue) {
            queue.add(runnable);
            queue.notifyAll();
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
            while (block) {
                Runnable runnable = null;
                synchronized (queue) {
                    while (queue.isEmpty()) {
                        queue.wait();
                    }
                    
                    // Check the size in case the thread is being awoken by
                    // unblock().
                    if (!queue.isEmpty()) {
                        runnable = (Runnable) queue.removeFirst();
                    }
                }

                if (runnable != null) {
                    safeRun(runnable);
                    runnable = null;
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
}