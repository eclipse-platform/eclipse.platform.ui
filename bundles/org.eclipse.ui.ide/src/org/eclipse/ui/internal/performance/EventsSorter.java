/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.performance;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.PerformanceStats;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class EventsSorter extends ViewerSorter {
	protected boolean reversed = false;

	protected int columnNumber;

	protected int[][] SORT_ORDERS_BY_COLUMN = {
			// Event
			{ PerformanceView.COLUMN_EVENT, PerformanceView.COLUMN_BLAME,
					PerformanceView.COLUMN_CONTEXT },
			// Blame
			{ PerformanceView.COLUMN_BLAME, PerformanceView.COLUMN_EVENT,
					PerformanceView.COLUMN_CONTEXT },
			// Context
			{ PerformanceView.COLUMN_CONTEXT, PerformanceView.COLUMN_EVENT,
					PerformanceView.COLUMN_BLAME },
			// Count
			{ PerformanceView.COLUMN_COUNT, PerformanceView.COLUMN_EVENT,
					PerformanceView.COLUMN_BLAME, PerformanceView.COLUMN_CONTEXT },
			// Time
			{ PerformanceView.COLUMN_TIME, PerformanceView.COLUMN_EVENT,
					PerformanceView.COLUMN_BLAME, PerformanceView.COLUMN_CONTEXT } };

	/**
	 * Create a new instance of the receiver sorted on columnNumber.
	 * 
	 * @param columnNumber
	 */
	public EventsSorter(int columnNumber) {
		this.columnNumber = columnNumber;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#sort(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object[])
	 */
	public void sort(final Viewer viewer, Object[] elements) {
		Comparator comparator = new Comparator() {
			Collator c = Collator.getInstance();

			/**
			 * Compares two stats objects, sorting first by the main column of
			 * this sorter, then by subsequent columns, depending on the column
			 * sort order.
			 */
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Comparator#compare(java.lang.Object,
			 *      java.lang.Object)
			 */
			public int compare(Object o1, Object o2) {
				PerformanceStats s1 = (PerformanceStats) o1;
				PerformanceStats s2 = (PerformanceStats) o2;
				// always sort failures above non-failures
				if (s1.isFailure() && !s2.isFailure())
					return -1;
				if (s2.isFailure() && !s1.isFailure())
					return 1;
				int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
				int result = 0;
				for (int i = 0; i < columnSortOrder.length; ++i) {
					result = compareColumnValue(columnSortOrder[i], s1, s2);
					if (result != 0)
						break;
				}
				if (reversed)
					result = -result;
				return result;
			}

			/**
			 * Compares two markers, based only on the value of the specified
			 * column.
			 */
			int compareColumnValue(int column, PerformanceStats s1,
					PerformanceStats s2) {
				switch (column) {
				case PerformanceView.COLUMN_EVENT:
					return c.compare(s1.getEvent(), s2.getEvent());
				case PerformanceView.COLUMN_BLAME:
					return c.compare(s1.getBlameString(), s2.getBlameString());
				case PerformanceView.COLUMN_CONTEXT:
					String name1 = s1.getContext() == null ? "" : s1.getContext(); //$NON-NLS-1$
					String name2 = s2.getContext() == null ? "" : s2.getContext(); //$NON-NLS-1$
					return c.compare(name1, name2);
				case PerformanceView.COLUMN_COUNT:
					return s2.getRunCount() - s1.getRunCount();
				case PerformanceView.COLUMN_TIME:
					return (int) (s2.getRunningTime() - s1.getRunningTime());
				}
				return 0;
			}
		};
		Arrays.sort(elements, comparator);
	}


}
