/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;

/**
 * The MarkerSupportView is a view that supports the extensions
 * in the markerSupport extension point.
 * @since 3.4
 *
 */
public abstract class MarkerSupportView extends ExtendedMarkersView {

	
	/**
	 * Create a new instance of the receiver on contentGeneratorId.
	 * @param contentGeneratorId the id of a markerContentGenerator
	 * 	defined in an extension of the markerSupport extension.
	 */
	public MarkerSupportView(String contentGeneratorId) {
		super(contentGeneratorId);
	}

}
