package org.eclipse.jface.tests.viewers.interactive;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.tests.viewers.TestElement;
import org.eclipse.jface.tests.viewers.TestLabelProvider;
import org.eclipse.jface.tests.viewers.TestModelContentProvider;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;

public class TestTable extends TestBrowser {

public Viewer createViewer(Composite parent) {
	TableViewer viewer = new TableViewer(parent);
	viewer.setContentProvider(new TestModelContentProvider());
	viewer.setLabelProvider(new TestLabelProvider());
	viewer.getTable().setLinesVisible(true);

	TableLayout layout = new TableLayout();
	viewer.getTable().setLayout(layout);
	viewer.getTable().setHeaderVisible(true);
	String headers[] = { "Label Column", "Second Column" };

	ColumnLayoutData layouts[] =
		{ new ColumnWeightData(100, false), new ColumnWeightData(100, false)};

	final TableColumn columns[] = new TableColumn[headers.length];

	for (int i = 0; i < headers.length; i++) {
		layout.addColumnData(layouts[i]);
		TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
		tc.setResizable(layouts[i].resizable);
		tc.setText(headers[i]);
		columns[i] = tc;
	}

	viewer.setUseHashlookup(true);

	return viewer;
}
	public static void main(String[] args) {
		TestTable browser = new TestTable();
		browser.setBlockOnOpen(true);
		browser.open(TestElement.createModel(3, 10));
	}
/**
 * 
 */
protected void viewerFillMenuBar(MenuManager mgr) {
}
}
