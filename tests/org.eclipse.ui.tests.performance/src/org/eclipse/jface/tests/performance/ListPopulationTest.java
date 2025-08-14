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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.junit.Test;

/**
 * The ListPopulationTest is the test for simple
 * SWT lists.
 */
public class ListPopulationTest extends BasicPerformanceTest {

	List list;

	protected void openBrowser() {
		Display fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		Shell shell = new Shell(fDisplay);
		shell.setSize(500, 500);
		shell.setLayout(new FillLayout());
		list = new List(shell,SWT.NONE);
		shell.open();
		// processEvents();
	}

	@Test
	public void testSmallAdd() throws Throwable {
		addBench(100);
	}

	@Test
	public void testSmallSetItems() throws Throwable {
		setItemsBench(100);
	}

	@Test
	public void testMediumAdd() throws Throwable {
		addBench(5000);
	}

	@Test
	public void testMediumSetItems() throws Throwable {
		setItemsBench(5000);
	}

	@Test
	public void testLargeAdd() throws Throwable {
		addBench(50000);
	}

	@Test
	public void testLargeSetItems() throws Throwable {
		setItemsBench(50000);
	}

	/**
	 * Test the time for adding elements using add.
	 */
	public void addBench(int count) throws Throwable {
		openBrowser();
		final String [] items = getItems(count);

		exercise(() -> {
			list.removeAll();
			startMeasuring();
			for (String item : items) {
				list.add(item);
			}
			processEvents();
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Test the time for adding elements using setItem.
	 */
	public void setItemsBench(int count) throws Throwable {
		openBrowser();
		final String [] items = getItems(count);
		exercise(() -> {
			list.removeAll();
			startMeasuring();
			list.setItems(items);
			processEvents();
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Get count number of items.
	 */
	private String[] getItems(int count) {
		String[] items = new String[count];
		for (int j = 0; j < items.length; j++) {
			items[j] = "Element " + j;

		}
		return items;
	}


}
