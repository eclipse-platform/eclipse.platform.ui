package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.tests.viewers.TestLabelProvider;
import org.eclipse.jface.viewers.ContentViewer;
 
public class SetLabelProviderAction extends TestBrowserAction {

	public SetLabelProviderAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		((ContentViewer) getBrowser().getViewer()).setLabelProvider(new TestLabelProvider());
	}
}
