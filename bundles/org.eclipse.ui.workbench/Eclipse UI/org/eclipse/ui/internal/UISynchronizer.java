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
