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
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.exercise;
import static org.eclipse.ui.tests.performance.UIPerformanceTestUtil.reportTimings;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * The ListPopulationTest is the test for simple
 * SWT lists.
 */
public class ListPopulationTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	List list;

	@AfterEach
	public void tearDown() {
		if (list != null && !list.isDisposed()) {
			list.getShell().dispose();
		}
	}

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
	public void testSmallAdd(TestInfo testInfo) throws Throwable {
		addBench(100, testInfo);
	}

	@Test
	public void testSmallSetItems(TestInfo testInfo) throws Throwable {
		setItemsBench(100, testInfo);
	}

	@Test
	public void testMediumAdd(TestInfo testInfo) throws Throwable {
		addBench(5000, testInfo);
	}

	@Test
	public void testMediumSetItems(TestInfo testInfo) throws Throwable {
		setItemsBench(5000, testInfo);
	}

	@Test
	public void testLargeAdd(TestInfo testInfo) throws Throwable {
		addBench(50000, testInfo);
	}

	@Test
	public void testLargeSetItems(TestInfo testInfo) throws Throwable {
		setItemsBench(50000, testInfo);
	}

	/**
	 * Test the time for adding elements using add.
	 */
	public void addBench(int count, TestInfo testInfo) throws Throwable {
		openBrowser();
		final String [] items = getItems(count);

		java.util.List<Long> timings = new ArrayList<>();
		exercise(() -> {
			list.removeAll();
			long before = System.nanoTime();
			for (String item : items) {
				list.add(item);
			}
			processEvents();
			timings.add(System.nanoTime() - before);
			assertEquals(count, list.getItemCount());
		});

		reportTimings(scenario(testInfo), timings);
	}

	/**
	 * Test the time for adding elements using setItem.
	 */
	public void setItemsBench(int count, TestInfo testInfo) throws Throwable {
		openBrowser();
		final String [] items = getItems(count);

		java.util.List<Long> timings = new ArrayList<>();
		exercise(() -> {
			list.removeAll();
			long before = System.nanoTime();
			list.setItems(items);
			processEvents();
			timings.add(System.nanoTime() - before);
			assertEquals(count, list.getItemCount());
		});

		reportTimings(scenario(testInfo), timings);
	}

	private String scenario(TestInfo testInfo) {
		return getClass().getSimpleName() + "." + testInfo.getDisplayName();
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