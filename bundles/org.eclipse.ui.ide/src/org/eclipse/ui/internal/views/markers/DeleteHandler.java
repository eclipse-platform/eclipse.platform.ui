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
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * DeleteHandler is the handler for the deletion of a marker.
 * 
 * @since 3.4
 * 
 */
public class DeleteHandler extends MarkerViewHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {

		final ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;

		final IMarker[] selected = view.getSelectedMarkers();

		for (int i = 0; i < selected.length; i++) {
			try {
				selected[i].delete();
			} catch (CoreException e) {
				MarkerSupportInternalUtilities.handle(e);
				throw new ExecutionException(e.getMessage(),e);
			}
		}
		return this;
	}
}
