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

import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.provisional.views.markers.api.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

class MarkerCategory extends MarkerItem {

	int start;

	int end;

	private MarkerEntry[] children;

	private String name;

	private CachedMarkerBuilder cachedMarkerBuilder;

	private int severity = -1;

	/**
	 * Create a new instance of the receiver that has the markers between
	 * startIndex and endIndex showing.
	 * 
	 * @param cachedMarkerBuilder
	 * @param startIndex
	 * @param endIndex
	 *            the builder used to generate the children lazily.
	 */
	MarkerCategory(CachedMarkerBuilder cachedMarkerBuilder, int startIndex,
			int endIndex, String categoryName) {
		this.cachedMarkerBuilder = cachedMarkerBuilder;
		start = startIndex;
		end = endIndex;
		name = categoryName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getChildren()
	 */
	public MarkerItem[] getChildren() {

		if (children == null) {

			// Return nothing while a build is going on as this could be
			// stale
			if (this.cachedMarkerBuilder.isBuilding()) {
				return MarkerSupportInternalUtilities.EMPTY_MARKER_ITEM_ARRAY;
			}

			MarkerItem[] allMarkers = cachedMarkerBuilder.getMarkerEntries();

			int totalSize = getTotalSize();
			children = new MarkerEntry[totalSize];

			System.arraycopy(allMarkers, start, children, 0, totalSize);

			for (int i = 0; i < children.length; i++) {
				children[i].setCategory(this);
			}
		}
		return children;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getParent()
	 */
	public MarkerItem getParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getDescription()
	 */
	public String getDescription() {

		int size = getTotalSize();
		int limit = MarkerSupportInternalUtilities.getMarkerLimit();

		if (limit > 0 && size > limit) {
			return NLS.bind(MarkerMessages.Category_Limit_Label,
					new Object[] {
							name,
							String.valueOf(MarkerSupportInternalUtilities
									.getMarkerLimit()),
							String.valueOf(getTotalSize()) });

		}
		if (size == 1)
			return NLS.bind(MarkerMessages.Category_One_Item_Label,
					new Object[] { name });

		return NLS.bind(MarkerMessages.Category_Label, new Object[] { name,
				String.valueOf(size) });

	}

	/**
	 * Get the total size of the receiver.
	 * 
	 * @return int
	 */
	private int getTotalSize() {
		return end - start + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#isConcrete()
	 */
	public boolean isConcrete() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.views.markers.internal.MarkerNode#getConcreteRepresentative()
	 */
	public MarkerEntry getConcreteRepresentative() {
		return cachedMarkerBuilder.getMarkerEntries()[start];
	}

	/**
	 * Return the name of the receiver.
	 * 
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the highest severity in the receiver.
	 * @return int
	 */
	int getHighestSeverity() {
		if(severity  >= 0)
			return severity;
		severity = 0;//Reset to info
		MarkerItem[] contents = getChildren();
		for (int i = 0; i < contents.length; i++) {
			if(contents[i].isConcrete()){
				int elementSeverity = contents[i].getAttributeValue(IMarker.SEVERITY, -1);
				if(elementSeverity > severity)
					severity = elementSeverity;
				if(severity == IMarker.SEVERITY_ERROR)//As bad as it gets
					return severity;
			}			
		}
		return severity;
	}

}