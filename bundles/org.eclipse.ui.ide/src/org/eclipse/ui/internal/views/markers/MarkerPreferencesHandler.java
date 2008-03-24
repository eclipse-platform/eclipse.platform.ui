/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.views.markers.MarkerViewHandler;

/**
 * MarkerPreferencesHandler is the handler for opening the marker preferences dialog.
 * @since 3.4
 *
 */
public class MarkerPreferencesHandler extends MarkerViewHandler implements
		IHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {
		
		ExtendedMarkersView view = getView(event);
		if(view == null)
			return this;
		
		(new MarkerPreferencesDialog(view.getSite().getShell())).open();
		return this;
	}

}
