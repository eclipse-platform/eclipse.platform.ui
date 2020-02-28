/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
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
package org.eclipse.jface.tests.performance;

import org.eclipse.ui.tests.performance.FilteredTestSuite;
import org.eclipse.ui.tests.performance.UIPerformanceTestSetup;

import junit.framework.Test;

/**
 * The JFacePerformanceSuite are the performance tests for JFace.
 */
public class JFacePerformanceSuite extends FilteredTestSuite {

	//Specify the minimum number of iterations
	//and the time to drop down to a lower number

	public static int MAX_TIME = 10000;

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static Test suite() {
		return new UIPerformanceTestSetup(new JFacePerformanceSuite());
	}

	public JFacePerformanceSuite() {
		super();
		addTestSuite(ListViewerRefreshTest.class);
		addTestSuite(ComboViewerRefreshTest.class);
		addTestSuite(FastTableViewerRefreshTest.class);
		addTestSuite(FastTreeTest.class);
		addTestSuite(TreeAddTest.class);
		addTestSuite(ProgressMonitorDialogPerformanceTest.class);
		addTestSuite(ShrinkingTreeTest.class);
		addTestSuite(CollatorPerformanceTest.class);

	}
}
