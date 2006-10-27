/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jface.databinding.observable;

import java.util.LinkedList;

/**
 * Subclasses must override at least one of asyncExec()/syncExec(). For realms
 * based on a designated thread, it may be easier to implement asyncExec and
 * keep the default implementation of syncExec. For realms based on holding a
 * lock, it may be easier to implement syncExec and keep the default
 * implementation of asyncExec.
 * 
 * @since 1.1
 * 
 */
public abstract class Realm {

	private static ThreadLocal defaultRealm = new ThreadLocal();

	/**
	 * Returns the default realm for the calling thread, or <code>null</code>
	 * if no default realm has been set.
	 * 
	 * @return the default realm, or <code>null</code>
	 */
	public static Realm getDefault() {
		return (Realm) defaultRealm.get();
	}

	/**
	 * Sets the default realm for the calling thread. Each thread can have its
	 * own default realm.
	 * 
	 * @param realm
	 *            the realm, or <code>null</code> to clear the current default
	 *            realm
	 */
	public static void setDefault(Realm realm) {
		defaultRealm.set(realm);
	}

	/**
	 * @return true if the caller is executing in this realm. This method must
	 *         not have side-effects (such as, for example, implicitly placing
	 *         the caller in this realm).
	 */
	abstract public boolean isCurrent();

	private Thread workerThread;

	LinkedList workQueue = new LinkedList();

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked from
	 * within this realm. If the caller is executing in this realm, the
	 * runnable's run method is invoked directly, otherwise it is run at the
	 * next reasonable opportunity using asyncExec.
	 * 
	 * @param runnable
	 */
	public void exec(Runnable runnable) {
		if (isCurrent()) {
			runnable.run();
		} else {
			asyncExec(runnable);
		}
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked from
	 * within this realm at the next reasonable opportunity. The caller of this
	 * method continues to run in parallel, and is not notified when the
	 * runnable has completed.
	 * 
	 * @param runnable
	 */
	public void asyncExec(Runnable runnable) {
		synchronized (workQueue) {
			ensureWorkerThreadIsRunning();
			workQueue.addLast(runnable);
			workQueue.notifyAll();
		}
	}

	/**
	 * 
	 */
	private void ensureWorkerThreadIsRunning() {
		if (workerThread == null) {
			workerThread = new Thread() {
				public void run() {
					try {
						while (true) {
							Runnable work = null;
							synchronized (workQueue) {
								while (workQueue.isEmpty()) {
									workQueue.wait();
								}
								work = (Runnable) workQueue.removeFirst();
							}
							syncExec(work);
						}
					} catch (InterruptedException e) {
						// exit
					}
				}
			};
			workerThread.run();
		}
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to be invoked from
	 * within this realm at the next reasonable opportunity. This method is
	 * blocking the caller until the runnable completes.
	 * <p>
	 * Note: This class is not meant to be called by clients and therefore has
	 * only protected access.
	 * </p>
	 * 
	 * @param runnable
	 */
	protected void syncExec(Runnable runnable) {
		SyncRunnable syncRunnable = new SyncRunnable(runnable);
		asyncExec(syncRunnable);
		synchronized (syncRunnable) {
			while (!syncRunnable.hasRun) {
				try {
					syncRunnable.wait();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	static class SyncRunnable implements Runnable {
		boolean hasRun = false;

		private Runnable runnable;

		SyncRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		public void run() {
			runnable.run();
			synchronized (this) {
				hasRun = true;
				this.notifyAll();
			}
		}
	}
}
