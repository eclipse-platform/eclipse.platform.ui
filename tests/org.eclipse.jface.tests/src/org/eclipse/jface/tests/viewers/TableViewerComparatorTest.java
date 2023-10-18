/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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
 ******************************************************************************/

package org.eclipse.jface.tests.viewers;

import static org.junit.Assert.assertEquals;

import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.junit.Test;

/**
 * @since 3.2
 *
 */
public class TableViewerComparatorTest extends ViewerComparatorTest {

	@Override
	protected StructuredViewer createViewer(Composite parent) {
		TableViewer viewer = new TableViewer(parent);
		viewer.setContentProvider(new TeamModelContentProvider());
		viewer.setLabelProvider(new TeamModelLabelProvider());
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

	@Test
	@SuppressWarnings("deprecation")
	public void testViewerSorter() {
		fViewer.setSorter(new ViewerSorter());
		assertSortedResult(TEAM1_SORTED);
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testViewerSorterInsertElement() {
		fViewer.setSorter(new ViewerSorter());
		team1.addMember("Duong");
		assertSortedResult(TEAM1_SORTED_WITH_INSERT);
	}

	@Test
	public void testViewerComparator() {
		fViewer.setComparator(new ViewerComparator());
		assertSortedResult(TEAM1_SORTED);
	}

	@Test
	public void testViewerComparatorInsertElement() {
		fViewer.setComparator(new ViewerComparator());
		team1.addMember("Duong");
		assertSortedResult(TEAM1_SORTED_WITH_INSERT);
	}

	private void assertSortedResult(String[] expected) {
		TableItem[] items = getTableViewer().getTable().getItems();
		for (int i = 0; i < items.length; i++) {
			TableItem item = items[i];
			assertEquals("Item not expected.  actual=" + item.getText() + " expected=", expected[i], item.getText());
		}
	}

	@Override
	protected void setInput() {
		fViewer.setInput(team1);
	}

	protected TableViewer getTableViewer() {
		return (TableViewer) fViewer;
	}

}
