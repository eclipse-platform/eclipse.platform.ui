/**********************************************************************
 * Copyright (c) 2004 Jeremiah Lott and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Jeremiah Lott (jeremiah.lott@timesys.com) - Initial implementation
 *   IBM Added comments, removed printlns, and incorporated into platform test suites
 **********************************************************************/
package org.eclipse.ui.tests.concurrency;

import junit.framework.TestCase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

/**
 * This is a regression test for a case where a recursive attempt to syncExec
 * from within code that owns a lock would cause deadlock. See bug 76378 for details.
 */
public class NestedSyncExecDeadlockTest extends TestCase {

	private class ResourceListener implements IResourceChangeListener {
		public void resourceChanged(IResourceChangeEvent event) {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
				}
			});
		}
	}

	private IResourceChangeListener listener;
	private IProject project;

	private IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public NestedSyncExecDeadlockTest() {
		super();
	}

	public NestedSyncExecDeadlockTest(String name) {
		super(name);
	}

	public void doTest(final long timeToSleep) throws Exception {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(new Shell());
		dialog.run(true, false, new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor pm) {
				Display.getDefault().syncExec(new Runnable() {
					public void run() {
						try {
							workspace.run(new IWorkspaceRunnable() {
								public void run(IProgressMonitor mon) throws CoreException {
									project.touch(null);
									try {
										// wait long enough to be sure to trigger notification
										Thread.sleep(timeToSleep);
									} catch (InterruptedException ex) {
										ex.printStackTrace();
									}
								}
							}, workspace.getRoot(), IResource.NONE, pm);
							workspace.run(new IWorkspaceRunnable() {
								public void run(IProgressMonitor mon) {
								}
							}, pm);

						} catch (CoreException ex) {
							ex.printStackTrace();
						}
					}
				});
			}
		});
	}

	protected void setUp() throws Exception {
		project = workspace.getRoot().getProject("test-deadlock");

		tearDown();

		project.create(null);
		project.open(null);

		listener = new ResourceListener();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}

	protected void tearDown() throws Exception {
		if (listener != null) {
			workspace.removeResourceChangeListener(listener);
		}
		project.delete(true, true, null);
	}

	public void testDeadlock() throws Exception {
		doTest(1000 * 30); // 30 secs almost always locks
	}

	public void testOK() throws Exception {
		doTest(0); // 0 rarely locks		
	}
}