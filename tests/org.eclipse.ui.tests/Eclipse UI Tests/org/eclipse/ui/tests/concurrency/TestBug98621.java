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
public class TestBug98621 extends TestCase {
	class TransferTestOperation extends WorkspaceModifyOperation implements IThreadListener {
		public void execute(final IProgressMonitor pm) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					try {
						workspace.run(new IWorkspaceRunnable() {
							public void run(IProgressMonitor mon) {
								//
							}
						}, workspace.getRoot(), IResource.NONE, null);
					} catch (CoreException ex) {
						ex.printStackTrace();
					}
				}
			});
			//wait until the asyncExec is blocking the UI thread
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//ignore
			}
		}

		public void threadChange(Thread thread) {
			Platform.getJobManager().transferRule(workspace.getRoot(), thread);
		}
	}

	private IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public TestBug98621() {
		super();
	}

	public TestBug98621(String name) {
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