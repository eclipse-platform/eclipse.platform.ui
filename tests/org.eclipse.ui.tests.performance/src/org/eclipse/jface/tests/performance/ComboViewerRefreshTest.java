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

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.TestRunnable;

/**
 * ComboViewerRefreshTest is a test of refreshes of difference size in the combo
 * viewer.
 */
public class ComboViewerRefreshTest extends ViewerTest {

	ComboViewer viewer;

	private RefreshTestContentProvider contentProvider;

	private static int ELEMENT_COUNT;

	public ComboViewerRefreshTest(String testName, int tagging) {
		super(testName, tagging);

	}

	public ComboViewerRefreshTest(String testName) {
		super(testName);

	}

	protected StructuredViewer createViewer(Shell shell) {

		viewer = new ComboViewer(shell);
		contentProvider = new RefreshTestContentProvider(ELEMENT_COUNT);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(getLabelProvider());
		return viewer;
	}

	/**
	 * Test the time for doing a refresh.
	 * 
	 * @throws Throwable
	 */
	public void testRefresh() throws Throwable {

		setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=98265>See Bug 98265</a> ");
		ELEMENT_COUNT = 1000;
		openBrowser();

		exercise(new TestRunnable() {
			public void run() {
				startMeasuring();
				viewer.refresh();
				processEvents();
				stopMeasuring();
			}
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 * 
	 * @throws Throwable
	 */
	public void testRefreshSmall() throws Throwable {

		setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=98265>See Bug 98265</a> ");

		ELEMENT_COUNT = 50;
		openBrowser();

		exercise(new TestRunnable() {
			public void run() {
				startMeasuring();
				for (int i = 0; i < 1000; i++) {
					viewer.refresh();
				}
				processEvents();
				stopMeasuring();
			}
		}, MIN_ITERATIONS, slowGTKIterations(), JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
