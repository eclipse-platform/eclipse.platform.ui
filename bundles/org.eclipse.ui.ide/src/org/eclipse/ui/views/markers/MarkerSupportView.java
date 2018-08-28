/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
