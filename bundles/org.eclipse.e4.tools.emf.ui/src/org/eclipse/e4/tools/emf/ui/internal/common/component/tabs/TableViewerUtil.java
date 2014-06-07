/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.tools.emf.ui.internal.common.component.tabs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import java.util.Arrays;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerUtil {
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
		ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
		if (cell == null) {
			ret = false;
		} else {
			int index = Arrays.asList(viewer.getTable().getColumns()).indexOf(tvColumn.getColumn());
			if (index == -1) {
				ret = false;
			} else {
				ret = index == cell.getColumnIndex();
			}
		}
		return ret;
	}

	public static Object getData(TableViewer viewer, MouseEvent e) {
		ViewerCell cell = viewer.getCell(new Point(e.x, e.y));
		if (cell == null) {
			return null;
		} else {
			return cell.getElement();
		}
	}

	public static void packAllColumns(TableViewer viewer) {
		for (TableColumn col : viewer.getTable().getColumns()) {
			col.pack();
		}
	}

	static public void resetColumnOrder(TableViewer tvResults) {
		int[] order = tvResults.getTable().getColumnOrder();
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
}