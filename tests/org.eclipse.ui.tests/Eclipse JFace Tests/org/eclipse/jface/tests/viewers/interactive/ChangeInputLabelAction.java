package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class ChangeInputLabelAction extends TestBrowserAction {

	public ChangeInputLabelAction(String label, TestBrowser browser) {
		super(label, browser);
	}
public void run() {
	TestElement element = (TestElement) getBrowser().getInput();
	element.setLabel(element.getLabel() + " changed");
}
}
