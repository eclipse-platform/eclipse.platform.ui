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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * Tests the following sequence of events:
 *  1) Lock is acquired in UI thread
 *  2) Modal context thread is started using IThreadListener
 *  3) Lock is transferred to modal context thread
 *  4) Modal context thread performs an asyncExec
 *  5) The asyncExec tries to acquire the same lock held by the modal context
 *  6) The modal context thread exits, thus transferring the rule back to the UI thread
 *  <p>
 *  Now the rule has been transferred back to the UI thread, but the UI thread
 *  is in a wait loop waiting to obtain the rule. The UI thread should realize that
 *  it now owns the lock it is waiting for, and continue with its execution.
 *  <p>
 *  See bug 98621 for more details.
 *  @since 3.2
 */
public class TestBug98621 {

	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	class TransferTestOperation extends WorkspaceModifyOperation {
		@Override
		public void execute(final IProgressMonitor pm) {
			Display.getDefault().asyncExec(() -> {
				try {
					workspace.run((IWorkspaceRunnable) mon -> {
						//
					}, workspace.getRoot(), IResource.NONE, null);
				} catch (CoreException ex) {
					ex.printStackTrace();
				}
			});
			//wait until the asyncExec is blocking the UI thread
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignore
			}
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
		Shell shell = new Shell();
		workspace.run((IWorkspaceRunnable) monitor -> {
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			try {
				dialog.run(true, false, new TransferTestOperation());
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				fail(e1.getMessage());
			} catch (InterruptedException e2) {
				// ignore
			}
		}, workspace.getRoot(), IResource.NONE, null);
		shell.close();
	}
}