package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.WorkspaceLock;
import org.eclipse.swt.widgets.Display;

public class UIWorkspaceLock extends WorkspaceLock {
	protected Display display;
	protected Thread ui;
	protected Semaphore pendingWork;
public UIWorkspaceLock(IWorkspace workspace, Display display) throws CoreException {
	super(workspace);
	this.display = display;
}
public boolean acquire() throws InterruptedException {
	if (isUI()) {
		if (getCurrentOperationThread() != null && display.getSyncThread() == getCurrentOperationThread())
			throw new RuntimeException("The resource tree cannot be modified during delta or lifecycle notification.");//$NON-NLS-1$
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
		pendingWork.getRunnable().run();
	} finally {
		Semaphore temp = pendingWork;
		pendingWork = null;
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
