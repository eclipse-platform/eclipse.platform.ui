package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.viewers.StructuredSelection;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

public class ClearSelectionAction extends TestBrowserAction {

	public ClearSelectionAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		getBrowser().getViewer().setSelection(new StructuredSelection());
	}
}
