package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelChange;

public class AddElementAction extends TestBrowserAction {
	
	public AddElementAction(String label, TestBrowser browser) {
		super(label, browser);
//		window.addFocusChangedListener(this);
	}
	public void run() {
		TestElement element = (TestElement) getBrowser().getViewer().getInput();
		element.addChild(TestModelChange.INSERT);
	}
}
