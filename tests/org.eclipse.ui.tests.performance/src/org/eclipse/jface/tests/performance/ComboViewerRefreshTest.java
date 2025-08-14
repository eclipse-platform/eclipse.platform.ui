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

import static org.eclipse.ui.tests.harness.util.UITestUtil.processEvents;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;

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

	@Override
	protected StructuredViewer createViewer(Shell shell) {

		viewer = new ComboViewer(shell);
		contentProvider = new RefreshTestContentProvider(ELEMENT_COUNT);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(getLabelProvider());
		return viewer;
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testRefresh() throws Throwable {
		ELEMENT_COUNT = 1000;
		openBrowser();

		exercise(() -> {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, ITERATIONS, JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testRefreshSmall() throws Throwable {
		ELEMENT_COUNT = 50;
		openBrowser();

		exercise(() -> {
			startMeasuring();
			for (int i = 0; i < 1000; i++) {
				viewer.refresh();
			}
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, slowGTKIterations(), JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
