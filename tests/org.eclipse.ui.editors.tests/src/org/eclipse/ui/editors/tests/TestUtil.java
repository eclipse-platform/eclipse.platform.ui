/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.ui.editors.tests;

import org.eclipse.swt.widgets.Display;

import org.eclipse.core.runtime.jobs.Job;

public class TestUtil {

	/**
	 * Process all queued UI events. If called from background thread, does
	 * nothing.
	 */
	public static void runEventLoop() {
		Display display = Display.getCurrent();
		if (display != null && !display.isDisposed()) {
			while (display.readAndDispatch()) {
				// Keep pumping events until the queue is empty
			}
		}
	}

	/**
	 * Utility for waiting until the execution of jobs of any family has
	 * finished or timeout is reached. If no jobs are running, the method waits
	 * given minimum wait time. While this method is waiting for jobs, UI events
	 * are processed.
	 *
	 * @param minTimeMs
	 *            minimum wait time in milliseconds
	 * @param maxTimeMs
	 *            maximum wait time in milliseconds
	 * @return true if the method timed out, false if all the jobs terminated
	 *         before the timeout
	 */
	public static boolean waitForJobs(long minTimeMs, long maxTimeMs) {
		if (maxTimeMs < minTimeMs) {
			throw new IllegalArgumentException("Max time is smaller as min time!");
		}
		final long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < minTimeMs) {
			runEventLoop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Uninterruptable
			}
		}
		while (!Job.getJobManager().isIdle()) {
			if (System.currentTimeMillis() - start >= maxTimeMs) {
				return true;
			}
			runEventLoop();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// Uninterruptable
			}
		}
		return false;
	}

	/**
	 * JUnit 4 rule to clean up state that can otherwise leak through SWT between
	 * tests.
	 */
	public static class CleanupRule extends org.junit.rules.ExternalResource {
		@Override
		protected void after() {
			// Ensure that the Thread.interrupted() flag didn't leak.
			org.junit.Assert.assertFalse("The main thread should not be interrupted at the end of a test", Thread.interrupted());
			// Wait for any outstanding jobs to finish. Protect against deadlock by terminating the wait after a timeout.
			boolean timedOut = waitForJobs(0, java.util.concurrent.TimeUnit.MINUTES.toMillis(3));
			org.junit.Assert.assertFalse("Some Job did not terminate at the end of the test", timedOut);
			// Wait for any pending *syncExec calls to finish
			runEventLoop();
			// Ensure that the Thread.interrupted() flag didn't leak.
			org.junit.Assert.assertFalse("The main thread should not be interrupted at the end of a test", Thread.interrupted());
		}
	}
}