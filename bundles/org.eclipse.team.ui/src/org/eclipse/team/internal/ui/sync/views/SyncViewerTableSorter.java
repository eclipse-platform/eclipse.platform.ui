/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.views.navigator.ResourceSorter;

/**
 * Provides support for sorting the table viewer of the SyncViewer by column
 */
public class SyncViewerTableSorter extends SyncViewerSorter {

	private boolean reversed = false;
	private int columnNumber;
	
	//column constants
	public static final int COL_NAME = 0;
	public static final int COL_PARENT = 1;
	
	// column headings:	"Revision" "Tags" "Date" "Author" "Comment"
	private int[][] SORT_ORDERS_BY_COLUMN = {
		{COL_NAME, COL_PARENT},	/* name */ 
		{COL_PARENT, COL_NAME}	/* parent */
	};
	
	/**
	 * Return a listener that will change the sorter in the table when the column header 
	 * is clicked.
	 */
	public static SelectionListener getColumnListener(final TableViewer tableViewer) {
		/**
		 * This class handles selections of the column headers.
		 * Selection of the column header will cause resorting
		 * of the shown tasks using that column's sorter.
		 * Repeated selection of the header will toggle
		 * sorting order (ascending versus descending).
		 */
		return new SelectionAdapter() {
			/**
			 * Handles the case of user selecting the
			 * header area.
			 * <p>If the column has not been selected previously,
			 * it will set the sorter of that column to be
			 * the current tasklist sorter. Repeated
			 * presses on the same column header will
			 * toggle sorting order (ascending/descending).
			 */
			public void widgetSelected(SelectionEvent e) {
				// column selected - need to sort
				int column = tableViewer.getTable().indexOf((TableColumn) e.widget);
				SyncViewerTableSorter oldSorter = (SyncViewerTableSorter)tableViewer.getSorter();
				if (oldSorter != null && column == oldSorter.getColumnNumber()) {
					oldSorter.setReversed(!oldSorter.isReversed());
					tableViewer.refresh();
				} else {
					tableViewer.setSorter(new SyncViewerTableSorter(column));
				}
			}
		};
	}	
	
	/**
	 * 
	 */
	public SyncViewerTableSorter(int columnNumber) {
		super(ResourceSorter.NAME);
		this.columnNumber = columnNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		
		IResource resource1 = getResource(e1);
		IResource resource2 = getResource(e2);
		int result = 0;
		if (resource1 == null || resource2 == null) {
			result = super.compare(viewer, e1, e2);
		} else {
			int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
			for (int i = 0; i < columnSortOrder.length; ++i) {
				result = compareColumnValue(columnSortOrder[i], resource1, resource2);
				if (result != 0)
					break;
			}
		}
		if (reversed)
			result = -result;
		return result;
	}

	/**
	 * Compares two resources, based only on the value of the specified column.
	 */
	int compareColumnValue(int columnNumber, IResource e1, IResource e2) {
		switch (columnNumber) {
			case COL_NAME: /* revision */
			
				// Category behavior from superclass
				int cat1 = category(e1);
				int cat2 = category(e2);
	
				if (cat1 != cat2)
					return cat1 - cat2;
	
				// cat1 == cat2
				
				return getCollator().compare(e1.getName(), e2.getName());
			case COL_PARENT: /* parent */
				return getCollator().compare(e1.getParent().getFullPath().toString(), e2.getParent().getFullPath().toString());
			default:
				return 0;
		}
	}
	/**
	 * Returns the number of the column by which this is sorting.
	 */
	public int getColumnNumber() {
		return columnNumber;
	}
	/**
	 * Returns true for descending, or false
	 * for ascending sorting order.
	 */
	public boolean isReversed() {
		return reversed;
	}
	/**
	 * Sets the sorting order.
	 */
	public void setReversed(boolean newReversed) {
		reversed = newReversed;
	}
}
