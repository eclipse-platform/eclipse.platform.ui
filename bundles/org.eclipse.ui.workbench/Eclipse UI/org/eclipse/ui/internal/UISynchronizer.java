/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
	 * Indicates that the UI is in startup mode and that no non-workbench runnables
	 * should be invoked.
	 */
	protected boolean isStarting = true;

	/**
	 * List of non-workbench Runnables that need executing at some point in the
	 * future
	 */
	protected List pendingStartup = new ArrayList();

	/**
	 * Setting this variable to the value {@link Boolean#TRUE} will allow a thread
	 * to execute code during the startup sequence.
	 */
	public static final ThreadLocal startupThread = new ThreadLocal() {

		@Override
		protected Object initialValue() {
			return Boolean.FALSE;
		}

		@Override
		public void set(Object value) {
			if (value != Boolean.TRUE && value != Boolean.FALSE)
				throw new IllegalArgumentException();
			super.set(value);
		}
	};

	public static final ThreadLocal overrideThread = new ThreadLocal() {
		@Override
		protected Object initialValue() {
			return Boolean.FALSE;
		}

		@Override
		public void set(Object value) {
			if (value != Boolean.TRUE && value != Boolean.FALSE)
				throw new IllegalArgumentException();
			if (value == Boolean.TRUE && ((Boolean) startupThread.get()).booleanValue()) {
				throw new IllegalStateException();
			}
			super.set(value);
		}
	};

	public UISynchronizer(Display display, UILockListener lock) {
		super(display);
		this.lockListener = lock;
	}

	public void started() {
		synchronized (this) {
			if (!isStarting)
				throw new IllegalStateException();
			isStarting = false;
			for (Iterator i = pendingStartup.iterator(); i.hasNext();) {
				Runnable runnable = (Runnable) i.next();
				try {
					// queue up all pending asyncs
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

	@Override
	protected void asyncExec(Runnable runnable) {
		if (runnable != null) {
			synchronized (this) {
				if (isStarting && !(runnable instanceof StartupRunnable) && overrideThread.get() == Boolean.FALSE) {

					// don't run it now, add it to the list of deferred runnables
					pendingStartup.add(runnable);

					return;
				}
			}
		}
		super.asyncExec(runnable);
	}

	@Override
	public void syncExec(Runnable runnable) {

		synchronized (this) {
			if (isStarting && startupThread.get() == Boolean.FALSE
					&& overrideThread.get() == Boolean.FALSE) {
				do {
					try {
						this.wait();
					} catch (InterruptedException e) {
					}
				} while (isStarting);
			}
		}

		// if this thread is the UI or this thread does not own any locks, just do the
		// syncExec
		if ((runnable == null) || lockListener.isUI() || !lockListener.isLockOwner()) {
			super.syncExec(runnable);
			return;
		}
		PendingSyncExec work = new PendingSyncExec(runnable);
		work.setOperationThread(Thread.currentThread());
		lockListener.addPendingWork(work);
		asyncExec(lockListener::doPendingWork);

		try {
			work.waitUntilExecuted(lockListener);
		} catch (InterruptedException e) {
		}
	}
}
