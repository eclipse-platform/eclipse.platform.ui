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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.views.markers.internal.MarkerTypesModel;

/**
 * MarkerTypeFieldFilter is the field filter for filtering on types.
 * 
 * @since 3.4
 * 
 */
public class MarkerTypeFieldFilter extends MarkerFieldFilter {

	private MarkerContentGenerator generator;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.internal.provisional.views.markers.MarkerFieldFilter#select(org.eclipse.core.resources.IMarker)
	 */
	public boolean select(IMarker marker) {
		if(generator == null)
			return true;
		try {
			return generator.getMarkerTypes().contains(
					MarkerTypesModel.getInstance().getType(marker.getType()));
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Set the content generator for the receiver.
	 * 
	 * @param contentGenerator
	 */
	void setGenerator(MarkerContentGenerator contentGenerator) {
		// Set the contentGenerator for the receiver.
		generator = contentGenerator;

	}

}
