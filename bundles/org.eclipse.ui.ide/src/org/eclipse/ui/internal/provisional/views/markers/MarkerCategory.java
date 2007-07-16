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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

class MarkerCategory extends MarkerItem {


	int start;

	int end;

	private MarkerEntry[] children;

	private String name;

	private CachedMarkerBuilder cachedMarkerBuilder;

	/**
	 * Create a new instance of the receiver that has the markers between
	 * startIndex and endIndex showing.
	 * 
	 * @param cachedMarkerBuilder
	 * @param startIndex
	 * @param endIndex
	 * @param cachedMarkerBuilder the builder used to generate the children lazily.
	 */
	MarkerCategory(CachedMarkerBuilder cachedMarkerBuilder, int startIndex, int endIndex,
			String categoryName) {
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
				return MarkerUtilities.EMPTY_MARKER_ITEM_ARRAY;
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

		return NLS.bind(MarkerMessages.Category_Label, new Object[] {
					name, String.valueOf(getTotalSize()) });
		
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

	
}