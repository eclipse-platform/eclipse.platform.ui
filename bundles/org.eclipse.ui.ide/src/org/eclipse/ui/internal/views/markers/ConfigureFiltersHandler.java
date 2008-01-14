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
import org.eclipse.ui.views.markers.MarkerViewHandler;


/**
 * ConfigureFiltersHandler the handler for opening the configure filters
 * dialog.
 * @since 3.4
 *
 */
public class ConfigureFiltersHandler extends MarkerViewHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent arg0)  {
		ExtendedMarkersView view = getView(arg0);
		if(view != null)
			view.openFiltersDialog();
		return this;
	}


}
