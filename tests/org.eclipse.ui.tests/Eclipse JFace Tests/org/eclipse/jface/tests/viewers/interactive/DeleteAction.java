package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class DeleteAction extends TestSelectionAction {

	public DeleteAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run(TestElement element) {
		element.getContainer().deleteChild(element);
	}
}
