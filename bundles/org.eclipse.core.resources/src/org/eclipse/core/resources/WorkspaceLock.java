package org.eclipse.core.resources;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.internal.resources.WorkManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.Semaphore;

public class WorkspaceLock {
	private WorkManager workManager;
public WorkspaceLock(IWorkspace workspace) throws CoreException {
	this.workManager = ((Workspace) workspace).getWorkManager();
}
/**
 * This method needs to be called by subclasses. It is only intended to be called by the
 * platform.
 */
public boolean acquire() throws InterruptedException {
	Semaphore semaphore = workManager.acquire();
	if (semaphore == null)
		return true;
	// XXX: remove println hacks -- ask people first
	System.out.println("Operation waiting to be executed... :-/");
	semaphore.acquire();
	workManager.updateCurrentOperation();
	System.out.println("Operation started... :-)");
	return true;
}
protected Thread getCurrentOperationThread() {
	return workManager.getCurrentOperationThread();
}
public void release() {
	workManager.release();
}
}
