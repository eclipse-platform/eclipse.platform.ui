package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class DeleteSiblingsAction extends TestSelectionAction {

	boolean fAll= false;
	
	public DeleteSiblingsAction(String label, TestBrowser browser, boolean all) {
		super(label, browser);
		fAll= all;
	}
	public void run(TestElement element) {
		if (fAll)
			element.getContainer().deleteChildren();
		else
			element.getContainer().deleteSomeChildren();
	}
}
