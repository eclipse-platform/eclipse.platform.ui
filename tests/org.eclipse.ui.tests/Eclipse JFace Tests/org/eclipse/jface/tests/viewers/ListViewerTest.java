package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;

public class ListViewerTest extends StructuredViewerTest {
	
	public ListViewerTest(String name) {
		super(name);
	}
	protected StructuredViewer createViewer(Composite parent) {
		ListViewer viewer= new ListViewer(parent);
		viewer.setContentProvider(new TestModelContentProvider());
		return viewer;
	}
	protected int getItemCount() {
		TestElement first= fRootElement.getFirstChild();
		List list= (List)fViewer.testFindItem(first);
		return list.getItemCount();
	}
	protected String getItemText(int at) {
		List list= (List) fViewer.getControl();
		return list.getItem(at);
	}
	public static void main(String args[]) {
		junit.textui.TestRunner.run(ListViewerTest.class);
	}
}
