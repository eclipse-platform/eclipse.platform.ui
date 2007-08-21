/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.provisional.views.markers;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerFieldFilter;
import org.eclipse.ui.views.markers.internal.MarkerType;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * MarkerTypeFieldFilter is the field filter for filtering on types.
 * 
 * @since 3.4
 * 
 */
public class MarkerTypeFieldFilter extends MarkerFieldFilter {

	private static final String TAG_TYPES_DELIMITER = ":"; //$NON-NLS-1$
	private static final String TAG_SELECTED_TYPES = "selectedTypes"; //$NON-NLS-1$
	Collection selectedTypes;
	private HashMap allTypes;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#select(org.eclipse.core.resources.IMarker)
	 */
	public boolean select(IMarker marker) {

		try {
			return selectedTypes.contains(MarkerTypesModel.getInstance()
					.getType(marker.getType()));
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Set the selected types in the receiver.
	 * 
	 * @param markerTypes
	 *            Collection of MarkerType
	 */
	public void setSelectedTypes(Collection markerTypes) {
		selectedTypes = markerTypes;

	}

	/**
	 * Return the selectedTypes.
	 * 
	 * @return Collection of MarkerType
	 */
	public Collection getSelectedTypes() {
		return selectedTypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#saveSettings(org.eclipse.ui.IMemento)
	 */
	public void saveSettings(IMemento memento) {

		Iterator selected = selectedTypes.iterator();

		StringBuffer settings = new StringBuffer();
		while (selected.hasNext()) {
			MarkerType markerType = (MarkerType) selected.next();
			settings.append(markerType.getId());
			settings.append(TAG_TYPES_DELIMITER);
		}

		memento.putString(TAG_SELECTED_TYPES, settings.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#loadSettings(org.eclipse.ui.IMemento)
	 */
	public void loadSettings(IMemento memento) {

		String types = memento.getString(TAG_SELECTED_TYPES);
		selectedTypes.clear();

		int start = 0;
		int nextSpace = types.indexOf(TAG_TYPES_DELIMITER, 0);
		while (nextSpace > 0) {
			String typeId = types.substring(start, nextSpace);
			start = nextSpace + 1;

			if (allTypes.containsKey(typeId))
				selectedTypes.add(allTypes.get(typeId));
		}

	}

	/**
	 * Set the set of all types to markerTypes. Select all of them by default.
	 * 
	 * @param markerTypes
	 */
	public void setAndSelectAllTypes(Collection markerTypes) {
		allTypes = new HashMap();
		selectedTypes = markerTypes;
		Iterator allIterator = markerTypes.iterator();
		while (allIterator.hasNext()) {
			MarkerType next = (MarkerType) allIterator.next();
			allTypes.put(next.getId(), next);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#populateWorkingCopy(org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter)
	 */
	public void populateWorkingCopy(MarkerFieldFilter copy) {
		super.populateWorkingCopy(copy);
		((MarkerTypeFieldFilter) copy).selectedTypes = new HashSet(
				selectedTypes);
	}
}
