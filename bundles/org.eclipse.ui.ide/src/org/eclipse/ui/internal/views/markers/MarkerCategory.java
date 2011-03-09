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

import org.eclipse.core.resources.IMarker;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.views.markers.MarkerItem;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

class MarkerCategory extends MarkerSupportItem {
	
	boolean refreshing;
	
	int start;

	int end;

	MarkerEntry[] children;

	private String name;

	private int severity = -1;

	private Markers markers;

	/**
	 * Create a new instance of the receiver that has the markers between
	 * startIndex and endIndex showing.
	 * 
	 * @param markers
	 * @param startIndex
	 * @param endIndex
	 *            the builder used to generate the children lazily.
	 */
	MarkerCategory(Markers markers, int startIndex,
			int endIndex, String categoryName) {
		this.markers = markers;
		start = startIndex;
		end = endIndex;
		refreshing=false;
		name = categoryName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerSupportItem#getChildren()
	 */
	MarkerSupportItem[] getChildren() {
		if (children == null) {
			MarkerItem[] allMarkers = markers.getMarkerEntryArray();
			int totalSize = getChildrenCount();
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
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerSupportItem#getChildrenCount
	 * ()
	 */
	int getChildrenCount() {
		return end - start + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.internal.views.markers.MarkerSupportItem#getDescription()
	 */
	String getDescription() {
		//see Bug 294959
		//if(refreshing){
		//	//see Bug 294959
		//	return NLS.bind(MarkerMessages.Category_building,
		//			new Object[] { getName() });
		//}
		int size = getChildrenCount();
		MarkerContentGenerator generator = markers.getBuilder().getGenerator();
		boolean limitsEnabled = generator.isMarkerLimitsEnabled();
		int limit = generator.getMarkerLimits();

		if (limitsEnabled && size > limit) {
			return NLS.bind(MarkerMessages.Category_Limit_Label, new Object[] {
					name,
					String.valueOf(limit),
					String.valueOf(getChildrenCount()) });
		}
		if (size == 1)
			return NLS.bind(MarkerMessages.Category_One_Item_Label,
					new Object[] { name });

		return NLS.bind(MarkerMessages.Category_Label, new Object[] { name,
				String.valueOf(size) });

	}

	/**
	 * Get the highest severity in the receiver.
	 * 
	 * @return int
	 */
	int getHighestSeverity() {
		if (severity >= 0)
			return severity;
		severity = 0;// Reset to info
		MarkerSupportItem[] contents = getChildren();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].isConcrete()) {
				int elementSeverity = contents[i].getAttributeValue(
						IMarker.SEVERITY, -1);
				if (elementSeverity > severity)
					severity = elementSeverity;
				if (severity == IMarker.SEVERITY_ERROR)// As bad as it gets
					return severity;
			}
		}
		return severity;
	}

	/**
	 * Return the name of the receiver.
	 * 
	 * @return String
	 */
	String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#getParent()
	 */
	MarkerSupportItem getParent() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.views.markers.MarkerSupportItem#isConcrete()
	 */
	boolean isConcrete() {
		return false;
	}

	/**
	 * Clear the cached values for performance reasons.
	 */
	void clearCache() {
		MarkerSupportItem[] entries = getChildren();
		for (int i = 0; i < entries.length; i++) {
			entries[i].clearCache();
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((markers == null) ? 0 : markers.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarkerCategory other = (MarkerCategory) obj;
		if (markers == null) {
			if (other.markers != null)
				return false;
		} else if (!markers.equals(other.markers))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
