/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.junit.Test;

/**
 * Tests the following sequence of events:
 * 1) Workspace operation starts in the UI thread.  Workspace lock is taken, and a modal context thread is forked (call this modal context MC1).
 * 2) Operation transfers the workspace lock to MC1 using IJobManager.transferRule
 * 3) Operation runs in MC1, scheduling an asyncExec.
 * 4) MC1 passes the scheduling rule back to UI thread, and exits
 * 5) After passing the rule back to the UI thread, but before MC1 dies, the asyncExec is run.
 * 6) The asyncExec forks another model context (MC2), and blocks the UI thread in another event loop.
 * 7) MC2 tries to acquire the workspace lock and deadlocks, because at this point it has been transferred to the UI thread
 *
 * NOTE: This bug has not yet been fixed.  This test illustrates the problem, but must
 * not be added to the parent test suite until the problem has been fixed.
 */
public class TestBug105491 {
	class TransferTestOperation extends WorkspaceModifyOperation {
		@Override
		public void execute(final IProgressMonitor pm) {
			//clients assume this would not deadlock because it runs in an asyncExec
			Display.getDefault().asyncExec(() -> {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
				try {
					dialog.run(true, false, new WorkspaceModifyOperation() {
						@Override
						protected void execute(IProgressMonitor monitor) {
						}
					});
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
					fail(e1.getMessage());
				} catch (InterruptedException e2) {
					// ignore
				}
			});
		}

		@Override
		public void threadChange(Thread thread) {
			Job.getJobManager().transferRule(workspace.getRoot(), thread);
		}
	}

	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	/**
	 * Performs the test
	 */
	@Test
	public void testBug() throws CoreException {
		assertFalse(Thread.interrupted());

		if (Util.isWindows()) {
			// unstable on Windows with 2 cores, see bug 543693
			return;
		}
		workspace.run((IWorkspaceRunnable) monitor -> {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
			try {
				dialog.run(true, false, new TransferTestOperation());
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				fail(e1.getMessage());
			} catch (InterruptedException e2) {
				// ignore
			}
		}, workspace.getRoot(), IResource.NONE, null);
		assertFalse(Thread.interrupted());
	}
}