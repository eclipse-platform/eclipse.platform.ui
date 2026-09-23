/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
package org.eclipse.ui.internal.navigator.resources.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.ResourceHighlightDecorator;
import org.eclipse.ui.internal.navigator.resources.ResourceHighlightManager;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

/**
 * Handler to enable highlight markers on selected resources.
 */
public class ToggleHighlightHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (!(selection instanceof IStructuredSelection structuredSelection)) {
			return null;
		}

		for (Object obj : structuredSelection.toList()) {
			IResource resource = Adapters.adapt(obj, IResource.class);
			if (resource == null || !resource.isAccessible()) {
				continue;
			}

			boolean alreadyHighlighted = ResourceHighlightManager.isHighlighted(resource);

			if (alreadyHighlighted) {
				runMarkerOp(resource, false, null);
			} else {
				InputDialog dialog = new InputDialog(
							HandlerUtil.getActiveShell(event),
							WorkbenchNavigatorMessages.ToggleHighlightHandler_dialogTitle,
							WorkbenchNavigatorMessages.ToggleHighlightHandler_dialogMessage,
						resource.getName(),
						newText -> null); // any input accepted
				if (dialog.open() == Window.CANCEL) {
					continue;
				}
				String name = dialog.getValue().trim();
				if (name.isEmpty()) {
					name = resource.getName();
				}
				final String markerName = name;
				runMarkerOp(resource, true, markerName);
			}
		}

		IDecoratorManager dm = PlatformUI.getWorkbench().getDecoratorManager();
		dm.update(ResourceHighlightDecorator.ID);
		return null;
	}

	private void runMarkerOp(IResource resource, boolean highlight, String name) {
		IWorkspaceRunnable runnable = monitor -> {
			try {
				ResourceHighlightManager.setHighlighted(resource, highlight, name);
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log("Failed to toggle highlight marker", e.getStatus()); //$NON-NLS-1$
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(runnable, new NullProgressMonitor());
		} catch (CoreException e) {
			WorkbenchNavigatorPlugin.log("Failed to execute highlight operation", e.getStatus()); //$NON-NLS-1$
		}
	}
}
