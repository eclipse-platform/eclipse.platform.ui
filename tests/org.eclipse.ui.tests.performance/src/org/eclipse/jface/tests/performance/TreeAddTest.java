/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.performance;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.tests.performance.TestRunnable;

public class TreeAddTest extends TreeTest {

	static int TEST_COUNT = 1000;

	public TreeAddTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public TreeAddTest(String testName) {
		super(testName);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddOneAtATime() {
		openBrowser();

		for (int i = 0; i < ITERATIONS / 10; i++) {
			TestTreeElement input = new TestTreeElement(0, null);
			viewer.setInput(input);
			input.createChildren(TEST_COUNT);
			processEvents();
			startMeasuring();
			for (int j = 0; j < input.children.length; j++) {

				viewer.add(input, input.children[j]);
				processEvents();

			}
			stopMeasuring();
		}

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddTen() throws CoreException {

		doTestAdd(10, TEST_COUNT, false);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddFifty() throws CoreException {

		doTestAdd(50, TEST_COUNT, false);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddHundred() throws CoreException {

		tagIfNecessary("JFace - Add 1000 items in 10 blocks to TreeViewer",
				Dimension.ELAPSED_PROCESS);
		setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=101853>See Bug 101853</a> ");

		doTestAdd(100, TEST_COUNT, false);
	}

	/**
	 * Run the test for one of the fast insertions.
	 * 
	 * @param count
	 * @throws CoreException
	 */
	protected void doTestAdd(final int increment, final int total,final boolean preSort)
			throws CoreException {

		openBrowser();

		exercise(new TestRunnable() {
			public void run() {

				TestTreeElement input = new TestTreeElement(0, null);
				viewer.setInput(input);
				input.createChildren(total);
				if (preSort)
					viewer.getSorter().sort(viewer, input.children);
				Collection batches = new ArrayList();
				int blocks = input.children.length / increment;
				for (int j = 0; j < blocks; j = j + increment) {
					Object[] batch = new Object[increment];
					System.arraycopy(input.children, j * increment, batch, 0,
							increment);
					batches.add(batch);
				}
				processEvents();
				Object[] batchArray = batches.toArray();
				startMeasuring();

				// Measure more than one for the fast cases
				for (int k = 0; k < batchArray.length; k++) {
					viewer.add(input, (Object[]) batchArray[k]);
					processEvents();
				}

				stopMeasuring();

			}
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
	 * @throws CoreException
	 *             Test addition to the tree one element at a time.
	 */
	public void testAddTwoThousand() throws CoreException {

		doTestAdd(2000, 4000, false);

	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree with the items presorted.
	 */
	public void testAddHundredPreSort() throws CoreException {

		doTestAdd(100, 1000, true);
	}

	/**
	 * @throws CoreException
	 *             Test addition to the tree with the items presorted.
	 */
	public void testAddThousandPreSort() throws CoreException {
		tagIfNecessary("JFace - Add 2000 items in 2 blocks to TreeViewer",
				Dimension.ELAPSED_PROCESS);

		doTestAdd(1000, 2000, true);
	}

}
