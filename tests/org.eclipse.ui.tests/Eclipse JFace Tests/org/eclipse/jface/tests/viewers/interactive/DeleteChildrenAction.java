package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;

public class DeleteChildrenAction extends TestSelectionAction {

	boolean fAll= false;
	
	public DeleteChildrenAction(String label, TestBrowser browser, boolean all) {
		super(label, browser);
		fAll= all;
	}
	public void run(TestElement element) {
		if (fAll)
			element.deleteChildren();
		else
			element.deleteSomeChildren();
	}
}
