/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class TableComparator extends ViewerComparator implements Comparator {

	public static final int MAX_DEPTH = 4;

	public static final int ASCENDING = 1;

	public static final int DESCENDING = -1;

	protected IField[] fields;

	protected int[] priorities;

	protected int[] directions;

	protected int[] defaultPriorities;

	protected int[] defaultDirections;

	public static final String TAG_DIALOG_SECTION = "sorter"; //$NON-NLS-1$

	private static final String TAG_PRIORITY = "priority"; //$NON-NLS-1$ 

	private static final String TAG_DIRECTION = "direction"; //$NON-NLS-1$

	private static final String TAG_DEFAULT_PRIORITY = "defaultPriority"; //$NON-NLS-1$

	private static final String TAG_DEFAULT_DIRECTION = "defaultDirection"; //$NON-NLS-1$

	public TableComparator(TableComparator other) {
		this(other.getFields(), other.getDefaultPriorities(), other
				.getDefaultDirections());
		priorities = other.getPriorities();
		directions = other.getDirections();
	}

	public TableComparator(IField[] properties, final int[] defaultPriorities,
			final int[] defaultDirections) {
		super();
		this.fields = properties;
		if (properties == null
				|| defaultPriorities == null
				|| defaultDirections == null
				|| !(properties.length == defaultPriorities.length && properties.length == defaultDirections.length)
				|| !verifyPriorities(defaultPriorities)
				|| !verifyDirections(defaultDirections)) {
			this.priorities = new int[0];
			this.directions = new int[0];
			this.defaultPriorities = new int[0];
			this.defaultDirections = new int[0];
		} else {
			this.priorities = new int[defaultPriorities.length];
			System.arraycopy(defaultPriorities, 0, this.priorities, 0,
					priorities.length);
			this.directions = new int[defaultDirections.length];
			System.arraycopy(defaultDirections, 0, this.directions, 0,
					directions.length);
			this.defaultPriorities = new int[defaultPriorities.length];
			System.arraycopy(defaultPriorities, 0, this.defaultPriorities, 0,
					defaultPriorities.length);
			this.defaultDirections = new int[defaultDirections.length];
			System.arraycopy(defaultDirections, 0, this.defaultDirections, 0,
					defaultDirections.length);
		}
	}

	/**
	 * Return a TableSorter based on the supplied fields.
	 * 
	 * @param sortingFields
	 */
	static TableComparator createTableSorter(IField[] sortingFields) {
		int[] defaultPriorities = new int[sortingFields.length];
		for (int i = 0; i < defaultPriorities.length; i++) {
			defaultPriorities[i] = i;
		}

		int[] directions = new int[sortingFields.length];
		for (int i = 0; i < directions.length; i++) {
			directions[i] = sortingFields[i].getDefaultDirection();

		}

		return new TableComparator(sortingFields, defaultPriorities, directions);
	}

	protected void resetState() {
		System
				.arraycopy(defaultPriorities, 0, priorities, 0,
						priorities.length);
		System
				.arraycopy(defaultDirections, 0, directions, 0,
						directions.length);
	}

	public void reverseTopPriority() {
		directions[priorities[0]] *= -1;
	}

	public void setTopPriority(IField property) {
		for (int i = 0; i < fields.length; i++) {
			if (fields[i].equals(property)) {
				setTopPriority(i);
				return;
			}
		}
	}

	public void setTopPriority(int priority) {
		if (priority < 0 || priority >= priorities.length) {
			return;
		}

		int index = -1;
		for (int i = 0; i < priorities.length; i++) {
			if (priorities[i] == priority) {
				index = i;
			}
		}

		if (index == -1) {
			resetState();
			return;
		}

		// shift the array
		for (int i = index; i > 0; i--) {
			priorities[i] = priorities[i - 1];
		}
		priorities[0] = priority;
		directions[priority] = defaultDirections[priority];
	}

	public void setTopPriorityDirection(int direction) {
		if (direction == ASCENDING || direction == DESCENDING) {
			directions[priorities[0]] = direction;
		}
	}

	public int getTopPriorityDirection() {
		return directions[priorities[0]];
	}

	public int getTopPriority() {
		return priorities[0];
	}

	/**
	 * Return the field at the top priority.
	 * 
	 * @return IField
	 */
	public IField getTopField() {
		return fields[getTopPriority()];
	}

	public int[] getPriorities() {
		int[] copy = new int[priorities.length];
		System.arraycopy(priorities, 0, copy, 0, copy.length);
		return copy;
	}

	public int[] getDirections() {
		int[] copy = new int[directions.length];
		System.arraycopy(directions, 0, copy, 0, copy.length);
		return copy;
	}

	public int[] getDefaultPriorities() {
		int[] copy = new int[defaultPriorities.length];
		System.arraycopy(defaultPriorities, 0, copy, 0, copy.length);
		return copy;
	}

	public int[] getDefaultDirections() {
		int[] copy = new int[defaultDirections.length];
		System.arraycopy(defaultDirections, 0, copy, 0, copy.length);
		return copy;
	}

	public int compare(Viewer viewer, Object e1, Object e2) {
		return compare(e1, e2, 0, true);
	}

	/**
	 * Compare obj1 and obj2 at depth. If continueSearching continue searching
	 * below depth to continue the comparison.
	 * 
	 * @param obj1
	 * @param obj2
	 * @param depth
	 * @param continueSearching
	 * @return int
	 */
	protected int compare(Object obj1, Object obj2, int depth,
			boolean continueSearching) {
		if (depth >= priorities.length) {
			return 0;
		}

		int column = priorities[depth];
		IField property = fields[column];
		int result = property.compare(obj1, obj2);
		if (result == 0 && continueSearching) {
			return compare(obj1, obj2, depth + 1, continueSearching);
		}
		return result * directions[column];
	}

	/**
	 * @return IField[] an array of fields
	 */
	public IField[] getFields() {
		return fields;
	}

	private boolean verifyPriorities(int[] priorities) {
		int length = priorities.length;
		boolean[] included = new boolean[length];
		Arrays.fill(included, false);
		for (int i = 0; i < length; i++) {
			int priority = priorities[i];
			if (priority < 0 || priority >= length) {
				return false;
			}
			if (included[priority]) {
				return false;
			}
			included[priority] = true;
		}
		return true;
	}

	private boolean verifyDirections(int[] directions) {
		for (int i = 0; i < directions.length; i++) {
			if (directions[i] != ASCENDING && directions[i] != DESCENDING) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return compare(null, o1, o2);
	}

	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = dialogSettings.addNewSection(TAG_DIALOG_SECTION);
		}

		for (int i = 0; i < priorities.length; i++) {
			settings.put(TAG_PRIORITY + i, priorities[i]);
			settings.put(TAG_DIRECTION + i, directions[i]);
			settings.put(TAG_DEFAULT_PRIORITY + i, defaultPriorities[i]);
			settings.put(TAG_DEFAULT_DIRECTION + i, defaultDirections[i]);
		}
	}

	public void restoreState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			resetState();
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TAG_DIALOG_SECTION);
		if (settings == null) {
			resetState();
			return;
		}

		try {
			for (int i = 0; i < priorities.length; i++) {
				String priority = settings.get(TAG_PRIORITY + i);
				if (priority == null) {
					resetState();
					return;
				}
				
				int fieldIndex = Integer.parseInt(priority);
				
				//Make sure it is not old data from a different sized array
				if(fieldIndex < fields.length) {
					priorities[i] = fieldIndex;
				}
				
				String direction = settings.get(TAG_DIRECTION + i);
				if (direction == null) {
					resetState();
					return;
				}
				directions[i] = Integer.parseInt(direction);
				String defaultPriority = settings.get(TAG_DEFAULT_PRIORITY + i);
				if (defaultPriority == null) {
					resetState();
					return;
				}
				defaultPriorities[i] = Integer.parseInt(defaultPriority);
				String defaultDirection = settings.get(TAG_DEFAULT_DIRECTION
						+ i);
				if (defaultDirection == null) {
					resetState();
					return;
				}
				defaultDirections[i] = Integer.parseInt(defaultDirection);
			}
		} catch (NumberFormatException e) {
			resetState();
		}
	}

	/**
	 * Sort the array of markers in lastMarkers in place.
	 * 
	 * @param viewer
	 * @param lastMarkers
	 */
	public void sort(TreeViewer viewer, MarkerList lastMarkers) {
		sort(viewer, lastMarkers.getArray());

	}

	/**
	 * Sorts the given elements in-place, modifying the given array from index
	 * start to index end. <
	 * 
	 * @param viewer
	 * @param elements
	 * @param start
	 * @param end
	 */
	public void sort(final Viewer viewer, Object[] elements, int start, int end) {
		Arrays.sort(elements, start, end, new Comparator() {
			public int compare(Object a, Object b) {
				return TableComparator.this.compare(viewer, a, b);
			}
		});
	}

}
