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

package org.eclipse.ui.internal.provisional.views.markers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * MarkerViewHandler is the abstract class of the handlers for the 
 * {@link ExtendedMarkersView}
 * @since 3.4
 *
 */
abstract class MarkerViewHandler extends AbstractHandler {

	/**
	 * Get the view this event occurred on.
	 * @param arg0
	 * @return ExtendedMarkersView or <code>null</code>
	 */
	ExtendedMarkersView getView(ExecutionEvent arg0) {
		IWorkbenchPart part =  HandlerUtil.getActivePart(arg0);
		if(part == null)
			return null;
		return (ExtendedMarkersView) part;
	}
	
	
	/**
	 * Execute the specified undoable operation
	 * @param operation
	 * @param title
	 * @param monitor
	 * @param uiInfo
	 */
	void execute(IUndoableOperation operation, String title,
			IProgressMonitor monitor, IAdaptable uiInfo) {
		try {
			PlatformUI.getWorkbench().getOperationSupport()
					.getOperationHistory().execute(operation, monitor, uiInfo);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				ErrorDialog
						.openError(WorkspaceUndoUtil.getShell(uiInfo), title,
								null, ((CoreException) e.getCause())
										.getStatus());
			} else {
				IDEWorkbenchPlugin.log(title, e);
			}
		}
	}

	
}
