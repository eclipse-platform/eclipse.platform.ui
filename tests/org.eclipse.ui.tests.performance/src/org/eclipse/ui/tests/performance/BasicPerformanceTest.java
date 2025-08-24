/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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

package org.eclipse.ui.tests.performance;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCaseJunit4;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsRule;
import org.junit.Rule;
import org.junit.function.ThrowingRunnable;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * Baseclass for simple performance tests.
 *
 * @since 3.1
 */
public abstract class BasicPerformanceTest extends PerformanceTestCaseJunit4 {

	public static final int NONE = 0;

	public static final int LOCAL = 1;

	public static final int GLOBAL = 2;

	@Rule
	public final CloseTestWindowsRule closeTestWindows = new CloseTestWindowsRule();

	final private boolean tagAsGlobalSummary;

	final private boolean tagAsSummary;

	public BasicPerformanceTest() {
		this(NONE);
		Bundle bundle = FrameworkUtil.getBundle(getClass());
		BundleContext context = bundle != null ? bundle.getBundleContext() : null;
		if (context == null) { // most likely run in a wrong launch mode
			System.err.println("Unable to retrieve bundle context from BasicPerformanceTest; interactive mode is disabled");
			return;
		}
	}

	public BasicPerformanceTest(int tagging) {
		tagAsGlobalSummary = ((tagging & GLOBAL) != 0);
		tagAsSummary = ((tagging & LOCAL) != 0);
	}

	/**
	 * Answers whether this test should be tagged globally.
	 *
	 * @return whether this test should be tagged globally
	 */
	private boolean shouldGloballyTag() {
		return tagAsGlobalSummary;
	}

	/**
	 * Answers whether this test should be tagged locally.
	 *
	 * @return whether this test should be tagged locally
	 */
	private boolean shouldLocallyTag() {
		return tagAsSummary;
	}

	public void tagIfNecessary(String shortName, Dimension dimension) {
		if (shouldGloballyTag()) {
			tagAsGlobalSummary(shortName, dimension);
		}
		if (shouldLocallyTag()) {
			tagAsSummary(shortName, dimension);
		}
	}

	public static void waitForBackgroundJobs() {

		Job backgroundJob = new Job(
				"This is a test job which sits around being low priority until everything else finishes") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				return Status.OK_STATUS;
			}
		};

		backgroundJob.setPriority(Job.DECORATE);

		boolean hadEvents = true;
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null) {
			while (hadEvents) {
				hadEvents = false;
				// Join a low priority job then spin the event loop
				backgroundJob.schedule(0);
				try {
					backgroundJob.join();
				} catch (InterruptedException e) {
				}

				while (display.readAndDispatch()) {
					hadEvents = true;
				}
			}
		}
	}

	/**
	 * Runs the given runnable until either 100 iterations or 4s has elapsed.
	 * Runs a minimum of 3 times.
	 *
	 * @since 3.1
	 */
	public static void exercise(ThrowingRunnable runnable) throws CoreException {
		exercise(runnable, 3, 100, 4000);
	}

	/**
	 * Exercises the given runnable until either the given number of iterations
	 * or the given amount of time has elapsed, whatever occurs first.
	 *
	 * @since 3.1
	 */
	public static void exercise(ThrowingRunnable runnable,
			int minIterations,
			int maxIterations, int maxTime) throws CoreException {
		long startTime = System.currentTimeMillis();

		for (int counter = 0; counter < maxIterations; counter++) {

			try {
				runnable.run();
			} catch (Throwable e) {
				throw new CoreException(new Status(IStatus.ERROR,
						FrameworkUtil.getBundle(BasicPerformanceTest.class)
								.getSymbolicName(), IStatus.OK,
						"An exception occurred", e));
			}

			long curTime = System.currentTimeMillis();
			if (curTime - startTime > maxTime && counter >= minIterations - 1) {
				break;
			}
		}
	}

}
