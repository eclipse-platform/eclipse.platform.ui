/*******************************************************************************
 * Copyright (c) 2005, 2017 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Dimension;

public class FastTreeTest extends TreeAddTest {


	public FastTreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public FastTreeTest(String testName) {
		super(testName);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddTenTenTimes() throws CoreException {

		doTestAdd(10, TEST_COUNT, false);
	}


	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddFiftyTenTimes() throws CoreException {

		doTestAdd(50, TEST_COUNT, false);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddHundredTenTimes() throws CoreException {
		tagAsSummary("JFace - Add 10000 items 100 at a time TreeViewer 10 times",
				Dimension.ELAPSED_PROCESS);

		doTestAdd(100, TEST_COUNT, false);
	}

	/**
	 * Run the test for one of the fast insertions.
	 */
	@Override
	protected void doTestAdd(final int increment, final int total,final boolean preSort)
			throws CoreException {

		openBrowser();

		exercise(() -> {

			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(total);
			if (preSort)
				viewer.getComparator().sort(viewer, input.children);
			Collection<Object> batches = new ArrayList<>();
			int blocks = input.children.length / increment;
			for (int j = 0; j < blocks; j = j + increment) {
				Object[] batch1 = new Object[increment];
				System.arraycopy(input.children, j * increment, batch1, 0, increment);
				batches.add(batch1);
			}
			processEvents();
			Object[] batchArray = batches.toArray();
			startMeasuring();
			for (int i = 0; i < 10; i++) {
				viewer.remove((Object[]) input.children);
				for (Object batch2 : batchArray) {
					viewer.add(input, (Object[]) batch2);
					processEvents();
				}
			}


			stopMeasuring();

		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();

	}
}
