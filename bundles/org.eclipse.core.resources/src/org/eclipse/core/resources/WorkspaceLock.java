/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.internal.resources.WorkManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Semaphore;

/**
 * A lock used to control write access to the resources in a workspace.
 * Clients may subclass.
 * 
 * @see IWorkspace#setWorkspaceLock
 */

public class WorkspaceLock {
	private WorkManager workManager;

/**
 * Returns a new workspace lock.
 */
public WorkspaceLock(IWorkspace workspace) throws CoreException {
	this.workManager = ((Workspace) workspace).getWorkManager();
}
/**
 * Attempts to acquire this lock.  Callers will block indefinitely until this lock comes
 * available to them.  
 * <p>
 * Clients may extend this method but should not otherwise call it.
 * </p>
 * @see #release
 */
public boolean acquire() throws InterruptedException {
	Semaphore semaphore = workManager.acquire();
	if (semaphore == null)
		return true;
	if (Workspace.DEBUG)
		System.out.println("[" + Thread.currentThread() + "] Operation waiting to be executed... :-/"); //$NON-NLS-1$ //$NON-NLS-2$
	try {
		semaphore.acquire();
	} catch (InterruptedException e) {
		if (Workspace.DEBUG)
			System.out.println("[" + Thread.currentThread() + "] Operation interrupted while waiting... :-|"); //$NON-NLS-1$ //$NON-NLS-2$
		throw e;
	}	
	workManager.updateCurrentOperation();
	if (Workspace.DEBUG)
		System.out.println("[" + Thread.currentThread() + "] Operation started... :-)"); //$NON-NLS-1$ //$NON-NLS-2$
	return true;
}
/**
 * Returns the thread that currently owns the workspace lock.
 */
protected Thread getCurrentOperationThread() {
	return workManager.getCurrentOperationThread();
}
/**
 * Releases this lock allowing others to acquire it.
 * <p>
 * Clients may extend this method but should not otherwise call it.
 * </p>
 * @see #acquire
 */
public void release() {
	workManager.release();
}
/**
 * Returns whether the workspace tree is locked
 * for resource changes.
 *
 * @return <code>true</code> if the tree is locked, otherwise
 *    <code>false</code>
 */
protected boolean isTreeLocked() {
	return workManager.isTreeLocked();
}
}
