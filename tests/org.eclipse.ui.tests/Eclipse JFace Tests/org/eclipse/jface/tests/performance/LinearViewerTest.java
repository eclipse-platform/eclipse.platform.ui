/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jface.tests.performance;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.tests.performance.BasicPerformanceTest;

/**
 * The LinearViewerTest is a test that tests viewers with non hierarchal data
 * such as list, combo or table viewers.
 * 
 */
public abstract class LinearViewerTest extends BasicPerformanceTest {

	Display fDisplay;

	Shell fShell;

	StructuredViewer fViewer;

	public LinearViewerTest(String testName, int tagging) {
		super(testName, tagging);
	}

	public LinearViewerTest(String testName) {
		super(testName);
	}

	protected void openBrowser() {
		fDisplay = Display.getCurrent();
		if (fDisplay == null) {
			fDisplay = new Display();
		}
		fShell = new Shell(fDisplay);
		fShell.setSize(500, 500);
		fShell.setLayout(new FillLayout());
		fViewer = createViewer(fShell);
		fViewer.setUseHashlookup(true);
		fViewer.setInput(this);
		fShell.open();
		// processEvents();
	}

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

}
