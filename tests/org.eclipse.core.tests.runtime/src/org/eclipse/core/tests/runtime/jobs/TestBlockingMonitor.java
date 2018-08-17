/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
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

	@Override
	public void clearBlocked() {
		//leave empty for now
	}

	@Override
	public boolean isCanceled() {
		return cancelled;
	}

	@Override
	public void setBlocked(IStatus reason) {
		barrier.setStatus(TestBarrier.STATUS_BLOCKED);
	}

	@Override
	public void setCanceled(boolean b) {
		cancelled = true;
	}
}
