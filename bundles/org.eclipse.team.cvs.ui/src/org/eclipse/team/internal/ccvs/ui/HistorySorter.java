package org.eclipse.team.internal.ccvs.ui;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.*;
import org.eclipse.team.ccvs.core.ILogEntry;

import java.text.Collator;import java.util.Date;

/**
 * This sorter is used by the HistoryView
 */
class HistorySorter extends ViewerSorter {
	private boolean reversed = false;
	private int columnNumber;
	
	private VersionCollator versionCollator = new VersionCollator();
	
	// column headings:	"Revision" "Tags" "Date" "Author" "Comment"
	private int[][] SORT_ORDERS_BY_COLUMN = {
		{0, 4, 1, 2, 3},	/* version */ 
		{1, 0, 2, 3, 4},	/* tags */
		{1, 4, 0, 2, 3},	/* date */
		{2, 4, 1, 3, 0},	/* author */
		{3, 4, 1, 2, 0}	/* comment */
	};
	
	/**
	 * The constructor.
	 */
	public HistorySorter(int columnNumber) {
		this.columnNumber = columnNumber;
	}
	/**
	 * Compares two log entries, sorting first by the main column of this sorter,
	 * then by subsequent columns, depending on the column sort order.
	 */
	public int compare(Viewer viewer, Object o1, Object o2) {
		ILogEntry e1 = (ILogEntry)o1;
		ILogEntry e2 = (ILogEntry)o2;
		int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
		int result = 0;
		for (int i = 0; i < columnSortOrder.length; ++i) {
			result = compareColumnValue(columnSortOrder[i], e1, e2);
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
	int compareColumnValue(int columnNumber, ILogEntry e1, ILogEntry e2) {
		switch (columnNumber) {
			case 0: /* version */
				return versionCollator.compare(e1.getRevision(), e2.getRevision());
			case 1: /* tags */
				String[] tags1 = e1.getTags();
				String[] tags2 = e2.getTags();
				if (tags2.length == 0) {
					return -1;
				}
				if (tags1.length == 0) {
					return 1;
				}
				return getCollator().compare(tags1[0], tags2[0]);
			case 2: /* date  */
				// To do: use dates instead of strings
				String date1 = e1.getDate();
				String date2 = e2.getDate();
				//return date1.compareTo(date2);
				return getCollator().compare(date1, date2);
			case 3: /* author */
				return getCollator().compare(e1.getAuthor(), e2.getAuthor());
			case 4: /* comment */
				return getCollator().compare(e1.getComment(), e2.getComment());
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
