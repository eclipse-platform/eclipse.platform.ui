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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.WorkbenchPlugin;

import junit.framework.TestCase;

/**
 * This tests the simple traditional deadlock of a thread holding a lock trying
 * to perform a syncExec, while the UI thread is waiting for that lock.
 * UISynchronizer and UILockListener conspire to prevent deadlock in this case.
 */
public class SyncExecWhileUIThreadWaitsForLock extends TestCase {

	private List<IStatus> reportedErrors;
	private ILogListener listener;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		reportedErrors = new ArrayList<>();
		listener = (status, plugin) -> reportedErrors.add(status);
		WorkbenchPlugin.getDefault().getLog().addLogListener(listener);
	}

	@Override
	protected void tearDown() throws Exception {
		if (listener != null) {
			WorkbenchPlugin.getDefault().getLog().removeLogListener(listener);
		}
		super.tearDown();
	}

	public void testDeadlock() {
		if (Thread.interrupted()) {
			fail("Thread was interrupted at start of test");
		}
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
			if (System.currentTimeMillis()-waitStart > 60000) {
				assertTrue("Deadlock occurred", false);
			}
		}
		//if we get here, the test succeeded

		assertEquals("Unexpected error count reported: " + reportedErrors, 1, reportedErrors.size());
		MultiStatus status = (MultiStatus) reportedErrors.get(0);
		assertEquals("Unexpected child status count reported: " + Arrays.toString(status.getChildren()), 2,
				status.getChildren().length);
		if (Thread.interrupted()) {
			// TODO: re-enable this check after bug 505920 is fixed
			// fail("Thread was interrupted at end of test");
		}
	}
}
