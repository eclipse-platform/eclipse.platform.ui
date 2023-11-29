/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.internal.DialogTaskProperties;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * AddTaskHandler is the handler for adding a new task to the task list.
 *
 * @since 3.4
 */
public class AddTaskHandler extends MarkerViewHandler {

	@Override
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
