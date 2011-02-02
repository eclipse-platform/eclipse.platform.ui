/*******************************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.views.markers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * MarkerViewHandler is the abstract class of the handlers for the
 * {@link MarkerSupportView}
 * 
 * @since 3.4
 * 
 */
public abstract class MarkerViewHandler extends AbstractHandler {

	private static final IMarker[] EMPTY_MARKER_ARRAY = new IMarker[0];

	/**
	 * Get the view this event occurred on.
	 * 
	 * @param event
	 * @return {@link MarkerSupportView} or <code>null</code>
	 */
	public MarkerSupportView getView(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (!(part instanceof MarkerSupportView))
			return null;
		return (MarkerSupportView) part;
	}

	/**
	 * Execute the specified undoable operation
	 * 
	 * @param operation
	 * @param title
	 * @param monitor
	 * @param uiInfo
	 */
	public void execute(IUndoableOperation operation, String title,
			IProgressMonitor monitor, IAdaptable uiInfo) {
		try {
			PlatformUI.getWorkbench().getOperationSupport()
					.getOperationHistory().execute(operation, monitor, uiInfo);
		} catch (ExecutionException e) {
			if (e.getCause() instanceof CoreException) {
				StatusManager.getManager().handle(
						StatusUtil
								.newStatus(IStatus.ERROR, title, e.getCause()),
						StatusManager.SHOW);

			} else {
				StatusManager.getManager().handle(
						StatusUtil.newStatus(IStatus.ERROR, title, e));
			}
		}
	}

	/**
	 * Get the selected markers for the receiver in the view from event. If the
	 * view cannot be found then return an empty array.
	 * 
	 * This is run using {@link Display#syncExec(Runnable)} so that it can be called 
	 * outside of the UI {@link Thread}.
	 * 
	 * @param event
	 * @return {@link IMarker}[]
	 */
	public IMarker[] getSelectedMarkers(ExecutionEvent event) {
		final MarkerSupportView view = getView(event);
		if (view == null)
			return EMPTY_MARKER_ARRAY;

		final IMarker[][] result = new IMarker[1][];
		view.getSite().getShell().getDisplay().syncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
				result[0] = view.getSelectedMarkers();
			}
		});
		return result[0];
	}
}
