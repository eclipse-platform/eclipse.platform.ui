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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The JFacePerformanceSuite are the performance tests for JFace.
 */
@RunWith(Suite.class)
@SuiteClasses({ //
		ListViewerRefreshTest.class, //
		ComboViewerRefreshTest.class, //
		FastTableViewerRefreshTest.class, //
		FastTreeTest.class, //
		TreeAddTest.class, //
		ProgressMonitorDialogPerformanceTest.class, //
		ShrinkingTreeTest.class, //
		CollatorPerformanceTest.class, //
})
public class JFacePerformanceSuite {
	//Specify the minimum number of iterations
	//and the time to drop down to a lower number

	public static int MAX_TIME = 10000;
}
