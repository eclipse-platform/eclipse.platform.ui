package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.ViewPerformanceSuite;

/**
 * The ListViewerRefreshTest is a test of refreshing the list viewer.
 * 
 */
public class ListViewerRefreshTest extends LinearViewerTest {

	ListViewer viewer;
	public ListViewerRefreshTest(String testName, int tagging) {
		super(testName, tagging);

	}

	public ListViewerRefreshTest(String testName) {
		super(testName);

	}

	protected StructuredViewer createViewer(Shell shell) {
		viewer = new ListViewer(shell);
		viewer.setContentProvider(new RefreshTestContentProvider(
				RefreshTestContentProvider.ELEMENT_COUNT / 2));
		viewer.setLabelProvider(getLabelProvider());
		return viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#runTest()
	 */
	public void testRefresh() throws Throwable {
		openBrowser();

		long start = System.currentTimeMillis();
		for (int i = 0; i < ViewPerformanceSuite.ITERATIONS; i++) {
			startMeasuring();
			viewer.refresh();
			processEvents();
			stopMeasuring();
		}
		long end = System.currentTimeMillis();
		System.out.println("Elapsed time " + String.valueOf(end-start));
		commitMeasurements();
		assertPerformance();
	}

}
