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
import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IThreadListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

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
public class TestBug105491 extends TestCase {
	class TransferTestOperation extends WorkspaceModifyOperation implements IThreadListener {
		@Override
		public void execute(final IProgressMonitor pm) {
			//clients assume this would not deadlock because it runs in an asyncExec
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
					try {
						dialog.run(true, false, new WorkspaceModifyOperation() {
							@Override
							protected void execute(IProgressMonitor monitor) {}
						});
					} catch (InvocationTargetException e) {
						e.printStackTrace();
						fail(e.getMessage());
					} catch (InterruptedException e) {
						//ignore
					}
				}
			});
		}

		@Override
		public void threadChange(Thread thread) {
			Platform.getJobManager().transferRule(workspace.getRoot(), thread);
		}
	}

	private IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public TestBug105491() {
		super();
	}

	public TestBug105491(String name) {
		super(name);
	}

	/**
	 * Performs the test
	 */
	public void testBug() throws CoreException {
		workspace.run(new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) {
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
				try {
					dialog.run(true, false, new TransferTestOperation());
				} catch (InvocationTargetException e) {
					e.printStackTrace();
					fail(e.getMessage());
				} catch (InterruptedException e) {
					//ignore
				}
			}
		}, workspace.getRoot(), IResource.NONE, null);
	}
}