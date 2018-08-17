/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test all areas of the UI API.
 */
public class UIPerformanceTestSuite extends FilteredTestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIPerformanceTestSetup(new UIPerformanceTestSuite());
	}

	/**
	 * Construct the test suite.
	 */
	public UIPerformanceTestSuite() {
		super();
		addTest(new ActivitiesPerformanceSuite());
		addTest(new WorkbenchPerformanceSuite());
		addTest(new ViewPerformanceSuite());
		addTest(new EditorPerformanceSuite());
		addTest(new TestSuite(CommandsPerformanceTest.class));
		addTest(new LabelProviderTestSuite());
		addTest(new TestSuite(ProgressReportingTest.class));
	}
}
