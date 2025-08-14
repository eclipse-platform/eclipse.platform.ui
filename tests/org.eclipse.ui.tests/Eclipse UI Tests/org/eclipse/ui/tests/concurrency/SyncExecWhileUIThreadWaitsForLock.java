/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;
import static org.eclipse.ui.tests.harness.util.UITestUtil.processEventsUntil;
import static org.eclipse.ui.tests.harness.util.UITestUtil.waitForJobs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.views.log.AbstractEntry;
import org.eclipse.ui.internal.views.log.LogEntry;
import org.eclipse.ui.internal.views.log.LogView;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This tests the simple traditional deadlock of a thread holding a lock trying
 * to perform a syncExec, while the UI thread is waiting for that lock.
 * UISynchronizer and UILockListener conspire to prevent deadlock in this case.
 */
public class SyncExecWhileUIThreadWaitsForLock {

	private List<IStatus> reportedErrors;
	private ILogListener listener;
	private LogView logView;
	private IWorkbenchPage activePage;
	private boolean shouldClose;

	@Before
	public void setUp() throws Exception {
		processEvents();
		reportedErrors = new ArrayList<>();
		listener = (status, plugin) -> reportedErrors.add(status);
		activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		String viewId = "org.eclipse.pde.runtime.LogView";
		logView = (LogView) activePage.findView(viewId);
		if (logView == null) {
			shouldClose = true;
			logView = (LogView) activePage.showView(viewId);
		}
		processEvents();
		WorkbenchPlugin.log("Log for test: init log view");
		waitForJobs(100, 10000);
		WorkbenchPlugin.getDefault().getLog().addLogListener(listener);

		logView.handleClear();
		waitForJobs(100, 10000);
		processEventsUntil(() -> logView.getElements().length == 0, 30000);
	}

	@After
	public void tearDown() throws Exception {
		if (listener != null) {
			WorkbenchPlugin.getDefault().getLog().removeLogListener(listener);
		}
		if (shouldClose && logView != null) {
			activePage.hideView(logView);
		}
	}

	@Test
	public void testDeadlock() throws Exception {
		assertFalse(Thread.interrupted());
		final ILock lock = Job.getJobManager().newLock();
		final boolean[] blocked = new boolean[] {false};
		final boolean[] lockAcquired= new boolean[] {false};
		Thread locking = new Thread("SyncExecWhileUIThreadWaitsForLock") {
			@Override
			public void run() {
				try {
					//first make sure this background thread owns the lock
					lock.acquire();
					//spawn an asyncExec that will cause the UI thread to be blocked
					Display.getDefault().asyncExec(() -> {
						blocked[0] = true;
						lock.acquire();
						lock.release();
						blocked[0] = false;
					});
					//wait until the UI thread is blocked waiting for the lock
					while (!blocked[0]) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					//now attempt to do a syncExec that also acquires the lock
					//this should succeed even while the above asyncExec is blocked, thanks to UISynchronizer
					Display.getDefault().syncExec(() -> {
						try {
							// use a timeout to avoid deadlock in case of regression
							if (lock.acquire(60000)) {
								// this flag is used to verify that we actually acquired the lock
								lockAcquired[0] = true;
								lock.release();
							}
						} catch (InterruptedException e) {
						}
					});
				} finally {
					lock.release();
				}
			}
		};
		locking.start();
		//wait until we succeeded to acquire the lock in the UI thread
		long waitStart = System.currentTimeMillis();
		Display display = Display.getDefault();
		while (!lockAcquired[0]) {
			//spin event loop so that asyncExed above gets run
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			//if we waited too long, fail the test
			if (System.currentTimeMillis() - waitStart > 60000) {
				fail("Deadlock occurred");
			}
		}
		//if we get here, the test succeeded

		assertEquals("Unexpected error count reported: " + reportedErrors, 1, reportedErrors.size());
		MultiStatus status = (MultiStatus) reportedErrors.get(0);
		assertEquals("Unexpected child status count reported: " + Arrays.toString(status.getChildren()), 2,
				status.getChildren().length);

		processEvents();
		processEventsUntil(() -> logView.getElements().length > 0, 30000);
		AbstractEntry[] elements = logView.getElements();
		List<AbstractEntry> list = Arrays.asList(elements).stream()
				.filter(x -> ((LogEntry) x).getMessage().startsWith("To avoid deadlock")).toList();
		assertEquals("Unexpected list content: " + list, 1, list.size());
		AbstractEntry[] children = list.get(0).getChildren(list.get(0));
		assertEquals("Unexpected children content: " + Arrays.toString(children), 2, children.length);
		String label = ((LogEntry) children[0]).getMessage();
		assertTrue("Unexpected: " + label, label.startsWith("UI thread"));
		label = ((LogEntry) children[1]).getMessage();
		assertTrue("Unexpected: " + label, label.startsWith("SyncExec"));
		assertFalse(Thread.interrupted());
	}
}
