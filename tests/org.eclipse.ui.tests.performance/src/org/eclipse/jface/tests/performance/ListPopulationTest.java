/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;
import org.eclipse.ui.tests.performance.TestRunnable;

/**
 * The ListPopulationTest is the test for simple
 * SWT lists.
 *
 */
public class ListPopulationTest extends BasicPerformanceTest {

	List list;

	public ListPopulationTest(String testName, int tagging) {
		super(testName, tagging);		
	}

	public ListPopulationTest(String testName) {
		super(testName);
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
	
	public void testSmallAdd() throws Throwable {
		addBench(100);
	}
	
	public void testSmallSetItems() throws Throwable {
		setItemsBench(100);
	}
	
	public void testMediumAdd() throws Throwable {
		addBench(5000);
	}
	
	public void testMediumSetItems() throws Throwable {
		setItemsBench(5000);
	}
	
	public void testLargeAdd() throws Throwable {
		addBench(50000);
	}
	
	public void testLargeSetItems() throws Throwable {
		setItemsBench(50000);
	}
	
	/**
	 * Test the time for adding elements using add.
	 * @throws Throwable
	 */
	public void addBench(int count) throws Throwable {
		openBrowser();		
		final String [] items = getItems(count);
        
        exercise(new TestRunnable() {
            public void run() {
    			list.removeAll();
    			startMeasuring();
    			for (int j = 0; j < items.length; j++) {
    				list.add(items[j]);
    			}
    			processEvents();
    			stopMeasuring();
    		}
        });
		
		commitMeasurements();
		assertPerformance();
	}
	
	/**
	 * Test the time for adding elements using setItem.
	 * @throws Throwable
	 */
	public void setItemsBench(int count) throws Throwable {
		openBrowser();		
		final String [] items = getItems(count);
        exercise(new TestRunnable() {
            public void run() {
    			list.removeAll();
    			startMeasuring();
    			list.setItems(items);
    			processEvents();
    			stopMeasuring();
            }
        });
		
		commitMeasurements();
		assertPerformance();
	}

	/**
	 * Get count number of items.
	 * @param count
	 * @return
	 */
	private String[] getItems(int count) {
		String[] items = new String[count];
		for (int j = 0; j < items.length; j++) {
			items[j] = "Element " + String.valueOf(j);
			
		}
		return items;
	}
	

}
