package org.eclipse.jface.tests.viewers.interactive;

import org.eclipse.jface.viewers.StructuredViewer;
/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

public class WorldChangedAction extends TestBrowserAction {

	public WorldChangedAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		((StructuredViewer)getBrowser().getViewer()).refresh();
	}
}
