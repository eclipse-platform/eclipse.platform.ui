package org.eclipse.jface.tests.viewers;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 1999, 2000
 */

import org.eclipse.jface.*;
import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import java.util.*;
import junit.framework.*;

public class TableViewerTest extends StructuredItemViewerTest {
public static class TableTestLabelProvider
	extends TestLabelProvider
	implements ITableLabelProvider {
	public boolean fExtended = false;

	public String getText(Object element) {
		if (fExtended)
			return providedString((String) element);
		return element.toString();
	}
	public String getColumnText(Object element, int index) {
		if (fExtended)
			return providedString((TestElement) element);
		return element.toString();
	}
	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}
}
public TableViewerTest(String name) {
	super(name);
}
/**
 * Creates the viewer used by this test, under the given parent widget.
 */
protected StructuredViewer createViewer(Composite parent) {
	TableViewer viewer = new TableViewer(parent);
	viewer.setContentProvider(new TestModelContentProvider());
	viewer.setLabelProvider(new TableTestLabelProvider());
	viewer.getTable().setLinesVisible(true);

	TableLayout layout = new TableLayout();
	viewer.getTable().setLayout(layout);
	viewer.getTable().setHeaderVisible(true);
	String headers[] = { "column 1 header", "column 2 header" };

	ColumnLayoutData layouts[] =
		{ new ColumnWeightData(100), new ColumnWeightData(100)};

	final TableColumn columns[] = new TableColumn[headers.length];

	for (int i = 0; i < headers.length; i++) {
		layout.addColumnData(layouts[i]);
		TableColumn tc = new TableColumn(viewer.getTable(), SWT.NONE, i);
		tc.setResizable(layouts[i].resizable);
		tc.setText(headers[i]);
		columns[i] = tc;
	}

	return viewer;
}
protected int getItemCount() {
	TestElement first = fRootElement.getFirstChild();
	TableItem ti = (TableItem) fViewer.testFindItem(first);
	Table table = ti.getParent();
	return table.getItemCount();
}
protected String getItemText(int at) {
	Table table = (Table) fViewer.getControl();
	return table.getItem(at).getText();
}
public static void main(String args[]) {
	junit.textui.TestRunner.run(TableViewerTest.class);
}
public void testLabelProvider() {

	TableViewer viewer = (TableViewer) fViewer;
	TableTestLabelProvider provider = (TableTestLabelProvider) viewer.getLabelProvider();

	provider.fExtended = true;
	// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
	fViewer.refresh();
	TestElement first = fRootElement.getFirstChild();
	String newLabel = providedString(first);
	assertEquals("rendered label", newLabel, getItemText(0));
	provider.fExtended = false;
	// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
}
public void testLabelProviderStateChange() {
	TableViewer tableviewer = (TableViewer) fViewer;
	TableTestLabelProvider provider = (TableTestLabelProvider) tableviewer.getLabelProvider();

	provider.fExtended = true;
	provider.setSuffix("added suffix");
	// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
	tableviewer.refresh();
	TestElement first = fRootElement.getFirstChild();
	String newLabel = providedString(first);
	assertEquals("rendered label", newLabel, getItemText(0));
	provider.fExtended = false;
	// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider changes
	fViewer.refresh();
}
}
