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

package org.eclipse.ui.provisional.views.markers;

import org.eclipse.core.resources.IMarker;

/**
 * The MarkerMap is a helper class that manages the mapping between a set of
 * {@link IMarker} and thier {@link MarkerEntry} wrappers.
 * @since 3.4
 *
 */
class MarkerMap {

	static final MarkerMap EMPTY_MAP = new MarkerMap();
	private MarkerEntry[] markers;
	
	/**
	 * Creates an initially empty marker map
	 */
	public MarkerMap() {
		this(new MarkerEntry[0]);
	}

	/**
	 * Create an instance of the receiver from markers.
	 * @param markers
	 */
	
	public MarkerMap(MarkerEntry[] markers) {
		this.markers = markers;
	}

	/**
	 * Get the size of the entries
	 * @return
	 */
	public int getSize() {
		return markers.length;
	}

	/**
	 * Return the entries as an array.
	 * @return MarkerEntry[]
	 */
	public MarkerEntry[] toArray() {
		return markers;
	}


	/**
	 * Return the entry at index
	 * @param index
	 * @return MarkerEntry
	 */
	public MarkerEntry elementAt(int index) {
		return markers[index];
	}

}
