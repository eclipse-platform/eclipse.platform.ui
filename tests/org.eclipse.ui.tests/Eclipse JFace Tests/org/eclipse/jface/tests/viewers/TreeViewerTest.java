package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;

public class TreeViewerTest extends AbstractTreeViewerTest {
			
	public TreeViewerTest(String name) {
		super(name);
	}
	protected StructuredViewer createViewer(Composite parent) {
		fTreeViewer= new TreeViewer(parent);
		fTreeViewer.setContentProvider(new TestModelContentProvider());
		return fTreeViewer;
	}
	protected int getItemCount() {
		TestElement first= fRootElement.getFirstChild();
		TreeItem ti= (TreeItem)fViewer.testFindItem(first);
		Tree tree= ti.getParent();		
		return tree.getItemCount();
	}
/**
 * getItemCount method comment.
 */
protected int getItemCount(TestElement element) {
	return 0;
}
	protected String getItemText(int at) {
		Tree tree= (Tree) fTreeViewer.getControl();
		return tree.getItems()[at].getText();
	}
	public static void main(String args[]) {
		junit.textui.TestRunner.run(TreeViewerTest.class);
	}
}
