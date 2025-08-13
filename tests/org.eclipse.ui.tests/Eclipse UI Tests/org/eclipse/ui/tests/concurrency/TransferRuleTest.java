/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * Tests IJobManger#transferRule in conjunction with ModalContext.run.
 * This also exercises the IThreadListener API to allow the runnable to transfer
 * and obtain the rule owned by the calling thread.
 */
public class TransferRuleTest {
	static class TestRule implements ISchedulingRule {
		@Override
		public boolean contains(ISchedulingRule rule) {
			return rule == this;
		}

		@Override
		public boolean isConflicting(ISchedulingRule rule) {
			return rule == this;
		}
	}

	/**
	 * A simple runnable that uses the IThreadListener interface to transfer
	 * a scheduling rule.
	 */
	static class TestRunnable implements IRunnableWithProgress, IThreadListener {
		Throwable error;
		private final ISchedulingRule rule;

		public TestRunnable(ISchedulingRule aRule) {
			this.rule = aRule;
		}

		@Override
		public void run(IProgressMonitor monitor) {
			//if we already have an error don't run the rest of the test
			if (error != null) {
				return;
			}
			try {
				try {
					//acquire the rule that was transferred (will hang if the rule transfer failed)
					Job.getJobManager().beginRule(rule, monitor);
				} finally {
					Job.getJobManager().endRule(rule);
				}
			} catch (Throwable t) {
				//remember any error so we can fail the test
				error = t;
			}
		}

		@Override
		public void threadChange(Thread thread) {
			try {
				Job.getJobManager().transferRule(rule, thread);
			} catch (Throwable t) {
				//remember any error so we can fail the test
				error = t;
			}
		}
	}

	@Test
	public void testModalContextTransfer() throws Throwable {
		ISchedulingRule rule = new TestRule();
		TestRunnable runnable = new TestRunnable(rule);
		try {
			//first get the rule in the test thread
			Job.getJobManager().beginRule(rule, null);
			//now execute the runnable using ModalContext - the rule should transfer correctly
			new ProgressMonitorDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell()).run(true, true, runnable);
		} finally {
			//must release the rule when finished
			Job.getJobManager().endRule(rule);
		}
		//check the runnable for errors
		if (runnable.error != null) {
			throw runnable.error;
		}
	}
}
