/*******************************************************************************
 * Copyright (c) 2019 Paul Pazderski and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Paul Pazderski - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.harness.util;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * Util to help on consistent logging of test runs.
 */
public final class TestRunLogUtil {

	private static final String LINE_BREAK = System.getProperty("line.separator"); //$NON-NLS-1$

	/**
	 * A {@link TestWatcher} to log test start and end. Should be added as @Rule in
	 * every JUnit4 test class (which not already extends {@link UITestCase}) as
	 *
	 * <pre>
	 * &#64;Rule
	 * public TestWatcher LOG_TESTRUN = TestRunLogUtil.LOG_TESTRUN;
	 * </pre>
	 *
	 * Note: field must be public or JUnit4 will complain.
	 */
	public static TestWatcher LOG_TESTRUN = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			System.out.println(formatTestStartMessage(description.getMethodName()));
		}

		@Override
		protected void finished(Description description) {
			System.out.println(formatTestFinishedMessage(description.getMethodName()));
		}
	};

	/**
	 * Create message used to log start of a test.
	 *
	 * @param testName name of started test
	 * @return message used to log test start
	 */
	public static String formatTestStartMessage(String testName) {
		return "----- " + testName + LINE_BREAK + testName + ": setUp..."; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Create message used to log end of a test.
	 *
	 * @param testName name of finished test
	 * @return message used to log test end
	 */
	public static String formatTestFinishedMessage(String testName) {
		return testName + ": tearDown...\n"; //$NON-NLS-1$
	}

	private TestRunLogUtil() {
	}
}
