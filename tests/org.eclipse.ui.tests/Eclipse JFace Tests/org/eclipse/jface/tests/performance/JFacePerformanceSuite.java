/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.performance;

import junit.framework.TestSuite;

import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * The JFacePerformanceSuite are the performance tests for JFace.
 */
public class JFacePerformanceSuite extends TestSuite {

	public JFacePerformanceSuite() {
		super();
		addTest(new ListViewerRefreshTest("testRefresh",BasicPerformanceTest.GLOBAL));
		addTest(new ComboViewerRefreshTest("testRefresh",BasicPerformanceTest.LOCAL));
		addTest(new TableViewerRefreshTest("testRefresh"));
		addTest(new TableViewerRefreshTest("testUpdate"));
		addTest(new TreeTest("testAddTen"));
		addTest(new TreeTest("testAddFifty"));
		addTest(new TreeTest("testAddThousand"));
		addTest(new TreeTest("testAddHundred", BasicPerformanceTest.GLOBAL));
		addTest(new TreeTest("testAddThousandPreSort", BasicPerformanceTest.LOCAL));

	}
}
