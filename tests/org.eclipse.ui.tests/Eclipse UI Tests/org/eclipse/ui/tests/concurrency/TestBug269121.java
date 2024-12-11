/*******************************************************************************
 * Copyright (c) 2014, 2017 IBM Corporation and others.
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
 ******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicIntegerArray;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.tests.harness.TestBarrier2;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

import junit.framework.AssertionFailedError;

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
 * dialog.run(false, true, operation) call.</li>
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
public class TestBug269121 {

	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	@Test
	public void testBug() throws InterruptedException,
			InvocationTargetException {
		Job job = new UIJob("UI job") {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};
		job.setRule(ResourcesPlugin.getWorkspace().getRoot());
		final AtomicIntegerArray status = new AtomicIntegerArray(new int[] { -1 });
		WorkspaceModifyOperation operation = new WorkspaceModifyOperation() {
			@Override
			protected void execute(IProgressMonitor monitor) {
				status.set(0, TestBarrier2.STATUS_DONE);
			}
		};
		Shell shell = new Shell();
		final ProgressMonitorDialog dialog = new ProgressMonitorDialog(
				shell);
		Job statusJob = new Job("Checking for deadlock") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					TestBarrier2.waitForStatus(status, TestBarrier2.STATUS_DONE);
					return Status.OK_STATUS;
				} catch (AssertionFailedError e) {
					// syncExecs are processed by
					// UILockListener.aboutToWait(Thread) without running the
					// event loop so we can cancel the dialog to stop the test
					dialog.getShell().getDisplay().syncExec(() -> dialog.getProgressMonitor().setCanceled(true));
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
		assertTrue("Timeout occurred - possible Deadlock. See logging!", statusJob.getResult().isOK());
		shell.close();
	}
}
