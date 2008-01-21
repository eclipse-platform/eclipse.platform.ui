package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * AllMarkersView is the view that shows all markers.
 * @since 3.4
 *
 */
public class AllMarkersView extends MarkerSupportView {

	/**
	 * Create a new instance of the receiver.
	 */
	public AllMarkersView() {
		super(MarkerSupportRegistry.ALL_MARKERS_GENERATOR);
	}

}
