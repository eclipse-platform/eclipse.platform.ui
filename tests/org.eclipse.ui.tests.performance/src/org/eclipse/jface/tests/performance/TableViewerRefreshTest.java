/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
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

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;

/**
 * The TableViewerRefreshTest is a test for refreshing the TableViewer.
 */
public class TableViewerRefreshTest extends ViewerTest {

	class TestTableViewer extends TableViewer {

		public TestTableViewer(Composite parent, int style) {
			super(parent, style);
		}

		public TestTableViewer(Composite parent) {
			super(parent);
		}

		public TestTableViewer(Table table) {
			super(table);
		}

		public void testUpdateItem(Widget widget, Object element) {
			updateItem(widget, element);
		}
	}

	TestTableViewer viewer;

	private RefreshTestContentProvider contentProvider;

	public TableViewerRefreshTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public TableViewerRefreshTest(String testName) {
		super(testName);
	}

	@Override
	protected StructuredViewer createViewer(Shell shell) {
		viewer = new TestTableViewer(shell);
		contentProvider = new RefreshTestContentProvider(
				RefreshTestContentProvider.ELEMENT_COUNT);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(getLabelProvider());
		return viewer;
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testRefresh() throws Throwable {
		openBrowser();

		exercise(() -> {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testRefreshSorted() throws Throwable {
		openBrowser();
		viewer.setComparator(new ViewerComparator());

		exercise(() -> {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testRefreshPreSorted() throws Throwable {
		openBrowser();
		final ViewerComparator sorter = new ViewerComparator();
		viewer.setComparator(sorter);

		exercise(() -> {
			contentProvider.refreshElements();
			startMeasuring();
			contentProvider.cloneElements();
			contentProvider.preSortElements(viewer, sorter);
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}, MIN_ITERATIONS, ITERATIONS,
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 */
	public void testUpdate() throws Throwable {
		openBrowser();

		exercise(() -> {

			TableItem[] items = viewer.getTable().getItems();
			startMeasuring();
			for (int j = 0; j < items.length; j++) {
				TableItem item = items[j];
				Object element = RefreshTestContentProvider.allElements[j];

				viewer.testUpdateItem(item, element);

			}
			processEvents();
			stopMeasuring();

		}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
