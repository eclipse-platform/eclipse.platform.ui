/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;
import org.eclipse.ui.internal.StartupThreading.StartupRunnable;

public class UISynchronizer extends Synchronizer {
    protected UILockListener lockListener;
    
    /**
	 * Indicates that the UI is in startup mode and that no non-workbench
	 * runnables should be invoked.
	 */
	protected boolean isStarting = true;

	/**
	 * List of non-workbench Runnables that need executing at some point in the future
	 */
	protected List pendingStartup = new ArrayList();

	/**
	 * If this boolean is set to true the synchronizer will fall back to the
	 * behaviour it had prior to 3.3/3.4. Ie: there will be no restriction on
	 * what runnables may be run via a/syncExec calls.
	 * 
	 * @see IPreferenceConstants#USE_32_THREADING
	 */
	private boolean use32Threading = false;

	/**
	 * Setting this variable to the value {@link Boolean#TRUE} will allow a
	 * thread to execute code during the startup sequence.
	 */
	public static final ThreadLocal startupThread = new ThreadLocal() {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.ThreadLocal#initialValue()
		 */
		protected Object initialValue() {
			return Boolean.FALSE;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.ThreadLocal#set(java.lang.Object)
		 */
		public void set(Object value) {
			if (value != Boolean.TRUE && value != Boolean.FALSE)
				throw new IllegalArgumentException();
			super.set(value);
		}
	};
	
	public static final ThreadLocal overrideThread = new ThreadLocal() {
		protected Object initialValue() {
			return Boolean.FALSE;
		}
		public void set(Object value) {
			if (value != Boolean.TRUE && value != Boolean.FALSE)
				throw new IllegalArgumentException();
			if (value == Boolean.TRUE
                    && ((Boolean)startupThread.get()).booleanValue()) {
				throw new IllegalStateException();
			}
			super.set(value);
		}
	};
	
    public UISynchronizer(Display display, UILockListener lock) {
        super(display);
        this.lockListener = lock;
        use32Threading = WorkbenchPlugin.getDefault()
				.getPreferenceStore().getBoolean(
						IPreferenceConstants.USE_32_THREADING);
    }
    
    public void started() {
    	synchronized (this) {
			if (!isStarting)
				throw new IllegalStateException();
			isStarting = false;
			for (Iterator i = pendingStartup.iterator(); i.hasNext();) {
				Runnable runnable = (Runnable) i.next();
				try {
					//queue up all pending asyncs
					super.asyncExec(runnable);
				} catch (RuntimeException e) {
					// do nothing
				}
			}
			pendingStartup = null;
			// wake up all pending syncExecs
			this.notifyAll();
    	}    	
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Synchronizer#asyncExec(java.lang.Runnable)
     */
    protected void asyncExec(Runnable runnable) {
    	// the following block should not be invoked if we're using 3.2 threading.
    	if (runnable != null && !use32Threading) {
			synchronized (this) {
				if (isStarting && !(runnable instanceof StartupRunnable)
						&& overrideThread.get() == Boolean.FALSE) {

					// don't run it now, add it to the list of deferred runnables
					pendingStartup.add(runnable);

					return;
				}
			}
		}
    	super.asyncExec(runnable);
    }

	public void syncExec(Runnable runnable) {
		
		synchronized (this) {
			// the following block should not be invoked if we're using 3.2 threading.
			if (isStarting && !use32Threading && startupThread.get() == Boolean.FALSE
					&& overrideThread.get() == Boolean.FALSE) {
				do {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				} while (isStarting);
			}
		}
		
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
                if (lockListener.isUIWaiting()) {
					lockListener.interruptUI();
				}
            } while (!work.acquire(1000));
        } catch (InterruptedException e) {
        }
    }
}
