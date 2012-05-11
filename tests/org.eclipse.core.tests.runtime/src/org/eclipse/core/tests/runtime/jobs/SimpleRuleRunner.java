/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
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
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.*;
import org.eclipse.core.tests.harness.TestBarrier;

/**
 * This runnable will try to begin the given rule in the Job Manager.  It will
 * end the rule before returning.
 */
class SimpleRuleRunner implements Runnable {
	private ISchedulingRule rule;
	private IProgressMonitor monitor;
	private int[] status;
	RuntimeException exception;
	private static final IJobManager manager = Job.getJobManager();

	public SimpleRuleRunner(ISchedulingRule rule, int[] status, IProgressMonitor monitor) {
		this.rule = rule;
		this.monitor = monitor;
		this.status = status;
		this.exception = null;
	}

	public void run() {
		//tell the caller that we have entered the run method
		status[0] = TestBarrier.STATUS_RUNNING;
		try {
			try {
				manager.beginRule(rule, monitor);
			} finally {
				manager.endRule(rule);
			}
		} catch (OperationCanceledException e) {
			//ignore
		} catch (RuntimeException e) {
			exception = e;
		} finally {
			status[0] = TestBarrier.STATUS_DONE;
		}
	}
}
