package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

public class FlushInputAction extends TestBrowserAction {

	public FlushInputAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		getBrowser().setInput(null);
	}
}
