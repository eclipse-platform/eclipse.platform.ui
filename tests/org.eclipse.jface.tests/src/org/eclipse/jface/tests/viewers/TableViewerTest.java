/*******************************************************************************
 * Copyright (c) 2000, 2025 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

public class TableViewerTest extends StructuredItemViewerTest {
	public static class TableTestLabelProvider extends TestLabelProvider implements ITableLabelProvider {
		public boolean fExtended = false;

		@Override
		public String getText(Object element) {
			if (fExtended) {
				return providedString((String) element);
			}
			return element.toString();
		}

		@Override
		public String getColumnText(Object element, int index) {
			if (fExtended) {
				return providedString((TestElement) element);
			}
			return element.toString();
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}

	/**
	 * Creates the viewer used by this test, under the given parent widget.
	 */
	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = createTableViewer(parent);
		viewer.setContentProvider(getContentProvider());
		viewer.setLabelProvider(new TableTestLabelProvider());
		viewer.getTable().setLinesVisible(true);

		TableLayout layout = new TableLayout();
		viewer.getTable().setLayout(layout);
		viewer.getTable().setHeaderVisible(true);
		String headers[] = { "column 1 header", "column 2 header" };

		ColumnLayoutData layouts[] = { new ColumnWeightData(100), new ColumnWeightData(100) };

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

	ViewerColumn getViewerColumn(ColumnViewer viewer, int index) {
		Method method;
		try {
			method = ColumnViewer.class.getDeclaredMethod("getViewerColumn", int.class);
			method.setAccessible(true);
			return (ViewerColumn) method.invoke(viewer, Integer.valueOf(index));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testViewerColumn() {
		assertNull(getViewerColumn((TableViewer) fViewer, -1));
		assertNotNull(getViewerColumn((TableViewer) fViewer, 0));
		assertNotNull(getViewerColumn((TableViewer) fViewer, 1));
		assertNull(getViewerColumn((TableViewer) fViewer, 2));
	}

	/**
	 * Get the content provider for the viewer.
	 *
	 * @return IContentProvider
	 */
	protected TestModelContentProvider getContentProvider() {
		return new TestModelContentProvider();
	}

	/**
	 * Create the table viewer for the test
	 *
	 * @return The newly created TableViewer.
	 */
	protected TableViewer createTableViewer(Composite parent) {
		return new TableViewer(parent);
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TableItem ti = (TableItem) fViewer.testFindItem(first);
		Table table = ti.getParent();
		return table.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		Table table = (Table) fViewer.getControl();
		return table.getItem(at).getText();
	}

	@Test
	@Override
	public void testLabelProvider() {

		TableViewer viewer = (TableViewer) fViewer;
		TableTestLabelProvider provider = (TableTestLabelProvider) viewer.getLabelProvider();

		provider.fExtended = true;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		fViewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
	}

	@Test
	@Override
	public void testLabelProviderStateChange() {
		TableViewer tableviewer = (TableViewer) fViewer;
		TableTestLabelProvider provider = (TableTestLabelProvider) tableviewer.getLabelProvider();

		provider.fExtended = true;
		provider.setSuffix("added suffix");
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		tableviewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for
		// LabelProvider changes
		fViewer.refresh();
	}

	@Test
	public void testRemove() {
		TableViewer tableviewer = (TableViewer) fViewer;
		TestElement first = fRootElement.getFirstChild();
		((TestElement) fViewer.getInput()).deleteChild(first);
		tableviewer.remove(first);
		assertNull("Removed item still exists", fViewer.testFindItem(first));

	}

	@Test
	public void testContains() {
		TableViewer tViewer = (TableViewer) fViewer;
		// some random element.
		assertFalse("element must not be available on the viewer", tViewer.contains(""));

		// first child of root.
		assertTrue("element must be available on the viewer", tViewer.contains(fRootElement.getFirstChild()));

		// last child of the root
		assertTrue("element must be available on the viewer", tViewer.contains(fRootElement.getLastChild()));
	}

}
