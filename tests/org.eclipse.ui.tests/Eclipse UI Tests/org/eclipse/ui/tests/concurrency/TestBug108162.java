/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.concurrency;

import java.lang.reflect.InvocationTargetException;
import junit.framework.TestCase;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * Tests the following sequence of events:
 * 1) acquire a lock in the UI thread
 * 2) execute an operation (e.g. MoveFilesAndFoldersOperation) which spawns a modal context thread
 * 3) modal context tries to acquire lock held by UI thread
 * 
 * NOTE: This bug has not yet been fixed.  This test illustrates the problem, but must
 * not be added to the parent test suite until the problem has been fixed.
 */
public class TestBug108162 extends TestCase {
	class LockAcquringOperation extends WorkspaceModifyOperation {
		public void execute(final IProgressMonitor pm) {
			//empty operation is sufficient to cause deadlock
		}
	}

	private IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public TestBug108162() {
		super();
	}

	public TestBug108162(String name) {
		super(name);
	}

	/**
	 * Performs the test
	 */
	public void testBug() throws CoreException {
		workspace.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
				try {
					dialog.run(true, false, new LockAcquringOperation());
					//should not succeed
					assertTrue("Should not get here", false);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					fail(e.getMessage());
				} catch (InterruptedException e) {
					//ignore
				} catch (IllegalStateException e) {
					//this failure is expected because it tried to fork and block while owning a lock.
				}
			}
		}, workspace.getRoot(), IResource.NONE, null);
	}
}