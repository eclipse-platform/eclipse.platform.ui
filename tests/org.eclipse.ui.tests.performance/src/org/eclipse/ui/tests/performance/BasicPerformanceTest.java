/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.tests.performance;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Baseclass for simple performance tests.
 * 
 * @since 3.1
 */
public abstract class BasicPerformanceTest extends UITestCase {

	public static final int NONE = 0;

	public static final int LOCAL = 1;

	public static final int GLOBAL = 2;

	private PerformanceTester tester;

	private IProject testProject;

	final private boolean tagAsGlobalSummary;

	final private boolean tagAsSummary;

	public BasicPerformanceTest(String testName) {
		this(testName, NONE);
	}

	/**
	 * @param testName
	 */
	public BasicPerformanceTest(String testName, int tagging) {
		super(testName);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doSetUp()
	 */
	protected void doSetUp() throws Exception {
		super.doSetUp();
		tester = new PerformanceTester(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		super.doTearDown();
		tester.dispose();
	}

	protected IProject getProject() {
		if (testProject == null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			testProject = workspace.getRoot().getProject(
					UIPerformanceTestSetup.PROJECT_NAME);
		}
		return testProject;
	}

	/**
	 * Asserts default properties of the measurements captured for this test
	 * case.
	 * 
	 * @throws RuntimeException
	 *             if the properties do not hold
	 */
	public void assertPerformance() {
		tester.assertPerformance();
	}

	/**
	 * Asserts that the measurement specified by the given dimension is within a
	 * certain range with respect to some reference value. If the specified
	 * dimension isn't available, the call has no effect.
	 * 
	 * @param dim
	 *            the Dimension to check
	 * @param lowerPercentage
	 *            a negative number indicating the percentage the measured value
	 *            is allowed to be smaller than some reference value
	 * @param upperPercentage
	 *            a positive number indicating the percentage the measured value
	 *            is allowed to be greater than some reference value
	 * @throws RuntimeException
	 *             if the properties do not hold
	 */
	public void assertPerformanceInRelativeBand(Dimension dim,
			int lowerPercentage, int upperPercentage) {
		tester.assertPerformanceInRelativeBand(dim, lowerPercentage,
				upperPercentage);
	}

	public void commitMeasurements() {
		tester.commitMeasurements();
	}

	/**
	 * Called from within a test case immediately before the code to measure is
	 * run. It starts capturing of performance data. Must be followed by a call
	 * to {@link PerformanceTestCase#stopMeasuring()}before subsequent calls to
	 * this method or {@link PerformanceTestCase#commitMeasurements()}.
	 */
	public void startMeasuring() {
		tester.startMeasuring();
	}

	public void stopMeasuring() {
		tester.stopMeasuring();
	}

	/**
	 * Mark the scenario of this test case to be included into the global
	 * performance summary. The summary shows the given dimension of the
	 * scenario and labels the scenario with the short name.
	 * 
	 * @param shortName
	 *            a short (shorter than 40 characters) descritive name of the
	 *            scenario
	 * @param dimension
	 *            the dimension to show in the summary
	 */
	private void tagAsGlobalSummary(String shortName, Dimension dimension) {
		System.out.println("GLOBAL " + shortName);
		tester.tagAsGlobalSummary(shortName, dimension);
	}

	private void tagAsSummary(String shortName, Dimension dimension) {
		System.out.println("LOCAL " + shortName);
		tester.tagAsSummary(shortName, dimension);
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
	 * @param runnable
	 * @since 3.1
	 */
	public static void exercise(TestRunnable runnable) throws CoreException {
		exercise(runnable, 3, 100, 4000);
	}

	/**
	 * Exercises the given runnable until either the given number of iterations
	 * or the given amount of time has elapsed, whatever occurs first.
	 * 
	 * @param runnable
	 * @param maxIterations
	 * @param maxTime
	 * @since 3.1
	 */
	public static void exercise(TestRunnable runnable, int minIterations,
			int maxIterations, int maxTime) throws CoreException {
		long startTime = System.currentTimeMillis();

		for (int counter = 0; counter < maxIterations; counter++) {

			try {
				runnable.run();
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						UIPerformancePlugin.getDefault().getBundle()
								.getSymbolicName(), IStatus.OK,
						"An exception occurred", e));
			}

			long curTime = System.currentTimeMillis();
			if (curTime - startTime > maxTime && counter >= minIterations - 1) {
				break;
			}
		}
	}

	/**
	 * Set the comment for the receiver to string. Note this is added to the
	 * output as is so you will need to add markup if you need a link.
	 * 
	 * @param string
	 *            The comment to write out for the test.
	 */
	public void setDegradationComment(String string) {
		tester.setDegradationComment(string);

	}

}
