/*******************************************************************************
 * Copyright (c) 2005, 2019 IBM Corporation and others.
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
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * Tests the following sequence of events:
 * 1) acquire a lock in the UI thread
 * 2) execute an operation (e.g. MoveFilesAndFoldersOperation) which spawns a modal context thread
 * 3) modal context tries to acquire lock held by UI thread
 *
 * This sequence would cause a deadlock, so an exception is thrown by ModalContext.
 * This test asserts that the exception is thrown and that deadlock does not occur.
 */
public class TestBug108162 {

	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	static class LockAcquiringOperation extends WorkspaceModifyOperation {
		@Override
		public void execute(final IProgressMonitor pm) {
			//empty operation is sufficient to cause deadlock
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
				dialog.run(true, false, new LockAcquiringOperation());
				// should not succeed
				fail("Should not get here");
			} catch (InvocationTargetException | InterruptedException | IllegalStateException e) {
				// this failure is expected because it tried to fork and block while owning a
				// lock.
			}
		}, workspace.getRoot(), IResource.NONE, null);
		shell.close();
	}
}