package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class ChangeChildLabelAction extends TestSelectionAction {
	public ChangeChildLabelAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run(TestElement element) {
		TestElement child= element.getFirstChild();
		if (child != null)
			child.setLabel(child.getLabel() + " renamed child");
	}
}
