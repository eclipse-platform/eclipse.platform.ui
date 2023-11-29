package org.eclipse.ui.internal.views.markers;
/*******************************************************************************
 * Copyright (c) 2008, 2015 IBM Corporation and others.
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
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.views.markers.MarkerSupportView;
import org.eclipse.ui.views.markers.internal.MarkerSupportRegistry;


/**
 * AllMarkersView is the view that shows all markers.
 * @since 3.4
 */
public class AllMarkersView extends MarkerSupportView {

	/**
	 * Create a new instance of the receiver.
	 */
	public AllMarkersView() {
		super(MarkerSupportRegistry.ALL_MARKERS_GENERATOR);
	}

	@Override
	protected IUndoContext getUndoContext() {
		ObjectUndoContext context= new ObjectUndoContext(new Object(), "All Markers Context"); //$NON-NLS-1$
		context.addMatch(WorkspaceUndoUtil.getBookmarksUndoContext());
		context.addMatch(WorkspaceUndoUtil.getTasksUndoContext());
		context.addMatch(WorkspaceUndoUtil.getProblemsUndoContext());
		return context;
	}

}
