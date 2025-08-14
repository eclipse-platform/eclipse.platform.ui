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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

public class SWTTreeTest extends BasicPerformanceTest {

	Shell browserShell;

	Tree tree;

	public SWTTreeTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public SWTTreeTest(String testName) {
		super(testName);
	}

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
		// processEvents();
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
	public void testGetItems() throws CoreException {
		openBrowser();

		exercise(() -> {
			processEvents();
			startMeasuring();
			for (int j = 0; j < TreeAddTest.TEST_COUNT; j++) {
				tree.getItems();
				processEvents();
			}
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
		browserShell.close();
	}

	/**
	 * @throws CoreException
	 * Test the getItem API.
	 */
	public void testGetItemAt() throws CoreException {
		openBrowser();

		exercise(() -> {
			processEvents();
			startMeasuring();
			for (int j = 0; j < TreeAddTest.TEST_COUNT; j++) {
				tree.getItem(j);
				processEvents();
			}
			stopMeasuring();
		});

		commitMeasurements();
		assertPerformance();
		browserShell.close();
	}

}
