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

/**
 * The JFacePerformanceSuite are the performance
 * tests for JFace.
 *
 */
public class JFacePerformanceSuite extends TestSuite {

	public JFacePerformanceSuite() {
		super();
		addTest(new ListViewerRefreshTest("testRefresh"));
		addTest(new ListViewerRefreshTest("testRefreshSmaller"));
		addTest(new ListViewerRefreshTest("testRefreshLarger"));
		addTest(new ComboViewerRefreshTest("testRefresh"));
		addTest(new ComboViewerRefreshTest("testRefreshSmaller"));
		addTest(new ComboViewerRefreshTest("testRefreshLarger"));
		addTest(new TreeTest("testAdd"));
	}
}
