/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.operations;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.Test;

import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * Runs the operations API tests and workbench operation history tests from
 * background threads
 *
 * @since 3.1
 */
public class MultiThreadedOperationsTests {

	@Test
	public void testOperationsAPIinThreads() {
		// run the operations API test suite from three different jobs.
		class OperationsTestJob extends Job {
			public OperationsTestJob() {
				super("Operations Test Job");
			}

			@Override
			public IStatus run(IProgressMonitor monitor) {
				// System.out.println("Running OperationsAPITest from background job");
				new TestSuite(OperationsAPITest.class).run(new TestResult());
				// System.out.println("Running WorkbenchOperationHistoryTests from background job");
				new TestSuite(WorkbenchOperationHistoryTests.class).run(new TestResult());
				return Status.OK_STATUS;
			}
		}

		OperationsTestJob job1 = new OperationsTestJob();
		OperationsTestJob job2 = new OperationsTestJob();
		OperationsTestJob job3 = new OperationsTestJob();

		job1.schedule();
		job2.schedule();
		job3.schedule();

		// don't return from the test method until the jobs are complete.
		try {
			job1.join();
		} catch (InterruptedException e) {
			System.out.println("Job interrupted in test case");
		}
		try {
			job2.join();
		} catch (InterruptedException e) {
			System.out.println("Job interrupted in test case");
		}
		try {
			job3.join();
		} catch (InterruptedException e) {
			System.out.println("Job interrupted in test case");
		}
	}
}
