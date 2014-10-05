/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.tests.databinding.observable;

import java.util.LinkedList;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 3.2
 * 
 */
public class LockRealm extends Realm {

	private LinkedList queue;
	private ILock lock;
	private Job job;
	private boolean lockAcquired;

	public LockRealm() {
		queue = new LinkedList();
		lock = Job.getJobManager().newLock();
		job = new Job("Lock Realm Job") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				for (Runnable runnable; (runnable = dequeue()) != null;) {
					acquireLock();
					try {
						safeRun(runnable);
					} finally {
						releaseLock();
					}
				}
				return Status.OK_STATUS;
			}
		};
		job.setSystem(true);
	}

	@Override
	protected void syncExec(Runnable runnable) {
		acquireLock();
		try {
			safeRun(runnable);
		} finally {
			releaseLock();
		}
	}

	@Override
	public void asyncExec(Runnable runnable) {
		enqueue(runnable);
		job.schedule();
	}

	/**
	 * @param runnable
	 */
	private void enqueue(Runnable runnable) {
		synchronized (queue) {
			queue.addLast(runnable);
		}
	}

	private Runnable dequeue() {
		synchronized (queue) {
			if (queue.isEmpty()) {
				return null;
			}
			return (Runnable) queue.getFirst();
		}
	}

	@Override
	public boolean isCurrent() {
		return lockAcquired;
	}

	private void acquireLock() {
		lock.acquire();
		lockAcquired = true;
	}

	private void releaseLock() {
		lockAcquired = false;
		lock.release();
	}

}
