/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.tests.harness.TestBarrier;
import org.eclipse.core.tests.harness.TestProgressMonitor;

/**
 * A test progress monitor that sends a signal to a barrier object when it
 * becomes blocked.
 */
class TestBlockingMonitor extends TestProgressMonitor implements IProgressMonitorWithBlocking {
	private TestBarrier barrier;
	private boolean cancelled;

	public TestBlockingMonitor(int[] status, int index) {
		this(new TestBarrier(status, index));
	}

	public TestBlockingMonitor(TestBarrier barrier) {
		this.barrier = barrier;
	}

	public void clearBlocked() {
		//leave empty for now
	}

	public boolean isCanceled() {
		return cancelled;
	}

	public void setBlocked(IStatus reason) {
		barrier.setStatus(TestBarrier.STATUS_BLOCKED);
	}

	public void setCanceled(boolean b) {
		cancelled = true;
	}
}