/**********************************************************************
 * Copyright (c) 2004, 2017 Jeremiah Lott and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jeremiah Lott (jeremiah.lott@timesys.com) - Initial implementation
 *   IBM Added comments, removed printlns, and incorporated into platform test suites
 **********************************************************************/
package org.eclipse.ui.tests.concurrency;

import static org.junit.Assert.assertFalse;

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
import org.eclipse.ui.tests.SwtLeakTestWatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestWatcher;

/**
 * This is a regression test for a case where a recursive attempt to syncExec
 * from within code that owns a lock would cause deadlock. See bug 76378 for details.
 */
public class NestedSyncExecDeadlockTest {

	@Rule
	public TestWatcher swtLeakTestWatcher = new SwtLeakTestWatcher();

	private static class ResourceListener implements IResourceChangeListener {
		@Override
		public void resourceChanged(IResourceChangeEvent event) {
			Display.getDefault().syncExec(() -> {
			});
		}
	}

	private IResourceChangeListener listener;
	private IProject project;

	private final IWorkspace workspace = ResourcesPlugin.getWorkspace();

	public void doTest(final long timeToSleep) throws Exception {
		Shell shell = new Shell();
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
		dialog.run(true, false, new WorkspaceModifyOperation() {
			@Override
			public void execute(final IProgressMonitor pm) {
				Display.getDefault().syncExec(() -> {
					try {
						workspace.run((IWorkspaceRunnable) mon -> {
							project.touch(null);
							try {
								// wait long enough to be sure to trigger notification
								Thread.sleep(timeToSleep);
							} catch (InterruptedException ex) {
								ex.printStackTrace();
							}
						}, workspace.getRoot(), IResource.NONE, pm);
						workspace.run((IWorkspaceRunnable) mon -> {
						}, pm);

					} catch (CoreException ex) {
						ex.printStackTrace();
					}
				});
			}
		});
		shell.close();
	}

	@Before
	public void setUp() throws Exception {
		project = workspace.getRoot().getProject("test-deadlock");

		tearDown();

		project.create(null);
		project.open(null);

		listener = new ResourceListener();
		workspace.addResourceChangeListener(listener, IResourceChangeEvent.POST_CHANGE);
	}

	@After
	public void tearDown() throws Exception {
		if (listener != null) {
			workspace.removeResourceChangeListener(listener);
		}
		project.delete(true, true, null);
	}

	@Test
	public void testDeadlock() throws Exception {
		doTest(1000 * 30); // 30 secs almost always locks
		assertFalse(Thread.interrupted());
	}

	@Test
	public void testOK() throws Exception {
		doTest(0); // 0 rarely locks
		assertFalse(Thread.interrupted());
	}
}