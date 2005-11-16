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
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import junit.framework.TestCase;

public abstract class ViewerTestCase extends TestCase {

	Display fDisplay;
	protected Shell fShell;
	protected StructuredViewer fViewer;
	protected TestElement fRootElement;
	public TestModel fModel;

	public ViewerTestCase(String name) {
		super(name);
	}

	protected void assertSelectionEquals(String message, TestElement expected) {
	    ISelection selection = fViewer.getSelection();
	    assertTrue(selection instanceof StructuredSelection);
	    StructuredSelection expectedSelection = new StructuredSelection(
	            expected);
	    assertEquals("selectionEquals - " + message, selection, expectedSelection);
	}

	protected abstract StructuredViewer createViewer(Composite parent);

	public void interact() {
	    Shell shell = fShell;
	    if (shell != null && !shell.isDisposed()) {
	        Display display = shell.getDisplay();
	        if (display != null) {
	            while (shell.isVisible())
	                display.readAndDispatch();
	        }
	    }
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
	    fViewer.setInput(fRootElement);
	    fShell.open();
	    //processEvents();
	}

	public void processEvents() {
	    Shell shell = fShell;
	    if (shell != null && !shell.isDisposed()) {
	        Display display = shell.getDisplay();
	        if (display != null) {
	            while (display.readAndDispatch()) {
	            	// loop until there are no more events to dispatch
	            }
	        }
	    }
	}

	public void setUp() {
	    setUpModel();
	    openBrowser();
	}

	protected void setUpModel() {
		fRootElement = TestElement.createModel(3, 10);
	    fModel = fRootElement.getModel();
	}

	void sleep(int d) {
	    processEvents();
        try {
			Thread.sleep(d * 1000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	public void tearDown() {
	    processEvents();
	    fViewer = null;
	    if (fShell != null) {
	        fShell.dispose();
	        fShell = null;
	    }
	    // leave the display
	    fRootElement = null;
	}

}
