/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.ui.internal.editors.text.folding;

import java.util.Map;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import org.eclipse.jface.text.source.projection.ProjectionViewer;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

import org.eclipse.ui.internal.editors.text.TextOperationActionHandler;

/**
 * Turns folding on or off and shows the current state as a check mark.
 */
public class ToggleFoldingHandler extends TextOperationActionHandler implements IElementUpdater {

	public ToggleFoldingHandler() {
		super(ProjectionViewer.TOGGLE);
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object result= super.execute(event);
		IWorkbenchWindow window= HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {
			ICommandService commandService= window.getService(ICommandService.class);
			if (commandService != null) {
				commandService.refreshElements(event.getCommand().getId(), null);
			}
		}
		return result;
	}

	@Override
	public void updateElement(UIElement element, @SuppressWarnings("rawtypes") Map parameters) {
		IWorkbenchWindow window= element.getServiceLocator().getService(IWorkbenchWindow.class);
		ProjectionViewer viewer= window == null || window.getActivePage() == null ? null
				: FoldingPropertyTester.getProjectionViewer(window.getActivePage().getActivePart());
		element.setChecked(viewer != null && viewer.isProjectionMode());
	}
}
