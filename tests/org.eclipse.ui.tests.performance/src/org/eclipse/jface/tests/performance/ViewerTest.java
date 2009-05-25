/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * The LinearViewerTest is a test that tests viewers.
 * 
 */
public abstract class ViewerTest extends BasicPerformanceTest {

	Shell browserShell;
	
	public static int ITERATIONS = 100;
	public static int MIN_ITERATIONS = 20;

	public ViewerTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public ViewerTest(String testName) {
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
		StructuredViewer viewer = createViewer(browserShell);
		viewer.setUseHashlookup(true);
		viewer.setInput(getInitialInput());
		browserShell.open();
		// processEvents();
	}

	/**
	 * Get the initial input for the receiver.
	 * @return
	 */
	protected Object getInitialInput() {
		return this;
	}

	/**
	 * Create the viewer we are testing.
	 * @param aShell
	 * @return
	 */
	protected abstract StructuredViewer createViewer(Shell shell);

	public ILabelProvider getLabelProvider() {
		return new LabelProvider() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
			 */
			public String getText(Object element) {
				return ((TestElement) element).getText();
			}

		};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.tests.util.UITestCase#doTearDown()
	 */
	protected void doTearDown() throws Exception {
		super.doTearDown();
		if(browserShell!= null){
			browserShell.close();
			browserShell = null;
		}
	}
	
	/**
	 * Return the number of iterations for tests that are slow on Linux
	 * @return int
	 */
	public int slowGTKIterations(){
		if(Util.isGtk())
			return ITERATIONS / 5;
		return ITERATIONS;
	}
	
	
	/**
	 * Return the number of iterations for tests that are slow on Linux
	 * @return int
	 */
	public int slowWindowsIterations(){
		if(Util.isWindows())
			return ITERATIONS / 5;
		return ITERATIONS;
	}

}
