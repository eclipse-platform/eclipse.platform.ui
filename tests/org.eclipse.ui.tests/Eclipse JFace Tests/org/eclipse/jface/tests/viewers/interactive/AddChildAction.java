package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelChange;

public class AddChildAction extends TestSelectionAction {
			
	int fEventKind;
	
	public AddChildAction(String label, TestBrowser browser) {
		this(label, browser, TestModelChange.INSERT);
	}
	public AddChildAction(String label, TestBrowser browser, int eventKind) {
		super(label, browser);
		fEventKind= eventKind;
	}
	public void run(TestElement element) {
		element.addChild(fEventKind);
	}
}
