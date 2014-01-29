/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import java.lang.reflect.InvocationTargetException;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.TestBarrier;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.UIJob;

/**
 * Regression test for bug 269121. The test verifies that deadlock described in
 * the bug is correctly resolved. Scenario:
 * <ol>
 * <li>UIJob job is created and a rule is set on the job using job.setRule(rule)
 * call.</li>
 * <li>WorkspaceModifyOperation operation is created with a rule conflicting
 * with the rule used for the job.
 * <li>The job is scheduled.</li>
 * <li>The operation is run in the UI thread by ProgressMonitorDialog using
 * dialog.run(false, true, operation) call</li>.
 * </ol>
 * Deadlock occurred because the operation run in the UI thread was waiting for
 * the rule already acquired by the job and asyncExec registered by the job
 * never got a chance to run in the UI thread, thus preventing the job from
 * ending and releasing the rule.
 * <p>
 * The solution is to make sure that operations executed in the UI thread spin
 * the event loop to process pending asyncExecs.
 * </p>
 */
public class TestBug269121 extends TestCase {
	public static Test suite() {
		return new TestSuite(TestBug269121.class);
	}

	public void testBug() throws InterruptedException,
			InvocationTargetException {
		Job job = new UIJob("UI job") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			};
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		final int[] status = new int[] { -1 };
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor) {
				status[0] = TestBarrier.STATUS_DONE;
			}
		};
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				new Shell());
		Job statusJob = new Job("Checking for deadlock") {
			protected IStatus run(IProgressMonitor monitor) {
				try {
					TestBarrier.waitForStatus(status, TestBarrier.STATUS_DONE);
					return Status.OK_STATUS;
				} catch (AssertionFailedError e) {
					// syncExecs are processed by
					// UILockListener.aboutToWait(Thread) without running the
					// event loop so we can cancel the dialog to stop the test
					dialog.getShell().getDisplay().syncExec(new Runnable() {
						public void run() {
							dialog.getProgressMonitor().setCanceled(true);
						}
					});
					return Status.CANCEL_STATUS;
				}
			}
		};
		job.schedule();
		statusJob.schedule();
		try {
			dialog.run(false, true, operation);
		} catch (InterruptedException e) {
			// expected if operation was cancelled
		}
		statusJob.join();
		// run the event loop until the UI job is finished
		while (job.getResult() == null) {
			Display.getCurrent().readAndDispatch();
		}
		job.join();
		assertTrue("Deadlock occurred", statusJob.getResult().isOK());
	}
}
