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
 * 
 */
class BlockingMonitor extends TestProgressMonitor implements IProgressMonitorWithBlocking {
	int[] status;
	int index;
	public BlockingMonitor(int[] status, int index) {
		this.status = status;
		this.index = index;
	}
	public void setBlocked(IStatus reason) {
		status[index] = TestBarrier.STATUS_BLOCKED;
	}
	public void clearBlocked() {
		//leave empty for now
	}
}