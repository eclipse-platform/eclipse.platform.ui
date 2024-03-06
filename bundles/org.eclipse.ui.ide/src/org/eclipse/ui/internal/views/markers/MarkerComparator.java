/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ui.internal.views.markers;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.ui.IMemento;
import org.eclipse.ui.views.markers.MarkerField;
import org.eclipse.ui.views.markers.MarkerItem;

/**
 * The MarkerComparator is the class that handles the comparison of markers for
 * a specific content provider.
 *
 * @since 3.4
 */
class MarkerComparator implements Comparator<MarkerItem> {

	private MarkerField category;

	// These fields are in sort order
	private MarkerField[] fields;
	/**
	 * Constant to indicate an ascending sort direction.
	 */
	public static final int ASCENDING = 1;
	/**
	 * Constant to indicate an descending sort direction.
	 */
	public static final int DESCENDING = -1;
	private static final String PRIMARY_SORT_FIELD_TAG = "PRIMARY_SORT_FIELD"; //$NON-NLS-1$

	private static final String DESCENDING_FIELDS = "DESCENDING_FIELDS"; //$NON-NLS-1$

	// The fields with reversed direction
	HashSet<MarkerField> descendingFields = new HashSet<>();

	/**
	 * Create a new instance of the receiver categorised by categoryField
	 *
	 * @param categoryField
	 *            May be <code>null</code>
	 * @param mainFields in order of compare significance
	 */
	public MarkerComparator(MarkerField categoryField, MarkerField[] mainFields) {
		category = categoryField;
		fields = mainFields;
	}

	/**
	 * Compare the two objects to see if they have the same category value
	 *
	 * @return int
	 * @see Comparable#compareTo(Object)
	 */
	public int compareCategory(MarkerItem object1, MarkerItem object2) {
		if (category == null) {
			return 0;
		}
		return category.compare(object1, object2);
	}

	/**
	 * Comparator to compare the two MarkerEntry(s) to see if they have the same
	 * category value
	 *
	 * @return Comparator
	 */
	Comparator<MarkerItem> getCategoryComparator() {
		return this::compareCategory;
	}

	@Override
	public int compare(MarkerItem item1, MarkerItem item2) {
		// Sort by category first
		int value = compareCategory(item1, item2);
		if (value != 0) {
			return value;
		}
		return compareFields(item1, item2);
	}

	/**
	 * Compare the two objects by various fields
	 *
	 * @return int
	 */
	public int compareFields(MarkerItem item0, MarkerItem item1) {
		int value = 0;
		for (MarkerField field : fields) {
			value = field.compare(item0, item1);
			if (value != 0) {
				if (descendingFields.contains(field)) {
					value = -value;
				}
				break;
			}
		}
		return value;
	}
	/**
	 * Comparator to compare the two MarkerEntry(s) by various fields
	 *
	 * @return Comparator
	 */
	Comparator<MarkerItem> getFieldsComparator() {
		return this::compareFields;
	}

	/**
	 * Switch the priority of the field from ascending to descending or vice
	 * versa.
	 */
	public void reversePriority(MarkerField field) {
		if (descendingFields.remove(field)) {
			return;
		}
		descendingFields.add(field);
	}

	/**
	 * Set field to be the first sort field.
	 */
	void setPrimarySortField(MarkerField field) {
		if (fields[0] == field) {
			reversePriority(field);
			return;
		}
		int insertionIndex = 1;
		MarkerField[] newFields = new MarkerField[fields.length];

		newFields[0] = field;
		for (int i = 0; i < newFields.length; i++) {
			if (fields[i] == field) {
				continue;
			}
			newFields[insertionIndex] = fields[i];
			insertionIndex++;
		}
		fields = newFields;
	}

	/**
	 * Restore the receiver's state from memento.
	 */
	void restore(IMemento memento) {
		if (memento == null) {
			return;
		}

		String primaryField = memento.getString(PRIMARY_SORT_FIELD_TAG);
		if (primaryField == null || primaryField.equals(MarkerSupportInternalUtilities.getId(fields[0]))) {
			return;
		}
		for (int i = 1; i < fields.length; i++) {
			if (MarkerSupportInternalUtilities.getId(fields[i]).equals(primaryField)) {
				setPrimarySortField(fields[i]);
				break;
			}
		}
		IMemento[] descending = memento.getChildren(DESCENDING_FIELDS);

		for (MarkerField field : fields) {
			for (IMemento currentMemento : descending) {
				if (currentMemento.getID().equals(MarkerSupportInternalUtilities.getId(field))) {
					descendingFields.add(field);
					continue;
				}
			}
		}
	}

	/**
	 * Save the current sort field in the memento.
	 */
	void saveState(IMemento memento) {
		memento.putString(PRIMARY_SORT_FIELD_TAG, MarkerSupportInternalUtilities.getId(fields[0]));
		Iterator<MarkerField> descendingIterator = descendingFields.iterator();
		while (descendingIterator.hasNext()) {
			memento.createChild(DESCENDING_FIELDS, (MarkerSupportInternalUtilities.getId(descendingIterator.next())));
		}
	}

	/**
	 * Get the field that is the main sort field
	 *
	 * @return MarkerField
	 */
	MarkerField getPrimarySortField() {
		return fields[0];
	}

	/**
	 * Set the category field without changing other sort orders.
	 * @param category or <code>null</code>
	 */
	void setCategory(MarkerField category) {
		this.category = category;
	}

	/**
	 * @return Returns the fields.
	 */
	public MarkerField[] getFields() {
		return fields;
	}

	/**
	 * @return Returns the category.
	 */
	public MarkerField getCategory() {
		return category;
	}

}
