/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.runtime.CoreException;

/**
 * A lock used to control write access to the resources in a workspace.
 * Clients may subclass.
 * 
 * @see IWorkspace#setWorkspaceLock(WorkspaceLock)
 * @deprecated it is no longer possible to override the workspace lock behavior.
 * This functionality is now provided in the platform API by implementing the
 * org.eclipse.core.runtime.jobs.ILockListener interface. This API will be deleted
 * in a future release. See bug 311240 for details.
 */
public class WorkspaceLock {

	/**
	 * Returns a new workspace lock.
	 * @throws CoreException if fails.
	 */
	public WorkspaceLock(IWorkspace workspace) throws CoreException {
		//default implementation does nothing
		//thwart compiler warning
	}

	/**
	 * Attempts to acquire this lock.  Callers will block indefinitely until this lock comes
	 * available to them.  
	 * <p>
	 * Clients may extend this method but should not otherwise call it.
	 * </p>
	 * @throws InterruptedException if fails while acquiring the lock.
	 * @see #release()
	 */
	public boolean acquire() throws InterruptedException {
		//default implementation does nothing
		//thwart compiler warning
		return false;
	}

	/**
	 * Returns the thread that currently owns the workspace lock.
	 */
	protected Thread getCurrentOperationThread() {
		//deprecated API
		return null;
	}

	/**
	 * Releases this lock allowing others to acquire it.
	 * <p>
	 * Clients may extend this method but should not otherwise call it.
	 * </p>
	 * @see #acquire()
	 */
	public void release() {
		//deprecated API
	}

	/**
	 * Returns whether the workspace tree is locked
	 * for resource changes.
	 *
	 * @return <code>true</code> if the tree is locked, otherwise
	 *    <code>false</code>
	 */
	protected boolean isTreeLocked() {
		//deprecated API
		return true;
	}
}
