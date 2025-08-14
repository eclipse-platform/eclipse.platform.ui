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

import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

public class FastTableViewerRefreshTest extends TableViewerRefreshTest {

	/**
	 * Test the time for doing a refresh.
	 */
	@Test
	public void testRefreshMultiple() throws Throwable {
		openBrowser();

		exercise(() -> {
			startMeasuring();
			for (int i = 0; i < 10; i++) {
				viewer.refresh();
				processEvents();

			}
			stopMeasuring();
		}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for doing a refresh.
	 */
	@Test
	public void testUpdateMultiple() throws Throwable {
		openBrowser();

		exercise(
				() -> {
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

				}, MIN_ITERATIONS, slowGTKIterations(),
				JFacePerformanceSuite.MAX_TIME);

		commitMeasurements();
		assertPerformance();
	}

}
