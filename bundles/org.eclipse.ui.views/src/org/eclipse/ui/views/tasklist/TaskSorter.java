/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp. and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
	IBM Corporation - Initial implementation
**********************************************************************/

package org.eclipse.ui.views.tasklist;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * This is the task list's sorter.
 */
class TaskSorter extends ViewerSorter {
	private int[] priorities;
	private int direction;
	
	final static int ASCENDING = 1;
	final static int DESCENDING = -1;
	
	/**
	 * Creates a new task sorter.
	 */
	public TaskSorter() {
		priorities = new int[9];
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
		return compareColumnValue(m1, m2, 0) * direction;
	}

	public void setTopPriority(int priority) {
		if (priority < 0 || priority > 8)
			return;
		
		int index = -1;
		for (int i = 0; i < priorities.length; i++) {
			if (priorities[i] == priority)
				index = i;
		}
		
		if (index == -1) {
			resetState();
			direction = ASCENDING;
			return;
		}
			
		//shift the array
		for (int i = index; i > 0; i--) {
			priorities[i] = priorities[i - 1];
		}
		priorities[0] = priority;
	}
	
	public int getTopPriority() {
		return priorities[0];
	}
	
	public void setDirection(int direction) {
		if (direction == ASCENDING || direction == DESCENDING)
			this .direction = direction;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public void reverse() {
		direction *= -1;
	}
	
	public void resetState() {
		for (int i = 0; i < priorities.length; i++)
			priorities[i] = i; 
		direction = ASCENDING;
	}
	
	/* (non-Javadoc)
	 * Method declared on ViewerSorter.
	 */
	/**
	 * Compares two markers, based only on the value of the specified column.
	 */
	private int compareColumnValue(IMarker m1, IMarker m2, int depth) {
		if (depth >= priorities.length)
			return 0;
		
		int columnNumber = priorities[depth];
		switch (columnNumber) {
			case 0: { 
				/* category */
				int result = getCategoryOrder(m1) - getCategoryOrder(m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 1: {
				 /* completed */
				int result = getCompletedOrder(m1) - getCompletedOrder(m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 2: { 
				/* priority */
				int result = getPriorityOrder(m1) - getPriorityOrder(m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 3: { 
				/* description */
				int result = collator.compare(MarkerUtil.getMessage(m1), MarkerUtil.getMessage(m2));
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 4: {
				/* resource name */
				// Optimization: if the markers' resources are equal, then their names are the same.
				// If resources are equal, chances are they're identical; don't take hit for full equality comparison.
				IResource r1 = m1.getResource();
				IResource r2 = m2.getResource();
				if (r1.equals(r2))
					return compareColumnValue(m1, m2, depth + 1);
				String n1 = r1.getName();
				String n2 = r2.getName();
				int result = collator.compare(n1, n2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 5: {
				/* container name */
				// Optimization: if the markers' resources are equal, then container names are the same.
				// If resources are equal, chances are they're identical; don't take hit for full equality comparison.
				if (m1.getResource().equals(m2.getResource()))
					return compareColumnValue(m1, m2, depth + 1);
				String c1 = MarkerUtil.getContainerName(m1);
				String c2 = MarkerUtil.getContainerName(m2);
				int result = c1.equals(c2) ? 0 : collator.compare(c1, c2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 6: {
				/* line and location */
				int result = compareLineAndLocation(m1, m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 7: {
				/* creation time */
				int result = compareCreationTime(m1, m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			case 8: {
				/* id */
				int result = compareId(m1, m2);
				if (result == 0)
					return compareColumnValue(m1, m2, depth + 1);
				return result;
			}
			default:
				return 0;
		}
	}

	/**
	 * Compares the ids of two markers.
	 */
	private int compareId(IMarker m1, IMarker m2) {
		long result = m1.getId() - m2.getId();
		if (result > 0) return 1;
		else if (result < 0) return -1;
		return 0;
	}

	/**
	 * Compares the creation time of two markers.
	 */
	private int compareCreationTime(IMarker m1, IMarker m2) {
		long result;
		try {
			result = m1.getCreationTime() - m2.getCreationTime();
		} catch (CoreException e) {
			result = 0;
		}
		if (result > 0) return 1;
		else if (result < 0) return -1;
		return 0;
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
			return 4;
		}
		return 1000;
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
	public void saveState(IDialogSettings settings) {
		if (settings == null)
			return;
			
		settings.put("columnCount", priorities.length);//$NON-NLS-1$
		settings.put("direction", direction);//$NON-NLS-1$
		for (int i = 0; i < priorities.length; i++) 
			settings.put("priority" + i, priorities[i]);//$NON-NLS-1$
	}	
	
	public void restoreState(IDialogSettings settings) {
		if (settings == null)
			return;
		
		try {
			int columnCount = settings.getInt("columnCount");//$NON-NLS-1$
			if (priorities.length != columnCount)
				priorities = new int[columnCount];
			direction = settings.getInt("direction");//$NON-NLS-1$
			for (int i = 0; i < priorities.length; i++)
				priorities[i] = settings.getInt("priority" + i);//$NON-NLS-1$
		}
		catch (NumberFormatException e) {
			resetState();
		}
	}

}