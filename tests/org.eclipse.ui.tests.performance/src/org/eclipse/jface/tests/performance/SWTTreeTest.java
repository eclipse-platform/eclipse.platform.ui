/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.harness.util.CloseTestWindowsExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.RegisterExtension;

public class SWTTreeTest {

	@RegisterExtension
	CloseTestWindowsExtension closeTestWindows = new CloseTestWindowsExtension();

	Shell browserShell;

	Tree tree;

	protected void openBrowser() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = new Display();
		}
		browserShell = new Shell(display);
		browserShell.setSize(500, 500);
		browserShell.setLayout(new FillLayout());
		tree = new Tree(browserShell, SWT.NONE);
		createChildren();
		browserShell.open();
	}

	@AfterEach
	public void closeBrowserShell() {
		if (browserShell != null) {
			browserShell.close();
			browserShell = null;
		}
	}

	private void createChildren() {
		for (int i = 0; i < TreeAddTest.TEST_COUNT; i++) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText("Element " + i);

		}

	}

	/**
	 * Test the getItems API.
	 */
	@Test
	public void testGetItems(TestInfo testInfo) throws CoreException {
		openBrowser();

		List<Long> timings = new ArrayList<>();
		exercise(() -> {
			processEvents();
			long before = System.nanoTime();
			int seen = 0;
			for (int j = 0; j < TreeAddTest.TEST_COUNT; j++) {
				seen += tree.getItems().length;
				processEvents();
			}
			timings.add(System.nanoTime() - before);
			assertEquals(TreeAddTest.TEST_COUNT * TreeAddTest.TEST_COUNT, seen);
		});

		reportTimings(scenario(testInfo), timings);
	}

	/**
	 * Test the getItem API.
	 */
	@Test
	public void testGetItemAt(TestInfo testInfo) throws CoreException {
		openBrowser();

		List<Long> timings = new ArrayList<>();
		exercise(() -> {
			processEvents();
			long before = System.nanoTime();
			int seen = 0;
			for (int j = 0; j < TreeAddTest.TEST_COUNT; j++) {
				seen += tree.getItem(j) == null ? 0 : 1;
				processEvents();
			}
			timings.add(System.nanoTime() - before);
			assertEquals(TreeAddTest.TEST_COUNT, seen);
		});

		reportTimings(scenario(testInfo), timings);
	}

	private String scenario(TestInfo testInfo) {
		return getClass().getSimpleName() + "." + testInfo.getDisplayName();
	}

}
