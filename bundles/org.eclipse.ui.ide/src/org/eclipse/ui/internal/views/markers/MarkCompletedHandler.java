/*******************************************************************************
 * Copyright (c) 2007,2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.views.markers;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.undo.UpdateMarkersOperation;
import org.eclipse.ui.internal.ide.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.views.markers.MarkerViewHandler;
import org.eclipse.ui.views.markers.internal.MarkerMessages;

/**
 * MarkCompletedHandler is the handler for marking the current selection as
 * completed.
 *
 * @since 3.4
 *
 */
public class MarkCompletedHandler extends MarkerViewHandler {

	@Override
	public Object execute(ExecutionEvent event) {

		final ExecutionEvent finalEvent = event;
		try {
			PlatformUI.getWorkbench().getProgressService().run(true, true, mon -> {
				SubMonitor subMonitor = SubMonitor.convert(mon, MarkerMessages.markCompletedHandler_task, 100);
				IMarker[] markers = getSelectedMarkers(finalEvent);
				if (markers.length == 0) {
					return;
				}

				Map<String, Boolean> attrs = new HashMap<>();
				attrs.put(IMarker.DONE, Boolean.TRUE);
				IUndoableOperation op = new UpdateMarkersOperation(markers, attrs,
						MarkerMessages.markCompletedAction_title, true);

				subMonitor.worked(20);
				execute(op, MarkerMessages.markCompletedAction_title, subMonitor.split(80), null);
			});
		} catch (InvocationTargetException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.ERROR,
							e.getLocalizedMessage(), e), StatusManager.LOG);
		} catch (InterruptedException e) {
			StatusManager.getManager().handle(
					StatusUtil.newStatus(IStatus.WARNING, e
							.getLocalizedMessage(), e), StatusManager.LOG);
		}
		return this;

	}
}
