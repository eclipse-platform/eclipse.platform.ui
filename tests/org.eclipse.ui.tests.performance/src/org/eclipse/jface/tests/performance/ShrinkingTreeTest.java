/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Dimension;

/**
 * ShrinkingTreeTest is a test to see how long it takes to refresh a tree that goes
 * from a large item count to a smaller one.
 * @since 3.3
 */
public class ShrinkingTreeTest extends TreeTest {


	/**
	 * Create a new instance of the receiver.
	 */
	public ShrinkingTreeTest(String testName) {
		super(testName);
	}

	public ShrinkingTreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public void testTreeViewerRefresh() throws CoreException {

		tagIfNecessary("JFace - Refresh from 1000 items to 100 items",
				Dimension.ELAPSED_PROCESS);

		openBrowser();
//		int smallCount = 1;
//		for (int i = 0; i < 3; i++) {
//
//			int largeCount = smallCount * 10;
//			for (int j = 0; j < 2; j++) {
//				System.out.println("Small " + String.valueOf(smallCount)
//						+ "Large " + String.valueOf(largeCount));
				testRefresh(100, 1000);
//				largeCount *= 10;
//			}
//			smallCount *= 10;
//		}
	}

	/**
	 * Run the test for one of the fast insertions.
	 */
	private void testRefresh(final int smallSize, final int largeSize)
			throws CoreException {

		exercise(() -> {

			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(largeSize);

			processEvents();
			viewer.refresh();
			viewer.expandAll();
			input.createChildren(smallSize);
			startMeasuring();
			viewer.refresh();

			stopMeasuring();

		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();

	}



}
