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

import junit.framework.Assert;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * A runnable class that executes the given job and calls done when it is finished
 */
public class AsynchExecThread implements Runnable {
	private IProgressMonitor current;
	private Job job;
	private int ticks;
	private int tickLength;
	private String jobName;
	private int[] status;
	
	public AsynchExecThread(IProgressMonitor current, Job job, int ticks, int tickLength, String jobName, int [] status) {
		this.current = current;
		this.job = job;
		this.ticks = ticks;
		this.tickLength = tickLength;
		this.jobName = jobName;
		this.status = status;
	}
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		//set the current thread as the execution thread
		job.setThread(Thread.currentThread());
		status[0] = 1;
		int j = 0;
		while(status[0] != 2) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			}
			//sanity test to avoid hanging tests
			Assert.assertTrue("Timeout waiting for thread to be allowed to run", j++ < 100);
		}
		//must have positive work
		current.beginTask(jobName, ticks <= 0 ? 1 : ticks);
		try {
			
			for (int i = 0; i < ticks; i++) {
				current.subTask("Tick: " + i);
				if (current.isCanceled())
					job.done(Status.CANCEL_STATUS);
				try {
					//Thread.yield();
					Thread.sleep(tickLength);
				} catch (InterruptedException e) {
				
				}
				current.worked(1);
			}
			if (ticks <= 0)
				current.worked(1);
		} finally {
			current.done();
			job.done(Status.OK_STATUS);
		}
	}
	

}
