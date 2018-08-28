/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.core.internal.resources.MarkerManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

@SuppressWarnings("restriction")
public class NestedProjectsLabelProvider extends ResourceExtensionLabelProvider {

	private CompletableFuture<Map<IContainer, Integer>> severitiesPerContainer = null;
	private IResourceChangeListener refreshSeveritiesOnProblemMarkerChange;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		super.init(aConfig);
		this.severitiesPerContainer = refreshSeverities();
		refreshSeveritiesOnProblemMarkerChange = event -> {
			if (event.getDelta() == null) {
				return;
			}
			MarkerManager markerManager = ((Workspace) WorkbenchNavigatorPlugin.getWorkspace()).getMarkerManager();
			try {
				event.getDelta().accept(delta -> {
					if (severitiesPerContainer != null) {
						IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
						for (IMarkerDelta markerDelta : markerDeltas) {
							if (markerManager.isSubtype(markerDelta.getType(), IMarker.PROBLEM)) {
								severitiesPerContainer.cancel(true);
								severitiesPerContainer = null;
								return false;
							}
						}
					}
					return true;
				});
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			if (severitiesPerContainer == null) {
				this.severitiesPerContainer = refreshSeverities();
			}
		};
		WorkbenchNavigatorPlugin.getWorkspace().addResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
	}

	@Override
	public void dispose() {
		WorkbenchNavigatorPlugin.getWorkspace().removeResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
		super.dispose();
	}

	private CompletableFuture<Map<IContainer, Integer>> refreshSeverities() {
		return CompletableFuture.supplyAsync(() -> {
			Map<IContainer, Integer> severities = new HashMap<>();
			try {
				for (IMarker marker : WorkbenchNavigatorPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE)) {
					int severity = marker.getAttribute(IMarker.SEVERITY, -1);
					IContainer container = marker.getResource().getParent();
					if (marker.getResource() instanceof IContainer) {
						container = (IContainer) marker.getResource();
					}
					while (container != null) {
						if (!severities.containsKey(container) || severities.get(container).intValue() < severity) {
							severities.put(container, Integer.valueOf(severity));
							if (container.getType() == IResource.FOLDER) {
								container = container.getParent();
							} else if (container.getType() == IResource.PROJECT) {
								container = NestedProjectManager.getInstance().getMostDirectOpenContainer((IProject)container);
							} else {
								container = null;
							}
						} else {
							container = null;
						}
					}
				}
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
				throw new RuntimeException(e);
			}
			return severities;
		});
	}

	@Override
	protected String decorateText(String input, Object element) {
		super.decorateText(input, element);
		if (! (element instanceof IProject)) {
			return input;
		}
		IProject project = (IProject)element;
		IPath location = project.getLocation();
		if (location != null && !location.lastSegment().equals(project.getName())) {
			return input + " (in " + location.lastSegment() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		return input;
	}

	@Override
	protected int getHighestProblemSeverity(IResource resource) {
		int problemSeverity = super.getHighestProblemSeverity(resource);
		if (resource instanceof IContainer && problemSeverity < IMarker.SEVERITY_ERROR) {
			try {
				// keep a snapshot to avoid the value to suddenly turn null
				final CompletableFuture<Map<IContainer, Integer>> severitiesSnapshot = severitiesPerContainer;
				if (severitiesSnapshot != null) {
					Integer severity = severitiesSnapshot.get(50, TimeUnit.MILLISECONDS).get(resource);
					if (severity != null) {
						problemSeverity = Math.max(problemSeverity, severity.intValue());
					}
				}
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return problemSeverity;
	}

}
