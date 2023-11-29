/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
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
 */
public class LockRealm extends Realm {

	private LinkedList<Runnable> queue;
	private ILock lock;
	private Job job;
	private boolean lockAcquired;

	public LockRealm() {
		queue = new LinkedList<>();
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
			return queue.getFirst();
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
