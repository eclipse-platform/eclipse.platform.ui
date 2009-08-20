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

package org.eclipse.ui.internal.views.markers;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;

/**
 * TasksView is the ide view for showing tasks.
 * @since 3.4
 *
 */
public class TasksView extends MarkerSupportView {

	/**
	 * Create a new instance of the receiver.
	 */
	public TasksView() {
		super(MarkerSupportRegistry.TASKS_GENERATOR);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.ExtendedMarkersView#getStaticContextId()
	 */
	String getStaticContextId() {
		return PlatformUI.PLUGIN_ID + ".task_list_view_context"; //$NON-NLS-1$
	}

}
