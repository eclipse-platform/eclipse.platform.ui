/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.runtime.jobs;

/**
 * A lock listener is notified whenever a thread is about to wait
 * on a lock, and when a thread is about to release a lock.
 * <p>
 * This interface is for internal use by the platform-related plug-ins.
 * Clients should not implement this interface.
 * </p>
 */
public interface ILockListener {
	/**
	 * Notification that a thread is about to block on an attempt to acquire a lock.
	 * 
	 * @param lockOwner the thread that currently owns the lock this thread is
	 * waiting for
	 */
	public void aboutToWait(Thread lockOwner);
	/**
	 * Notification that a thread is about to release a lock.
	 */
	public void aboutToRelease();
}
