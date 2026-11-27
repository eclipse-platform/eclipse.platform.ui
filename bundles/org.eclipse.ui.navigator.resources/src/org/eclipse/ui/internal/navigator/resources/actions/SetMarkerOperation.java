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

import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.MultiRule;
import org.eclipse.ui.internal.navigator.resources.ResourceBookmarks;

/**
 * Add or remove markers to resources.
 */
public final class SetMarkerOperation implements IWorkspaceRunnable {

	private final List<IResource> toAdd;

	private final List<IResource> toRemove;

	public SetMarkerOperation(List<IResource> toAdd, List<IResource> toRemove) {
		this.toAdd = toAdd;
		this.toRemove = toRemove;
	}

	public boolean isEmpty() {
		return toAdd.isEmpty() && toRemove.isEmpty();
	}

	public ISchedulingRule getRule() {
		ISchedulingRule rule = null;
		for (List<IResource> resources : List.of(toAdd, toRemove)) {
			for (IResource resource : resources) {
				rule = MultiRule.combine(rule, resource.getWorkspace().getRuleFactory().markerRule(resource));
			}
		}
		return rule;
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {
		for (IResource resource : toRemove) {
			for (IMarker marker : resource.findMarkers(ResourceBookmarks.MARKER_TYPE, false, IResource.DEPTH_ZERO)) {
				marker.delete();
			}
		}
		for (IResource resource : toAdd) {
			if (resource.findMarkers(ResourceBookmarks.MARKER_TYPE, false, IResource.DEPTH_ZERO).length == 0) {
				IMarker marker = resource.createMarker(ResourceBookmarks.MARKER_TYPE);
				marker.setAttribute(IMarker.MESSAGE, resource.getName());
			}
		}
	}
}
