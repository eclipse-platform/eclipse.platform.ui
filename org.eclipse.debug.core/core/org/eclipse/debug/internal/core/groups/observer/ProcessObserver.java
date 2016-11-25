/*******************************************************************************
 *  Copyright (c) 2012, 2016 SSI Schaefer and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      SSI Schaefer
 *******************************************************************************/
package org.eclipse.debug.internal.core.groups.observer;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IProcess;

/**
 * The {@code ProcessObserver} observes a given {@linkplain IProcess process} instance and notifies
 * a {@linkplain CountDownLatch synchronization object} when the process terminates.
 */
public final class ProcessObserver implements Callable<Integer> {
    private final IProcess p;
    private final IProgressMonitor pMonitor;
    private final CountDownLatch countDownLatch;

    public ProcessObserver(IProgressMonitor monitor, IProcess p, CountDownLatch countDownLatch) {
        this.p = p;
        this.pMonitor = monitor;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public Integer call() throws Exception {
        try {
            while (!p.isTerminated() && !pMonitor.isCanceled()) {
                TimeUnit.MILLISECONDS.sleep(250);

                if (countDownLatch.getCount() == 0) {
                    break;
                }
            }
            // check if terminated or timeout
            if (p.isTerminated()) {
                return p.getExitValue();
            }
            return 0;
        } finally {
            countDownLatch.countDown();
        }
    }
}
