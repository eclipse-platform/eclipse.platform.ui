/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerMessages;
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
	 * @see org.eclipse.ui.views.markers.internal.MarkerView#getUndoContext()
	 * @since 3.7
	 */
	protected IUndoContext getUndoContext() {
		return WorkspaceUndoUtil.getTasksUndoContext();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.views.markers.ExtendedMarkersView#getDeleteOperationName(org.eclipse.core.resources.IMarker[])
	 * @since 3.7
	 */
	protected String getDeleteOperationName(IMarker[] markers) {
		Assert.isLegal(markers.length > 0);
		return markers.length == 1 ? MarkerMessages.deleteTaskMarker_operationName : MarkerMessages.deleteTaskMarkers_operationName;
	}

}
