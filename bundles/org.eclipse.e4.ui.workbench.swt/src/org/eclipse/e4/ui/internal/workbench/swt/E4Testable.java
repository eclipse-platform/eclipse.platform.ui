/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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
package org.eclipse.e4.ui.internal.workbench.swt;

import java.util.Locale;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.testing.TestableObject;
import org.osgi.service.component.annotations.Component;

/**
 * The Workbench's testable object facade to a test harness.
 *
 * @since 3.0
 */
@Component(service = TestableObject.class)
public class E4Testable extends TestableObject {

	private Display display;

	private IWorkbench workbench;

	private boolean oldAutomatedMode;

	private boolean oldIgnoreErrors;

	private volatile Exception displayCheck;

	/**
	 * Constructs a new workbench testable object.
	 */
	public E4Testable() {
		// do nothing
	}

	/**
	 * Initializes the workbench testable with the display and workbench, and
	 * notifies all listeners that the tests can be run.
	 *
	 * @param display
	 *            the display
	 * @param workbench
	 *            the workbench
	 */
	public void init(Display display, IWorkbench workbench) {
		Assert.isNotNull(display);
		Assert.isNotNull(workbench);
		this.display = display;
		this.workbench = workbench;
		if (getTestHarness() != null) {
			// don't use a job, since tests often wait for all jobs to complete
			// before proceeding
			Runnable runnable = () -> {
				// Some tests (notably the startup performance tests) do not
				// want to wait for early startup.
				// Allow this to be disabled by specifying the system
				// property: org.eclipse.ui.testsWaitForEarlyStartup=false
				// For details, see bug 94129 [Workbench] Performance test
				// regression caused by workbench harness change
				if (!"false".equalsIgnoreCase(System.getProperty("org.eclipse.ui.testsWaitForEarlyStartup"))) { //$NON-NLS-1$ //$NON-NLS-2$
					waitForEarlyStartup();
				}
				getTestHarness().runTests();
			};
			new Thread(runnable, "WorkbenchTestable").start(); //$NON-NLS-1$
		}
	}

	/**
	 * Waits for the early startup job to complete.
	 */
	private void waitForEarlyStartup() {
		try {
			Job.getJobManager().join("earlyStartup", null);
		} catch (OperationCanceledException | InterruptedException e) {
			// ignore
		}
	}

	/**
	 * The <code>WorkbenchTestable</code> implementation of this
	 * <code>TestableObject</code> method ensures that the workbench has been
	 * set.
	 */
	@Override
	public void testingStarting() {
		Assert.isNotNull(workbench);
		oldAutomatedMode = ErrorDialog.AUTOMATED_MODE;
		ErrorDialog.AUTOMATED_MODE = true;
		oldIgnoreErrors = SafeRunnable.getIgnoreErrors();
		SafeRunnable.setIgnoreErrors(true);
	}

	/**
	 * The <code>WorkbenchTestable</code> implementation of this
	 * <code>TestableObject</code> method flushes the event queue, runs the test
	 * in a <code>syncExec</code>, then flushes the event queue again.
	 */
	@Override
	public void runTest(Runnable testRunnable) {
		Assert.isNotNull(workbench);
		display.syncExec(new TestExecutionRunnable(testRunnable));
	}

	private final class TestExecutionRunnable implements Runnable {
		private final Runnable testRunnable;

		private TestExecutionRunnable(Runnable testRunnable) {
			this.testRunnable = testRunnable;
		}

		@Override
		public void run() {
			display.addListener(SWT.Dispose,
					e -> {
						// lambda block to allow breakpoint for debugging
						displayCheck = new Exception("Display is disposed");
					});

			try {
				// Run actual test
				testRunnable.run();
			} catch (OutOfMemoryError e) {
				try {
					e.printStackTrace(System.out);
					printMemoryUse();
				} finally {
					System.out.println("Calling System.exit() after OutOfMemoryError");
					System.exit(1);
				}
			}

			while (display.readAndDispatch()) {
				// process all pending tasks
			}

			// Display should be still there
			checkDisplay();
		}
	}

	private static void printMemoryUse() {
		System.gc();
		System.runFinalization();
		System.gc();
		System.runFinalization();
		long max = Runtime.getRuntime().maxMemory();
		long total = Runtime.getRuntime().totalMemory();
		long free = Runtime.getRuntime().freeMemory();
		long used = total - free;
		System.out.print("\n########### Memory usage reported by JVM ########");
		System.out.printf(Locale.GERMAN, "%n%,16d bytes max heap", max);
		System.out.printf(Locale.GERMAN, "%n%,16d bytes heap allocated", total);
		System.out.printf(Locale.GERMAN, "%n%,16d bytes free heap", free);
		System.out.printf(Locale.GERMAN, "%n%,16d bytes used heap", used);
		System.out.println("\n#################################################\n");
	}

	/**
	 * Throws an error with the stack of the thread that disposed the display if
	 * display is already disposed
	 */
	private void checkDisplay() {
		if (displayCheck != null) {
			throw new IllegalStateException("Display shouldn't be disposed yet!", displayCheck);
		}
	}

	/**
	 * The <code>WorkbenchTestable</code> implementation of this
	 * <code>TestableObject</code> method flushes the event queue, then closes
	 * the workbench.
	 */
	@Override
	public void testingFinished() {
		// force events to be processed, and ensure the close is done in the UI
		// thread
		Display.getDefault().syncExec(() -> Assert.isTrue(workbench.close()));
		ErrorDialog.AUTOMATED_MODE = oldAutomatedMode;
		SafeRunnable.setIgnoreErrors(oldIgnoreErrors);
	}
}
