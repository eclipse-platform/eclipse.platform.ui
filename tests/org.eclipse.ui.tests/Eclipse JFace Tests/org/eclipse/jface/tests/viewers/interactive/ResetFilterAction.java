package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

public class ResetFilterAction extends TestBrowserAction {

	public ResetFilterAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		Viewer viewer = getBrowser().getViewer();
		if (viewer instanceof StructuredViewer) {
			StructuredViewer v = (StructuredViewer) viewer;
			v.resetFilters();
		}	
	}
}
