package org.eclipse.ui.internal;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;

public class UISynchronizer extends Synchronizer {
	protected UIWorkspaceLock uiLock;
public UISynchronizer(Display display, UIWorkspaceLock lock) {
	super(display);
	this.uiLock = lock;
}
public void syncExec(Runnable runnable) {
	if ((runnable == null) || uiLock.isUI() || !uiLock.isCurrentOperation()) {
		super.syncExec(runnable);
		return;
	}
	Runnable runOnce = new Runnable() {
		public void run() {
			uiLock.doPendingWork();
		}
	};
	Semaphore work = new Semaphore(runnable);
	work.setOperationThread(Thread.currentThread());
	uiLock.addPendingWork(work);
	if (!uiLock.isUIWaiting())
		asyncExec(runOnce);
	else
		uiLock.interruptUI();
	try {
		work.acquire();
	} catch (InterruptedException e) {
	}
}
}
