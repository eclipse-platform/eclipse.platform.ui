package org.eclipse.jface.tests.performance;

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
		for (int i = 0; i < TreeTest.TEST_COUNT; i++) {
			TreeItem item = new TreeItem(tree, SWT.NONE);
			item.setText("Element " + String.valueOf(i));

		}

	}

	/**
	 * Test the getItems API.
	 * 
	 */
	public void testGetItems() {
		openBrowser();

		for (int i = 0; i < 25; i++) {
			processEvents();
			startMeasuring();
			for (int j = 0; j < TreeTest.TEST_COUNT; j++) {
				tree.getItems();
				processEvents();
			}
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();
		browserShell.close();
	}

	/**
	 * Test the getItem API.
	 * 
	 */
	public void testGetItemAt() {
		openBrowser();

		for (int i = 0; i < 25; i++) {
			processEvents();
			startMeasuring();
			for (int j = 0; j < TreeTest.TEST_COUNT; j++) {
				tree.getItem(j);
				processEvents();
			}
			stopMeasuring();
		}
		commitMeasurements();
		assertPerformance();
		browserShell.close();
	}

}
