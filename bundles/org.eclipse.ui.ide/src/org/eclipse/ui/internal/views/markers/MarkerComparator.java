/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
 * 
 */
class MarkerComparator implements Comparator {

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
	HashSet descendingFields = new HashSet();

	/**
	 * Create a new instance of the receiver categorised by categoryField
	 * 
	 * @param categoryField
	 *            May be <code>null/<code>
	 * @param mainFields in order of compare significance
	 */
	public MarkerComparator(MarkerField categoryField, MarkerField[] mainFields) {
		category = categoryField;
		fields = mainFields;
	}

	/**
	 * Compare the two objects to see if they have the same category value
	 * 
	 * @param object1
	 * @param object2
	 * @return int
	 * @see Comparable#compareTo(Object)
	 */
	public int compareCategory(Object object1, Object object2) {
		if (category == null)
			return 0;
		return category.compare((MarkerItem) object1, (MarkerItem) object2);
	}
	
	/**
	 * Comparator to compare the two MarkerEntry(s) to see if they have the same
	 * category value
	 * 
	 * @return Comparator
	 */
	Comparator getCategoryComparator(){
		return new Comparator(){
			public int compare(Object o1, Object o2) {
				return compareCategory(o1, o2);
			}			
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {

		// Sort by category first
		int value = compareCategory(arg0, arg1);
		if (value == 0)
			value=compareFields(arg0, arg1);

		return value ;
	}

	/**
	 * Compare the two objects by various fields
	 * 
	 * @param item0
	 * @param item1
	 * @return int
	 */
	public int compareFields(Object item0, Object item1) {
		int value=0;
		for (int i = 0; i < fields.length; i++) {
			if (descendingFields.contains(fields[i])){
				value = fields[i].compare((MarkerItem)item1,(MarkerItem)item0);
			}else{
				value = fields[i].compare((MarkerItem)item0,(MarkerItem)item1);
			}if (value != 0){
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
	Comparator getFieldsComparator(){
		return new Comparator(){
			public int compare(Object o1, Object o2) {
				return compareFields(o1, o2);
			}			
		};
	}

	/**
	 * Switch the priority of the field from ascending to descending or vice
	 * versa.
	 * 
	 * @param field
	 */
	public void reversePriority(MarkerField field) {
		if (descendingFields.remove(field))
			return;
		descendingFields.add(field);

	}

	/**
	 * Set field to be the first sort field.
	 * 
	 * @param field
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
			if (fields[i] == field)
				continue;
			newFields[insertionIndex] = fields[i];
			insertionIndex++;
		}

		fields = newFields;

	}

	/**
	 * Restore the receiver's state from memento.
	 * 
	 * @param memento
	 */
	void restore(IMemento memento) {
		if (memento == null)
			return;

		String primaryField = memento.getString(PRIMARY_SORT_FIELD_TAG);
		if (primaryField == null
				|| primaryField.equals(MarkerSupportInternalUtilities
						.getId(fields[0])))
			return;
		for (int i = 1; i < fields.length; i++) {
			if (MarkerSupportInternalUtilities.getId(fields[i]).equals(
					primaryField)) {
				setPrimarySortField(fields[i]);
				break;
			}
		}
		IMemento[] descending = memento.getChildren(DESCENDING_FIELDS);

		for (int i = 0; i < fields.length; i++) {
			for (int j = 0; j < descending.length; j++) {
				if (descending[j].getID().equals(
						MarkerSupportInternalUtilities.getId(fields[i]))) {
					descendingFields.add(fields[i]);
					continue;
				}

			}
		}

	}

	/**
	 * Save the current sort field in the memento.
	 * 
	 * @param memento
	 */
	void saveState(IMemento memento) {
		memento.putString(PRIMARY_SORT_FIELD_TAG,
				MarkerSupportInternalUtilities.getId(fields[0]));
		Iterator descendingIterator = descendingFields.iterator();
		while (descendingIterator.hasNext()) {
			memento.createChild(DESCENDING_FIELDS,
					(MarkerSupportInternalUtilities
							.getId((MarkerField) descendingIterator.next())));
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

}
