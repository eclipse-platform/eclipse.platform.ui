package org.eclipse.ui.views.tasklist;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IMarker;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This is the abstract superclass of sorters in the task list.
 */
/* package */ class TaskSorter extends ViewerSorter {
	private TaskList tasklist;
	private boolean reversed = false;
	private int columnNumber;
	
	private static final int NUM_COLUMNS = 7;
	
	// column headings:	"","C", "!","Description","Resource Name", "In Folder", "Location"
	private static final int[][] SORT_ORDERS_BY_COLUMN = {
		{0, 2, 4, 5, 6, 3, 1},	/* category */ 
		{1, 0, 2, 4, 5, 6, 3},	/* completed */
		{2, 0, 4, 5, 6, 3, 1},	/* priority */
		{3, 4, 5, 6, 0, 2, 1},	/* description */
		{4, 5, 6, 3, 0, 2, 1},	/* resource */
		{5, 4, 6, 3, 0, 2, 1},	/* container */
		{6, 4, 5, 3, 0, 2, 1} 	/* location */
	};
/**
 * Creates a new task sorter.
 */
public TaskSorter(TaskList tasklist, int columnNumber) {
	this.tasklist = tasklist;
	this.columnNumber = columnNumber;
}
/* (non-Javadoc)
 * Method declared on ViewerSorter.
 */
/**
 * Compares two markers, sorting first by the main column of this sorter,
 * then by subsequent columns, depending on the column sort order.
 */
public int compare(Viewer viewer, Object e1, Object e2) {
	IMarker m1 = (IMarker) e1;
	IMarker m2 = (IMarker) e2;
	int[] columnSortOrder = SORT_ORDERS_BY_COLUMN[columnNumber];
	int result = 0;
	for (int i = 0; i < NUM_COLUMNS; ++i) {
		result = compareColumnValue(columnSortOrder[i], m1, m2);
		if (result != 0)
			break;
	}
	if (reversed)
		result = -result;
	return result;
}
/* (non-Javadoc)
 * Method declared on ViewerSorter.
 */
/**
 * Compares two markers, based only on the value of the specified column.
 */
private int compareColumnValue(int columnNumber, IMarker m1, IMarker m2) {
	switch (columnNumber) {
		case 0: /* category */
			return getCategoryOrder(m1) - getCategoryOrder(m2);
		case 1: /* completed */
			return getCompletedOrder(m1) - getCompletedOrder(m2);
		case 2: /* priority */
			return getPriorityOrder(m1) - getPriorityOrder(m2);
		case 3: /* description */
			return collator.compare(MarkerUtil.getMessage(m1), MarkerUtil.getMessage(m2));
		case 4: /* resource name */
			// Optimization: if the markers' resources are equal, then their names are the same.
			// If resources are equal, chances are they're identical; don't take hit for full equality comparison.
			if (m1.getResource() == m2.getResource())
				return 0;
			return collator.compare(MarkerUtil.getResourceName(m1), MarkerUtil.getResourceName(m2));
		case 5: /* container name */
			// Optimization: if the markers' resources are equal, then container names are the same.
			// If resources are equal, chances are they're identical; don't take hit for full equality comparison.
			if (m1.getResource() == m2.getResource())
				return 0;
			return collator.compare(MarkerUtil.getContainerName(m1), MarkerUtil.getContainerName(m2));
		case 6: /* line and location */
			return compareLineAndLocation(m1, m2);
		default:
			return 0;
	}
}
/**
 * Compares the line number and location of the two markers.
 * If line number is specified for both, this sorts first by line number (numerically), 
 * then by start offset (numerically), then by location (textually).
 * If line number is not specified for either, this sorts by location.
 * Otherwise, if only one has a line number, this sorts by the combined text for line number and location.
 */
private int compareLineAndLocation(IMarker m1, IMarker m2) {
	int line1 = MarkerUtil.getLineNumber(m1);
	int line2 = MarkerUtil.getLineNumber(m2);
	if (line1 != -1 && line2 != -1) {
		if (line1 != line2) {
			return line1 - line2;
		}
		int start1 = MarkerUtil.getCharStart(m1);
		int start2 = MarkerUtil.getCharStart(m2);
		if (start1 != -1 && start2 != -1) {
			if (start1 != start2) {
				return start1 - start2;
			}
		}
		String loc1 = MarkerUtil.getLocation(m1);
		String loc2 = MarkerUtil.getLocation(m2);
		return collator.compare(loc1, loc2);
	}
	if (line1 == -1 && line2 == -1) {
		String loc1 = MarkerUtil.getLocation(m1);
		String loc2 = MarkerUtil.getLocation(m2);
		return collator.compare(loc1, loc2);
	}
	String loc1 = MarkerUtil.getLineAndLocation(m1);
	String loc2 = MarkerUtil.getLineAndLocation(m2);
	return collator.compare(loc1, loc2);
}
/**
 * Returns the sort order for the given marker based on its category.
 * Lower numbers appear first.
 */
private int getCategoryOrder(IMarker marker) {
	if (MarkerUtil.isMarkerType(marker, IMarker.PROBLEM)) {
		switch (MarkerUtil.getSeverity(marker)) {
			case IMarker.SEVERITY_ERROR:
				return 1;
			case IMarker.SEVERITY_WARNING:
				return 2;
			case IMarker.SEVERITY_INFO:
				return 3;
		}
	} else if (MarkerUtil.isMarkerType(marker, IMarker.TASK)) {
		return 0;
	}
	return 1000;
}
/**
 * Returns the number of the column by which this is sorting.
 */
public int getColumnNumber() {
	return columnNumber;
}
/**
 * Returns the sort order for the given marker based on its completion status.
 * Lower numbers appear first.
 */
private int getCompletedOrder(IMarker marker) {
	return MarkerUtil.isComplete(marker) ? 0 : 1;
}
/**
 * Returns the sort order for the given marker based on its priority.
 * Lower numbers appear first.
 */
private int getPriorityOrder(IMarker marker) {
	// want HIGH to appear first
	return IMarker.PRIORITY_HIGH - MarkerUtil.getPriority(marker);
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
