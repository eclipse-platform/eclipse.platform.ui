package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.jface.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.tests.viewers.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import java.util.*;

public class TestTableTree extends TestBrowser {

	TableTreeViewer fViewer;
	Action fExpandAllAction;

	public TestTableTree() {
		super();
		fExpandAllAction= new ExpandAllAction("Expand All", this);
	}
public Viewer createViewer(Composite parent) {
	TableTreeViewer viewer = new TableTreeViewer(parent);
	viewer.setContentProvider(new TestModelContentProvider());
	viewer.setLabelProvider(new TestTableTreeLabelProvider());
	viewer.getTableTree().getTable().setLinesVisible(true);

	TableLayout layout = new TableLayout();
	viewer.getTableTree().getTable().setLayout(layout);
	viewer.getTableTree().getTable().setHeaderVisible(true);
	String headers[] = { "First Column", "Second Column" };

	ColumnLayoutData layouts[] =
		{ new ColumnWeightData(100), new ColumnWeightData(100)};

	final TableColumn columns[] = new TableColumn[headers.length];

	for (int i = 0; i < headers.length; i++) {
		layout.addColumnData(layouts[i]);
		TableColumn tc = new TableColumn(viewer.getTableTree().getTable(), SWT.NONE, i);
		tc.setResizable(layouts[i].resizable);
		tc.setText(headers[i]);
		columns[i] = tc;
	}
	if (fViewer == null)
		fViewer = viewer;

	return viewer;
}
public static void main(String[] args) {
	TestBrowser browser = new TestTableTree();
	if (args.length > 0 && args[0].equals("-twopanes"))
		browser.show2Panes();
	browser.setBlockOnOpen(true);
	browser.open(TestElement.createModel(3, 10));
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
