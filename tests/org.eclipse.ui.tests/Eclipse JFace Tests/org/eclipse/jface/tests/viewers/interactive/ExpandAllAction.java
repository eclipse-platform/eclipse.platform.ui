package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class ExpandAllAction extends TestBrowserAction {

	public ExpandAllAction(String label, TestBrowser browser) {
		super(label, browser);
	}
	public void run() {
		Viewer viewer = getBrowser().getViewer();
		if (viewer instanceof AbstractTreeViewer)
			((AbstractTreeViewer) viewer).expandAll();
	}
}
