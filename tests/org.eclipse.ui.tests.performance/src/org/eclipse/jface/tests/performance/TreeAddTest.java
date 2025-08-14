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

public class TreeAddTest extends TreeTest {

	static int TEST_COUNT = 1000;

	public TreeAddTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public TreeAddTest(String testName) {
		super(testName);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddOneAtATime() {
		openBrowser();

		for (int i = 0; i < ITERATIONS / 10; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			processEvents();
			startMeasuring();
			for (TestTreeElement child : input.children) {
				viewer.add(input, child);
				processEvents();
			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddTen() throws CoreException {

		doTestAdd(10, TEST_COUNT, false);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddFifty() throws CoreException {

		doTestAdd(50, TEST_COUNT, false);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddHundred() throws CoreException {

		tagIfNecessary("JFace - Add 1000 items in 10 blocks to TreeViewer",
				Dimension.ELAPSED_PROCESS);

		doTestAdd(100, TEST_COUNT, false);
	}

	/**
	 * Run the test for one of the fast insertions.
	 */
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

			// Measure more than one for the fast cases
			for (Object batch2 : batchArray) {
				viewer.add(input, (Object[]) batch2);
				processEvents();
			}

			stopMeasuring();

		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();

	}

	/**
	 * Test addition to the tree.
	 */
	public void testAddThousand() throws CoreException {
		doTestAdd(1000, 2000, false);
	}

	/**
	 * Test addition to the tree one element at a time.
	 */
	public void testAddTwoThousand() throws CoreException {

		doTestAdd(2000, 4000, false);

	}

	/**
	 * Test addition to the tree with the items presorted.
	 */
	public void testAddHundredPreSort() throws CoreException {

		doTestAdd(100, 1000, true);
	}

	/**
	 * Test addition to the tree with the items presorted.
	 */
	public void testAddThousandPreSort() throws CoreException {
		tagAsGlobalSummary("JFace - Add 2000 items in 2 blocks to TreeViewer",
				Dimension.ELAPSED_PROCESS);

		doTestAdd(1000, 2000, true);
	}

}
