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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.junit.Test;

/**
 * The ListViewerRefreshTest is a test of refreshing the list viewer.
 */
public class ListViewerRefreshTest extends ViewerTest {

	ListViewer viewer;

	private RefreshTestContentProvider contentProvider;

	@Override
	protected StructuredViewer createViewer(Shell shell) {
		viewer = new ListViewer(shell);
		contentProvider = new RefreshTestContentProvider(
				RefreshTestContentProvider.ELEMENT_COUNT);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(getLabelProvider());
		return viewer;
	}

	/**
	 * Test the time for doing a refresh.
	 */
	@Test
	public void testRefresh() throws Throwable {
		openBrowser();

		exercise(() -> {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, ITERATIONS,
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
