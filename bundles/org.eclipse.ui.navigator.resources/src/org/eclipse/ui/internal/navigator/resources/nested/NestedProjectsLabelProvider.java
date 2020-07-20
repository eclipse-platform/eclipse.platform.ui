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
	private CompletableFuture<NestedProjectsProblemsModel> refreshModelJob;
	private volatile boolean isDisposed;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		super.init(aConfig);
		model = new NestedProjectsProblemsModel();
		refreshModelJob = refreshSeverities();
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
				refreshModelJob = refreshSeverities();
				refreshModelJob.thenAccept(model -> {
					if (!isDisposed) {
						Object[] toUpdate = model.getResourcesWithModifiedSeverity().toArray();
						LabelProviderChangedEvent evt = new LabelProviderChangedEvent(this, toUpdate);
						PlatformUI.getWorkbench().getDisplay().asyncExec(() -> fireLabelProviderChanged(evt));
					}
				});
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

	private CompletableFuture<NestedProjectsProblemsModel> refreshSeverities() {
		return CompletableFuture.supplyAsync(() -> {
			model.refreshModel();
			return model;
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
			return NLS.bind(WorkbenchNavigatorMessages.NestedProjectLabelProvider_nestedProjectLabel, input,
					location.lastSegment());
		}
		return input;
	}

	@Override
	protected int getHighestProblemSeverity(IResource resource) {
		int problemSeverity = super.getHighestProblemSeverity(resource);
		if (resource instanceof IContainer && problemSeverity < IMarker.SEVERITY_ERROR) {
			// ask the Nested Projects Problem model about whether a child has more sever
			// problem
			try {
				// keep a snapshot to avoid the value to suddenly turn null
				final CompletableFuture<NestedProjectsProblemsModel> problemsModelSnapshot = refreshModelJob;
				if (problemsModelSnapshot != null) {
					problemSeverity = Math.max(problemSeverity,
							problemsModelSnapshot.get(50, TimeUnit.MILLISECONDS)
									.getMaxSeverityIncludingNestedProjects(resource));
				}
			} catch (TimeoutException e) {
				// ignore
			} catch (InterruptedException | ExecutionException | RuntimeException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
		}
		return problemSeverity;
	}

}
