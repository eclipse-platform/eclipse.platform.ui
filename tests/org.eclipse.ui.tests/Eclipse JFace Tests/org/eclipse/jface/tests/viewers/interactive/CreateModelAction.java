package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class CreateModelAction extends TestBrowserAction {
	int fLevel;
	int fChildCount;
	public CreateModelAction(String label, TestBrowser browser, int level, int childCount) {
		super(label, browser);
		fLevel= level;
		fChildCount= childCount;
	}
	public void run() {
		// Clear input since TestElement.equals does only
		// check the id, not the size of the TestElement.
		getBrowser().setInput(null);
		getBrowser().setInput(TestElement.createModel(fLevel, fChildCount));
	}
}
