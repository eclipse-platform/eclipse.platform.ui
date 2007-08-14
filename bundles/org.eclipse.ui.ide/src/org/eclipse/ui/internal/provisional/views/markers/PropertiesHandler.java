package org.eclipse.ui.internal.provisional.views.markers;

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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.bookmarkexplorer.BookmarkPropertiesDialog;
import org.eclipse.ui.views.markers.internal.DialogMarkerProperties;
import org.eclipse.ui.views.markers.internal.DialogProblemProperties;
import org.eclipse.ui.views.markers.internal.DialogTaskProperties;

/**
 * The PropertiesHandler is the handler for opening a properties dialog on a
 * selected marker.
 * 
 * @since 3.4
 * 
 */
public class PropertiesHandler extends MarkerViewHandler implements IHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;
		// Assume there is only one item selected.
		IMarker selection = view.getSelectedMarkers()[0];
		DialogMarkerProperties dialog = null;
		try {
			if (selection.isSubtypeOf(IMarker.PROBLEM))
				dialog = new DialogProblemProperties(view.getSite().getShell());
			if (selection.isSubtypeOf(IMarker.TASK))
				dialog = new DialogTaskProperties(view.getSite().getShell());
			if (selection.isSubtypeOf(IMarker.BOOKMARK))
				dialog = new BookmarkPropertiesDialog(view.getSite().getShell());
		} catch (CoreException exception) {
			StatusManager.getManager().handle(exception.getStatus());
		}
		if (dialog == null)
			return this;

		dialog.setMarker(selection);
		dialog.open();
		return this;
	}
}
