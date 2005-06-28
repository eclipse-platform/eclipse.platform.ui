package org.eclipse.jface.tests.performance;

import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.tests.performance.TestRunnable;

public class FastTableViewerRefreshTest extends TableViewerRefreshTest {

	public FastTableViewerRefreshTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public FastTableViewerRefreshTest(String testName) {
		super(testName);
	}

	/**
	 * Test the time for doing a refresh.
	 * 
	 * @throws Throwable
	 */
	public void testRefreshMultiple() throws Throwable {
		openBrowser();

		exercise(new TestRunnable() {
			public void run() {
				startMeasuring();
				for (int i = 0; i < 10; i++) {
					viewer.refresh();
					processEvents();

				}
				stopMeasuring();
			}
		}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 * 
	 * @throws Throwable
	 */
	public void testUpdateMultiple() throws Throwable {
		openBrowser();

		exercise(
				new TestRunnable() {
					public void run() {
						startMeasuring();
						for (int i = 0; i < 10; i++) {
							TableItem[] items = viewer.getTable().getItems();
							for (int j = 0; j < items.length; j++) {
								TableItem item = items[j];
								Object element = RefreshTestContentProvider.allElements[j];
								viewer.testUpdateItem(item, element);
							}
							processEvents();
						}

						stopMeasuring();

					}
				}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
