/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Steven Spungin <steven@spungin.tv> - initial API and implementation, ongoing maintenance
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerUtil {

	// Sorts the column by the provider's text value
	static class ColumnLabelSorter extends TableViewerUtil.AbstractInvertableTableSorter {
		private final TableColumn col;

		ColumnLabelSorter(TableColumn col) {
			this.col = col;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			final TableViewer tableViewer = (TableViewer) viewer;
			final ColumnLabelProvider labelProvider = (ColumnLabelProvider) tableViewer.getLabelProvider(Arrays.asList(
				tableViewer.getTable().getColumns()).indexOf(col));
			return labelProvider.getText(e1).compareTo(labelProvider.getText(e2));
		}
	}

	static abstract class InvertableSorter extends ViewerSorter {
		@Override
		public abstract int compare(Viewer viewer, Object e1, Object e2);

		abstract InvertableSorter getInverseSorter();

		public abstract int getSortDirection();
	}

	static public abstract class AbstractInvertableTableSorter extends InvertableSorter {
		private final InvertableSorter inverse = new InvertableSorter() {

			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				return -1 * AbstractInvertableTableSorter.this.compare(viewer, e1, e2);
			}

			@Override
			InvertableSorter getInverseSorter() {
				return AbstractInvertableTableSorter.this;
			}

			@Override
			public int getSortDirection() {
				return SWT.DOWN;
			}
		};

		@Override
		InvertableSorter getInverseSorter() {
			return inverse;
		}

		@Override
		public int getSortDirection() {
			return SWT.UP;
		}
	}

	static public class TableSortSelectionListener implements SelectionListener {
		private final TableViewer viewer;
		private final TableColumn column;
		private final InvertableSorter sorter;
		private final boolean keepDirection;
		private InvertableSorter currentSorter;

		/**
		 * The constructor of this listener.
		 *
		 * @param viewer
		 *            the tableviewer this listener belongs to
		 * @param column
		 *            the column this listener is responsible for
		 * @param sorter
		 *            the sorter this listener uses
		 * @param defaultDirection
		 *            the default sorting direction of this Listener. Possible
		 *            values are {@link SWT.UP} and {@link SWT.DOWN}
		 * @param keepDirection
		 *            if true, the listener will remember the last sorting
		 *            direction of the associated column and restore it when the
		 *            column is reselected. If false, the listener will use the
		 *            default sorting direction
		 */
		public TableSortSelectionListener(TableViewer viewer, TableColumn column, AbstractInvertableTableSorter sorter,
			int defaultDirection, boolean keepDirection) {
			this.viewer = viewer;
			this.column = column;
			this.keepDirection = keepDirection;
			this.sorter = defaultDirection == SWT.UP ? sorter : sorter.getInverseSorter();
			currentSorter = this.sorter;

			this.column.addSelectionListener(this);
		}

		/**
		 * Chooses the column of this listener for sorting of the table. Mainly
		 * used when first initializing the table.
		 */
		public void chooseColumnForSorting() {
			viewer.getTable().setSortColumn(column);
			viewer.getTable().setSortDirection(currentSorter.getSortDirection());
			viewer.setSorter(currentSorter);
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			InvertableSorter newSorter;
			if (viewer.getTable().getSortColumn() == column) {
				newSorter = ((InvertableSorter) viewer.getSorter()).getInverseSorter();
			} else {
				if (keepDirection) {
					newSorter = currentSorter;
				} else {
					newSorter = sorter;
				}
			}

			currentSorter = newSorter;
			chooseColumnForSorting();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
			widgetSelected(e);
		}
	}

	static public void refreshAndPack(TableViewer viewer) {
		viewer.refresh();
		packAllColumns(viewer);
	}

	static public void updateAndPack(TableViewer viewer, Object object) {
		viewer.update(object, null);
		packAllColumns(viewer);
	}

	public static boolean isColumnClicked(TableViewer viewer, MouseEvent e, TableViewerColumn tvColumn) {
		boolean ret;
		final ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
		if (cell == null) {
			ret = false;
		} else {
			final int index = Arrays.asList(viewer.getTable().getColumns()).indexOf(tvColumn.getColumn());
			if (index == -1) {
				ret = false;
			} else {
				ret = index == cell.getColumnIndex();
			}
		}
		return ret;
	}

	public static Object getData(TableViewer viewer, MouseEvent e) {
		final ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
		if (cell == null) {
			return null;
		}
		return cell.getElement();
	}

	public static void packAllColumns(TableViewer viewer) {
		for (final TableColumn col : viewer.getTable().getColumns()) {
			col.pack();
		}
	}

	static public void resetColumnOrder(TableViewer tvResults) {
		final int[] order = tvResults.getTable().getColumnOrder();
		for (int i = 0; i < order.length; i++) {
			order[i] = i;
		}
		tvResults.getTable().setColumnOrder(order);
	}

	static public ArrayList<TableColumn> getColumnsInDisplayOrder(TableViewer viewer) {
		final ArrayList<TableColumn> allCols = new ArrayList<TableColumn>(Arrays.asList(viewer.getTable().getColumns()));
		final int[] order = viewer.getTable().getColumnOrder();
		Collections.sort(allCols, new Comparator<TableColumn>() {

			@Override
			public int compare(TableColumn o1, TableColumn o2) {
				return order[allCols.indexOf(o1)] - order[allCols.indexOf(o2)];
			}
		});
		return allCols;
	}

	static public int getVisibleColumnIndex(TableViewer tvResults2, TableColumn col) {
		final int createOrder = Arrays.asList(tvResults2.getTable().getColumns()).indexOf(col);
		if (createOrder == -1) {
			return -1;
		}
		return tvResults2.getTable().getColumnOrder()[createOrder];
	}
}