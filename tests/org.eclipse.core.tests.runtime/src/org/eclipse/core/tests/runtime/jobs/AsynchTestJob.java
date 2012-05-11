/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.TestBarrier;

/**
 * A job that executes asynchronously on a separate thread
 */
class AsynchTestJob extends Job {
	private int[] status;
	private int index;

	public AsynchTestJob(String name, int[] status, int index) {
		super(name);
		this.status = status;
		this.index = index;
	}

	public IStatus run(IProgressMonitor monitor) {
		status[index] = TestBarrier.STATUS_RUNNING;
		AsynchExecThread t = new AsynchExecThread(monitor, this, 100, 10, getName(), status, index);
		TestBarrier.waitForStatus(status, index, TestBarrier.STATUS_START);
		t.start();
		status[index] = TestBarrier.STATUS_WAIT_FOR_START;
		return Job.ASYNC_FINISH;
	}

}
