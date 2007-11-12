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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.TestRunnable;

public class BigTreeTest extends TreeTest {

	private boolean slow = false;

	/**
	 * Create a new instance of the receiver.
	 * 
	 * @param testName
	 */
	public BigTreeTest(String testName) {
		super(testName);
	}

	public BigTreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public void testRefreshPivots() throws CoreException {
		slow = false;
		runTests();
	}
	
	public void testRefreshPivotsSlow() throws CoreException {
		slow = true;
		runTests();
	}

	/**
	 * @throws CoreException
	 */
	private void runTests() throws CoreException {
		openBrowser();
		int smallCount = 2;
		for (int i = 0; i < 4; i++) {

			int largeCount = smallCount;
			for (int j = 0; j < 2; j++) {
				System.out.println("Small " + String.valueOf(smallCount)
						+ "Large " + String.valueOf(largeCount));
				testRefresh(smallCount, largeCount);
				largeCount *= 10;
			}
			smallCount *= 10;
		}
	}

	/**
	 * Run the test for one of the fast insertions.
	 * 
	 * @param count
	 * @throws CoreException
	 */
	private void testRefresh(final int smallSize, final int largeSize)
			throws CoreException {

		exercise(new TestRunnable() {
			public void run() {

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

			}
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.performance.TreeTest#createTreeViewer(org.eclipse.swt.widgets.Shell)
	 */
	protected TreeViewer createTreeViewer(Shell shell) {
		if (slow )
			return super.createTreeViewer(shell);

		return  new FastTreeViewer(shell);
	}

}
