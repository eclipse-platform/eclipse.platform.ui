package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.swt.widgets.Composite;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

public class TestTree extends TestBrowser {
	TreeViewer fViewer;
	Action fExpandAllAction;
	public TestTree() {
		super();
		fExpandAllAction= new ExpandAllAction("Expand All", this);
	}
/**
 * 
 */
public Viewer createViewer(Composite parent) {
	TreeViewer viewer = new TreeViewer(parent);
	viewer.setContentProvider(new TestModelContentProvider());
	viewer.setUseHashlookup(true);

	if (fViewer == null)
		fViewer = viewer;
	return viewer;
}
	public static void main(String[] args) {
		TestBrowser browser= new TestTree();
		if (args.length > 0 && args[0].equals("-twopanes")) 							
			browser.show2Panes();
		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));		
	}
	public void testTreeFillMenuBar(MenuManager testMenu) {
		


	}
/**
 * Adds the expand all action to the tests menu.
 */
protected void viewerFillMenuBar(MenuManager mgr) {
	MenuManager testMenu = (MenuManager) (mgr.findMenuUsingPath("tests"));
	testMenu.add(new Separator());
	testMenu.add(fExpandAllAction);
}
}
