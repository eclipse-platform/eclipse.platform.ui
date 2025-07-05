/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
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
 *     Christoph Läubrich - extract into own class
 *******************************************************************************/
package org.eclipse.jface.text.reconciler;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Background thread for the reconciling activity.
 */
class ReconcilerJob extends Job {

	/** Has the reconciler been canceled. */
	private boolean fCanceled= false;

	/** Has the reconciler been reset. */
	private boolean fReset= false;

	/** Some changes need to be processed. */
	private boolean fIsDirty= false;

	/** Is a reconciling strategy active. */
	private boolean fIsActive= false;

	private volatile boolean fIsAlive;

	private boolean started;

	private AbstractReconciler fReconciler;

	/**
	 * Creates a new background thread. The thread runs with minimal priority.
	 *
	 * @param name the thread's name
	 */
	public ReconcilerJob(String name, AbstractReconciler reconciler) {
		super(name);
		fReconciler= reconciler;
		setPriority(Job.DECORATE);
		setSystem(true);
	}

	/**
	 * Returns whether a reconciling strategy is active right now.
	 *
	 * @return <code>true</code> if a activity is active
	 */
	public boolean isActive() {
		return fIsActive;
	}

	/**
	 * Returns whether some changes need to be processed.
	 *
	 * @return <code>true</code> if changes wait to be processed
	 * @since 3.0
	 */
	public synchronized boolean isDirty() {
		return fIsDirty;
	}

	/**
	 * Cancels the background thread.
	 */
	public void doCancel() {
		fCanceled= true;
		IProgressMonitor pm= fReconciler.getProgressMonitor();
		if (pm != null)
			pm.setCanceled(true);
		synchronized (fReconciler.fDirtyRegionQueue) {
			fReconciler.fDirtyRegionQueue.notifyAll();
		}
	}

	/**
	 * Suspends the caller of this method until this background thread has emptied the dirty region
	 * queue.
	 */
	public void suspendCallerWhileDirty() {
		fReconciler.signalWaitForFinish();
		boolean isDirty;
		do {
			synchronized (fReconciler.fDirtyRegionQueue) {
				isDirty= fReconciler.fDirtyRegionQueue.getSize() > 0;
				if (isDirty) {
					try {
						fReconciler.fDirtyRegionQueue.wait();
					} catch (InterruptedException x) {
					}
				}
			}
		} while (isDirty);
	}

	/**
	 * Reset the background thread as the text viewer has been changed,
	 */
	public void reset() {

		if (fReconciler.fDelay > 0) {

			synchronized (this) {
				fIsDirty= true;
				fReset= true;
			}
			synchronized (fReconciler.fDirtyRegionQueue) {
				fReconciler.fDirtyRegionQueue.notifyAll(); // wake up wait(fDelay);
			}

		} else {

			synchronized (this) {
				fIsDirty= true;
			}

			synchronized (fReconciler.fDirtyRegionQueue) {
				fReconciler.fDirtyRegionQueue.notifyAll();
			}
		}
		synchronized (this) {
			started= false;
		}
		fReconciler.informNotFinished();
		fReconciler.reconcilerReset();
	}

	/**
	 * The background activity. Waits until there is something in the queue managing the changes
	 * that have been applied to the text viewer. Removes the first change from the queue and
	 * process it.
	 * <p>
	 * Calls {@link AbstractReconciler#initialProcess()} on entrance.
	 * </p>
	 */
	@Override
	public IStatus run(IProgressMonitor monitor) {
		fIsAlive= true;
		fReconciler.delay();

		if (fCanceled)
			return Status.CANCEL_STATUS;

		fReconciler.initialProcess();

		while (!fCanceled) {

			fReconciler.delay();

			if (fCanceled)
				break;

			if (!isDirty()) {
				fReconciler.waitFinish= false; //signalWaitForFinish() was called but nothing todo
				continue;
			}

			synchronized (this) {
				if (fReset) {
					fReset= false;
					continue;
				}
			}

			DirtyRegion r= null;
			synchronized (fReconciler.fDirtyRegionQueue) {
				r= fReconciler.fDirtyRegionQueue.removeNextDirtyRegion();
			}

			fIsActive= true;

			fReconciler.getProgressMonitor().setCanceled(false);

			fReconciler.process(r);

			synchronized (fReconciler.fDirtyRegionQueue) {
				if (0 == fReconciler.fDirtyRegionQueue.getSize()) {
					synchronized (this) {
						fIsDirty= fReconciler.getProgressMonitor().isCanceled();
					}
					fReconciler.fDirtyRegionQueue.notifyAll();
				}
			}

			fIsActive= false;
		}
		fIsAlive= false;
		return Status.OK_STATUS;
	}

	public boolean isAlive() {
		return fIsAlive;
	}

	public synchronized void start() {
		if (!started) {
			started= true;
			schedule();
		}
	}

	@Override
	public boolean belongsTo(Object family) {
		return family == fReconciler.getTextViewer() || AbstractReconciler.class == family;
	}

}
