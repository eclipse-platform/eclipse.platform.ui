/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

public class UISynchronizer extends Synchronizer {
    protected UILockListener lockListener;

    public UISynchronizer(Display display, UILockListener lock) {
        super(display);
        this.lockListener = lock;
    }

    public void syncExec(Runnable runnable) {
        //if this thread is the UI or this thread does not own any locks, just do the syncExec
        if ((runnable == null) || lockListener.isUI()
                || !lockListener.isLockOwner()) {
            super.syncExec(runnable);
            return;
        }
        Semaphore work = new Semaphore(runnable);
        work.setOperationThread(Thread.currentThread());
        lockListener.addPendingWork(work);
        asyncExec(new Runnable() {
            public void run() {
                lockListener.doPendingWork();
            }
        });
        try {
            //even if the UI was not blocked earlier, it might become blocked
            //before it can serve the asyncExec to do the pending work
            do {
                if (lockListener.isUIWaiting())
                    lockListener.interruptUI();
            } while (!work.acquire(1000));
        } catch (InterruptedException e) {
        }
    }
}