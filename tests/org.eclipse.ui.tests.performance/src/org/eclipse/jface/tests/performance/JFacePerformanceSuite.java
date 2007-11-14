/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.performance;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.eclipse.ui.tests.performance.UIPerformanceTestSetup;

/**
 * The JFacePerformanceSuite are the performance tests for JFace.
 */
public class JFacePerformanceSuite extends TestSuite {
	
	//Some of these tests are very slow on 3.0
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
		addTest(new ListViewerRefreshTest("testRefresh"));
		addTest(new ComboViewerRefreshTest("testRefreshSmall"));
		addTest(new FastTableViewerRefreshTest("testRefreshMultiple"));
		addTest(new FastTableViewerRefreshTest("testUpdateMultiple"));
		addTest(new FastTreeTest("testAddTenTenTimes"));
		addTest(new FastTreeTest("testAddFiftyTenTimes"));
		addTest(new TreeAddTest("testAddThousand"));
		addTest(new FastTreeTest("testAddHundredTenTimes", BasicPerformanceTest.LOCAL));
		addTest(new TreeAddTest("testAddThousandPreSort", BasicPerformanceTest.GLOBAL));
		addTest(new ProgressMonitorDialogPerformanceTest("testLongNames"));
		addTest(new ShrinkingTreeTest("testTreeViewerRefresh"));

	}
}
