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

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.ui.tests.performance.TestRunnable;

/**
 * The ListViewerRefreshTest is a test of refreshing the list viewer.
 * 
 */
public class ListViewerRefreshTest extends ViewerTest {

	ListViewer viewer;

	private RefreshTestContentProvider contentProvider;

	public ListViewerRefreshTest(String testName, int tagging) {
		super(testName, tagging);

	}

	public ListViewerRefreshTest(String testName) {
		super(testName);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.tests.performance.ViewerTest#createViewer(org.eclipse.swt.widgets.Shell)
	 */
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
	 * 
	 * @throws Throwable
	 */
	public void testRefresh() throws Throwable {

		tagIfNecessary("JFace - Refresh 100 item ListViewer 10 times",
				Dimension.ELAPSED_PROCESS);
		setDegradationComment("<a href=https://bugs.eclipse.org/bugs/show_bug.cgi?id=99022>See Bug 99022</a> ");

		openBrowser();

		exercise(new TestRunnable() {
			public void run() {
				startMeasuring();
				viewer.refresh();
				processEvents();
				stopMeasuring();
			}
		}, MIN_ITERATIONS, ITERATIONS,
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
