/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceLock;
import org.eclipse.swt.widgets.Display;

public class UIWorkspaceLock extends WorkspaceLock {
	protected Display display;
	protected Thread ui;
	protected Semaphore pendingWork;
	protected boolean pendingWorkStarted;
public UIWorkspaceLock(IWorkspace workspace, Display display) throws CoreException {
	super(workspace);
	this.display = display;
	pendingWorkStarted = false;
}
public boolean acquire() throws InterruptedException {
	if (isUI()) {
		Thread currentOperation = getCurrentOperationThread();
		if (currentOperation != null) {
			if (display.getSyncThread() == currentOperation && isTreeLocked())
				throw new RuntimeException(WorkbenchMessages.getString("UIWorkspaceLock.errorModDuringNotification")); //$NON-NLS-1$
			// If a syncExec was executed from the current operation, it
			// has already acquired the lock. So, just return true.
			if (pendingWork != null && pendingWorkStarted && pendingWork.getOperationThread() == currentOperation) {
				if (isTreeLocked())
					throw new RuntimeException(WorkbenchMessages.getString("UIWorkspaceLock.errorModDuringNotification")); //$NON-NLS-1$
				else
					return true; // we are a nested operation
			}
		}
		ui = Thread.currentThread();
		doPendingWork();
	}
	return super.acquire();
}
void addPendingWork(Semaphore work) {
	pendingWork = work;
}
/**
 * Should always be called from the UI thread.
 */
void doPendingWork() {
	if (pendingWork == null)
		return;
	try {
		pendingWorkStarted = true;
		pendingWork.getRunnable().run();
	} finally {
		// only null it after running
		Semaphore temp = pendingWork;
		// the following lines have to be done in
		// that order to avoid concurrency problems
		pendingWork = null;
		pendingWorkStarted = false;
		temp.release();
	}
}
void interruptUI() {
	display.getThread().interrupt();
}
boolean isCurrentOperation() {
	return getCurrentOperationThread() == Thread.currentThread();
}
boolean isUI() {
	return (!display.isDisposed()) && (display.getThread() == Thread.currentThread());
}
boolean isUIWaiting() {
	return (ui != null) && (Thread.currentThread() != ui);
}
public void release() {
	if (isUI())
		ui = null;
	super.release();
}
}
