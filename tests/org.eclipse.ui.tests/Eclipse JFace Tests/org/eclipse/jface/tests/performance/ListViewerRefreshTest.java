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

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.ViewPerformanceSuite;

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

	/* (non-Javadoc)
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
	 * @throws Throwable
	 */
	public void testRefresh() throws Throwable {
		openBrowser();

		for (int i = 0; i < ViewPerformanceSuite.ITERATIONS; i++) {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}
		
		commitMeasurements();
		assertPerformance();
	}
	

}
