/**********************************************************************
 * Copyright (c) 2003 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tests.runtime.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A job that executes asynchronously on a separate thread
 */
class AsynchTestJob extends Job {
	private int [] status;
	
	public AsynchTestJob(String name, int [] status) {
		super(name);
		this.status = status;
	}
			
	public IStatus run(IProgressMonitor monitor) {
		Thread t = new Thread(new AsynchExecThread(monitor, this, 100, 10, getName(), status));
				
		t.start();
		return Job.ASYNC_FINISH;
	}
		
}
