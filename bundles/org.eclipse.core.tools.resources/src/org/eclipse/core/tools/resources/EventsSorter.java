/**********************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools.resources;

import java.text.Collator;
import java.util.Arrays;
import java.util.Comparator;
import org.eclipse.core.internal.events.EventStats;
import org.eclipse.core.tools.ISorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class EventsSorter extends ViewerSorter implements ISorter {
	protected boolean reversed = false;
	protected int columnNumber;

	protected int[][] SORT_ORDERS_BY_COLUMN = {
	// Statistic Id
			{EventsView.STAT_ID_COLUMN, EventsView.PROJECT_COLUMN},
			// Project
			{EventsView.PROJECT_COLUMN, EventsView.STAT_ID_COLUMN, EventsView.TIME_COLUMN},
			// Count
			{EventsView.COUNT_COLUMN, EventsView.STAT_ID_COLUMN, EventsView.PROJECT_COLUMN},
			// Time
			{EventsView.TIME_COLUMN, EventsView.STAT_ID_COLUMN, EventsView.PROJECT_COLUMN},
			// Core Exceptions
			{EventsView.EXCEPTIONS_COLUMN, EventsView.STAT_ID_COLUMN}};

	public EventsSorter(int columnNumber) {
		this.columnNumber = columnNumber;
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

	/*
	 * Overrides method from ViewerSorter
	 */
	public void sort(final Viewer viewer, Object[] elements) {
		Comparator c = new Comparator() {
			Collator c = Collator.getInstance();

			/**
			 * Compares two stats objects, sorting first by the main column of this sorter,
			 * then by subsequent columns, depending on the column sort order.
			 */
			public int compare(Object o1, Object o2) {
				EventStats s1 = (EventStats) o1;
				EventStats s2 = (EventStats) o2;
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
			 * Compares two markers, based only on the value of the specified column.
			 */
			int compareColumnValue(int columnNumber, EventStats s1, EventStats s2) {
				if (s1.getProject() == null) {
					if (s2.getProject() != null)
						return -1;
				} else {
					if (s2.getProject() == null)
						return +1;
				}
				boolean notification = s1.getProject() == null;
				switch (columnNumber) {
					case EventsView.STAT_ID_COLUMN : {
						return c.compare(s1.getName(), s2.getName());
					}
					case EventsView.PROJECT_COLUMN : {
						if (notification)
							return 0;
						return c.compare(s1.getProject().getName(), s2.getProject().getName());
					}
					case EventsView.COUNT_COLUMN : {
						if (notification)
							return s1.getNotifyCount() - s2.getNotifyCount();
						return s1.getBuildCount() - s2.getBuildCount();
					}
					case EventsView.TIME_COLUMN : {
						if (notification)
							return (int) (s1.getNotifyRunningTime() - s2.getNotifyRunningTime());
						return (int) (s1.getBuildRunningTime() - s2.getBuildRunningTime());
					}
					case EventsView.EXCEPTIONS_COLUMN : {
						return s1.getExceptionCount() - s2.getExceptionCount();
					}
				}
				return 0;
			}
		};
		Arrays.sort(elements, c);
	}

	public int states() {
		return 2;
	}
}