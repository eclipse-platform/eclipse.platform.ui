/*******************************************************************************
 * Copyright (c) 2005, 2018 IBM Corporation and others.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.junit.Test;

public class TreeViewerColumnTest extends AbstractTreeViewerTest {

	public static class TableTreeTestLabelProvider extends TestLabelProvider implements ITableLabelProvider {
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

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TreeViewer viewer = new TreeViewer(parent);
		viewer.setContentProvider(new TestModelContentProvider());
		viewer.setLabelProvider(new TableTreeTestLabelProvider());
		viewer.getTree().setLinesVisible(true);

		viewer.getTree().setHeaderVisible(true);
		String headers[] = { "column 1 header", "column 2 header" };

		final TreeColumn columns[] = new TreeColumn[headers.length];

		for (int i = 0; i < headers.length; i++) {
			TreeColumn tc = new TreeColumn(viewer.getTree(), SWT.NONE, i);
			tc.setResizable(true);
			tc.setText(headers[i]);
			tc.setWidth(25);
			columns[i] = tc;
		}
		fTreeViewer = viewer;
		return viewer;
	}

	@Override
	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		TreeItem ti = (TreeItem) fViewer.testFindItem(first);
		return ti.getParent().getItemCount();
	}

	@Override
	protected int getItemCount(TestElement element) {
		TreeItem ti = (TreeItem) fViewer.testFindItem(element);
		return ti.getItemCount();
	}

	@Override
	protected String getItemText(int at) {
		return ((Tree) fViewer.getControl()).getItems()[at].getText();
	}

	private static ViewerColumn getViewerColumn(ColumnViewer viewer, int index) {
		Method method;
		try {
			method = ColumnViewer.class.getDeclaredMethod("getViewerColumn", new Class[] { int.class });
			method.setAccessible(true);
			return (ViewerColumn) method.invoke(viewer, Integer.valueOf(index));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void testViewerColumn() {
		assertNull(getViewerColumn((TreeViewer) fViewer, -1));
		assertNotNull(getViewerColumn((TreeViewer) fViewer, 0));
		assertNotNull(getViewerColumn((TreeViewer) fViewer, 1));
		assertNull(getViewerColumn((TreeViewer) fViewer, 2));
	}

	@Test
	@Override
	public void testLabelProvider() {
		TreeViewer viewer = (TreeViewer) fViewer;
		TableTreeTestLabelProvider provider = (TableTreeTestLabelProvider) viewer.getLabelProvider();
		provider.fExtended = true;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider
		// changes
		fViewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider
		// changes
		fViewer.refresh();
	}

	@Test
	@Override
	public void testLabelProviderStateChange() {
		TreeViewer viewer = (TreeViewer) fViewer;
		TableTreeTestLabelProvider provider = (TableTreeTestLabelProvider) viewer.getLabelProvider();
		provider.fExtended = true;
		provider.setSuffix("added suffix");
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider
		// changes
		fViewer.refresh();
		TestElement first = fRootElement.getFirstChild();
		String newLabel = providedString(first);
		assertEquals("rendered label", newLabel, getItemText(0));
		provider.fExtended = false;
		// BUG 1FZ5SDC: JFUIF:WINNT - TableViewerColumn should listen for LabelProvider
		// changes
		fViewer.refresh();
	}
}
