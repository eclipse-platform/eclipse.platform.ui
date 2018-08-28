package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
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
 *******************************************************************************/

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.views.markers.MarkerViewHandler;

/**
 * MarkerGoToHandler is the handler for the go to action.
 * @since 3.4
 *
 */
public class MarkerGoToHandler extends MarkerViewHandler {

	@Override
	public Object execute(ExecutionEvent event)  {
		ExtendedMarkersView view = getView(event);
		if(view == null)
			return this;
		view.openSelectedMarkers();
		return this;
	}



}
