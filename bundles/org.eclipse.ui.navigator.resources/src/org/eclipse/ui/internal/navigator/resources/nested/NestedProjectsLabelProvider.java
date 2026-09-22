/*******************************************************************************
 * Copyright (c) 2014, 2020 Red Hat Inc. and others.
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

import java.util.concurrent.CompletableFuture;

import org.eclipse.core.internal.resources.MarkerManager;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

@SuppressWarnings("restriction")
public class NestedProjectsLabelProvider extends ResourceExtensionLabelProvider {

	private IResourceChangeListener refreshSeveritiesOnProblemMarkerChange;
	private NestedProjectsProblemsModel model;
	private CompletableFuture<Void> refreshModelJob = CompletableFuture.completedFuture(null);
	private volatile boolean isDisposed;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		super.init(aConfig);
		model = new NestedProjectsProblemsModel();
		refreshSeverities();
		refreshSeveritiesOnProblemMarkerChange = event -> {
			if (event.getDelta() == null) {
				return;
			}
			MarkerManager markerManager = ((Workspace) ResourcesPlugin.getWorkspace()).getMarkerManager();
			try {
				event.getDelta().accept(delta -> {
					IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
					for (IMarkerDelta markerDelta : markerDeltas) {
						if (markerManager.isSubtype(markerDelta.getType(), IMarker.PROBLEM)) {
							IResource resource = markerDelta.getResource();
							if (resource != null) {
								model.markDirty(resource);
							}
						}
					}
					return true;
				});
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			if (model.isDirty()) {
				refreshSeverities();
			}
		};
		ResourcesPlugin.getWorkspace().addResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
	}

	@Override
	public void dispose() {
		isDisposed = true;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
		super.dispose();
	}

	// chained so refreshes never mutate the model concurrently
	private synchronized void refreshSeverities() {
		refreshModelJob = refreshModelJob.thenRunAsync(() -> {
			if (isDisposed || !model.isDirty()) {
				return;
			}
			try {
				model.refreshModel();
			} catch (RuntimeException e) {
				// already logged by the model
				return;
			}
			Object[] toUpdate = model.getResourcesWithModifiedSeverity().toArray();
			if (!isDisposed && toUpdate.length > 0) {
				LabelProviderChangedEvent evt = new LabelProviderChangedEvent(this, toUpdate);
				PlatformUI.getWorkbench().getDisplay().asyncExec(() -> fireLabelProviderChanged(evt));
			}
		});
	}

	@Override
	protected String decorateText(String input, Object element) {
		super.decorateText(input, element);
		if (! (element instanceof IProject project)) {
			return input;
		}
		IPath location = project.getLocation();
		if (location == null) {
			return input;
		}
		String lastSegment = location.lastSegment();
		if (lastSegment == null) {
			return input;
		}
		if (!lastSegment.equals(project.getName())) {
			return NLS.bind(WorkbenchNavigatorMessages.NestedProjectLabelProvider_nestedProjectLabel, input,
					lastSegment);
		}
		return input;
	}

	@Override
	protected int getHighestProblemSeverity(IResource resource) {
		int problemSeverity = super.getHighestProblemSeverity(resource);
		if (resource instanceof IContainer && problemSeverity < IMarker.SEVERITY_ERROR) {
			// never wait for a running refresh, it updates the labels when done
			problemSeverity = Math.max(problemSeverity, model.getMaxSeverityIncludingNestedProjects(resource));
		}
		return problemSeverity;
	}

}
