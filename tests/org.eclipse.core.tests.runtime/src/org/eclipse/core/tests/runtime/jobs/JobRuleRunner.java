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

import org.eclipse.core.internal.jobs.JobManager;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.tests.harness.TestBarrier;

/**
 * 
 */
class JobRuleRunner extends Job {
	private ISchedulingRule rule;
	private int[] status;
	private int index;
	private int numRepeats;
	private boolean reportBlocking;
	private static final IJobManager manager = JobManager.getInstance();

	/**
	 * This job will start applying the given rule in the manager
	 */
	public JobRuleRunner(String name, ISchedulingRule rule, int[] status, int index, int numRepeats, boolean reportBlocking) {
		super(name);
		this.status = status;
		this.rule = rule;
		this.index = index;
		this.numRepeats = numRepeats;
		this.reportBlocking = reportBlocking;
	}

	protected IStatus run(IProgressMonitor monitor) {
		//begin executing the job
		monitor.beginTask(getName(), numRepeats);
		try {
			//set the status flag to START
			status[index] = TestBarrier.STATUS_START;
			for (int i = 0; i < numRepeats; i++) {
				//wait until the tester allows this job to run again
				TestBarrier.waitForStatusNoFail(status, index, TestBarrier.STATUS_WAIT_FOR_RUN);
				//create a hook that would notify this thread when this job was blocked on a rule (if needed)
				BlockingMonitor bMonitor = null;
				if (reportBlocking)
					bMonitor = new BlockingMonitor(status, index);

				//start the given rule in the manager
				manager.beginRule(rule, bMonitor);
				//set status to RUNNING
				status[index] = TestBarrier.STATUS_RUNNING;

				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;

				//wait until tester allows this job to finish
				TestBarrier.waitForStatusNoFail(status, index, TestBarrier.STATUS_WAIT_FOR_DONE);
				//end the given rule
				manager.endRule(rule);
				//set status to DONE
				status[index] = TestBarrier.STATUS_DONE;

				monitor.worked(1);
				Thread.yield();
			}

		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}