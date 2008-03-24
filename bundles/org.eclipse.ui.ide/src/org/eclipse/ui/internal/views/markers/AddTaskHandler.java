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
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.internal.DialogTaskProperties;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * AddTaskHandler is the handler for adding a new task to the task list.
 * 
 * @since 3.4
 * 
 */
public class AddTaskHandler extends MarkerViewHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) {

		final ExtendedMarkersView view = getView(event);
		if (view == null)
			return this;

		DialogTaskProperties dialog = new DialogTaskProperties(view.getSite()
				.getShell(), MarkerMessages.addGlobalTaskDialog_title);
		dialog.open();
		return this;
	}
}
