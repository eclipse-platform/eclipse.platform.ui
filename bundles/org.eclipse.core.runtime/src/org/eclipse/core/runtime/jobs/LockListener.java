/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime.jobs;

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.internal.jobs.LockManager;
import org.eclipse.core.internal.runtime.InternalPlatform;

/**
 * A lock listener is notified whenever a thread is about to wait
 * on a lock, and when a thread is about to release a lock.
 * <p>
 * This class is for internal use by the platform-related plug-ins.
 * Clients outside of the base platform should not reference or subclass this class.
 * </p>
 * 
 * @see IJobManager#setLockListener(LockListener)
 * @since 3.0
 */
public class LockListener {
	private final LockManager manager = ((JobManager) InternalPlatform.getDefault().getJobManager()).getLockManager();

	/**
	 * Notification that a thread is about to block on an attempt to acquire a lock.
	 * Returns whether the thread should be granted immediate access to the lock.
	 * 
	 * @param lockOwner the thread that currently owns the lock this thread is
	 * waiting for, or <code>null</code> if unknown.
	 * @return <code>true</code> if the thread should be granted immediate access, 
	 * and <code>false</code> if it should wait for the lock to be available
	 */
	public boolean aboutToWait(Thread lockOwner) {
		return false;
	}

	/**
	 * Notification that a thread is about to release a lock.
	 */
	public void aboutToRelease() {
	}

	/**
	 * Returns whether this thread currently owns any locks
	 * @return <code>true</code> if this thread owns any locks, and 
	 * <code>false</code> otherwise.
	 */
	protected final boolean isLockOwnerThread() {
		return manager.isLockOwner();
	}
}