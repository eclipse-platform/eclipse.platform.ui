/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.views.markers.internal;

import java.util.Comparator;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * CategorySorter is the sorter that takes categories and the viewer into
 * account.
 * 
 */
public class CategoryComparator extends ViewerComparator implements Comparator {
	TableComparator innerSorter;

	IField categoryField;

	private final String TAG_FIELD = "categoryField"; //$NON-NLS-1$

	/**
	 * Create a new instance of the receiver wrapping a sorter.
	 * 
	 * @param sorter
	 */
	CategoryComparator(TableComparator sorter) {
		innerSorter = sorter;
	}

	/**
	 * Compare obj1 and obj starting with field depth.
	 * 
	 * @param obj1
	 * @param obj2
	 * @param depth
	 * @param continueSearching
	 * @return int
	 * @see ViewerComparator#compare(Viewer, Object, Object)
	 */
	int compare(Object obj1, Object obj2, int depth, boolean continueSearching) {

		if (obj1 == null || obj2 == null || !(obj1 instanceof MarkerNode)
				|| !(obj2 instanceof MarkerNode)) {
			return 0;
		}

		MarkerNode marker1;
		MarkerNode marker2;

		marker1 = (MarkerNode) obj1;
		marker2 = (MarkerNode) obj2;

		if (categoryField == null) {
			return innerSorter.compare(marker1, marker2, depth,
					continueSearching);
		}

		int result = categoryField.compare(marker1, marker2);
		if (continueSearching && result == 0) {
			return innerSorter.compare(marker1, marker2, 0, continueSearching);
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ViewerSorter#compare(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public int compare(Viewer viewer, Object e1, Object e2) {
		return compare(e1, e2, 0, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {
		return compare(arg0, arg1, 0, true);
	}

	/**
	 * Get the category field.
	 * 
	 * @return IField
	 */
	public IField getCategoryField() {
		return categoryField;
	}

	/**
	 * Set the field that we are categorizing by.
	 * 
	 * @param field
	 */
	public void setCategoryField(IField field) {
		this.categoryField = field;
	}

	/**
	 * Set the inner sorter to the new sorter.
	 * 
	 * @param sorter2
	 */
	public void setTableSorter(TableComparator sorter2) {
		innerSorter = sorter2;

	}

	/**
	 * Save the state of the receiver.
	 * 
	 * @param dialogSettings
	 */
	public void saveState(IDialogSettings dialogSettings) {
		if (dialogSettings == null) {
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TableComparator.TAG_DIALOG_SECTION);
		if (settings == null) {
			settings = dialogSettings
					.addNewSection(TableComparator.TAG_DIALOG_SECTION);
		}

		String description = Util.EMPTY_STRING;
		if (categoryField != null) {
			description = categoryField.getDescription();
		}

		settings.put(TAG_FIELD, description);

	}

	/**
	 * Restore the state of the receiver from the dialog settings.
	 * 
	 * @param dialogSettings
	 * @param view
	 */
	public void restoreState(IDialogSettings dialogSettings, ProblemView view) {
		if (dialogSettings == null) {
			selectDefaultGrouping(view);
			return;
		}

		IDialogSettings settings = dialogSettings
				.getSection(TableComparator.TAG_DIALOG_SECTION);
		if (settings == null) {
			selectDefaultGrouping(view);
			return;
		}

		String description = settings.get(TAG_FIELD);
		view.selectCategory(description, this);
	}

	/**
	 * Select the default grouping in the problem view
	 * @param view
	 */
	private void selectDefaultGrouping(ProblemView view) {
		view.selectCategoryField(MarkerSupportRegistry.getInstance()
				.getDefaultGroupField(), this);
	}
}
