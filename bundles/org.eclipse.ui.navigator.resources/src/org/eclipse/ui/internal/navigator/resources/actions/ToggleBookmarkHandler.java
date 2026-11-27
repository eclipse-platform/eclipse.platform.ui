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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarks;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

/**
 * Handler to enable toggle markers on selected resources.
 */
public class ToggleBookmarkHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getActiveMenuSelection(event);
		if (selection == null) {
			selection = HandlerUtil.getCurrentSelection(event);
		}
		if (!(selection instanceof IStructuredSelection structuredSelection)) {
			return null;
		}

		List<IResource> toAdd = new ArrayList<>();
		List<IResource> toRemove = new ArrayList<>();
		for (Object obj : structuredSelection.toList()) {
			IResource resource = Adapters.adapt(obj, IResource.class);
			if (resource == null || !resource.isAccessible()) {
				continue;
			}
			if (ResourceBookmarks.isBookmarked(resource)) {
				toRemove.add(resource);
			} else {
				toAdd.add(resource);
			}
		}

		SetMarkerOperation operation = new SetMarkerOperation(toAdd, toRemove);
		if (!operation.isEmpty()) {
			run(operation);
		}
		return null;
	}

	private void run(SetMarkerOperation operation) {
		try {
			ResourcesPlugin.getWorkspace().run(operation, operation.getRule(), IWorkspace.AVOID_UPDATE,
					new NullProgressMonitor());
		} catch (CoreException e) {
			WorkbenchNavigatorPlugin.log("Failed to toggle bookmark marker", e.getStatus()); //$NON-NLS-1$
		}
	}
}
