/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers.internal;

import java.util.ArrayList;
import java.util.Collection;

class MarkerNodeRefreshRecord{
	Collection removedMarkers;
	Collection addedMarkers;
	Collection changedMarkers;
	
	/**
	 * Create a new instance of the receiver with the supplied markers.
	 * @param removed
	 * @param added
	 * @param changed
	 */
	MarkerNodeRefreshRecord(Collection removed, Collection added, Collection changed){
		removedMarkers = new ArrayList(removed);
		addedMarkers =  new ArrayList(added);
		changedMarkers =  new ArrayList(changed);
	}

	/**
	 * Add the node to the list of removals.
	 * @param node
	 */
	public void remove(MarkerNode node) {
		removedMarkers.add(node);
		
	}
	
	/**
	 * Add the node to the list of adds.
	 * @param node
	 */
	public void add(MarkerNode node) {
		addedMarkers.add(node);
		
	}
}