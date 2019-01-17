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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorMessages;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.internal.navigator.resources.workbench.ResourceExtensionLabelProvider;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;

@SuppressWarnings("restriction")
public class NestedProjectsLabelProvider extends ResourceExtensionLabelProvider {

	private IResourceChangeListener refreshSeveritiesOnProblemMarkerChange;
	private ProblemsModelSupplier supplier;
	private CompletableFuture<Map<IResource, Integer>> severities = null;

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
		super.init(aConfig);
		supplier = new ProblemsModelSupplier();
		this.severities = refreshSeverities(null);
		refreshSeveritiesOnProblemMarkerChange = event -> {
			if (event.getDelta() == null) {
				return;
			}
			Set<IResource> dirtyResources = new HashSet<>();
			MarkerManager markerManager = ((Workspace) WorkbenchNavigatorPlugin.getWorkspace()).getMarkerManager();
			try {
				event.getDelta().accept(delta -> {
					IMarkerDelta[] markerDeltas = delta.getMarkerDeltas();
					for (IMarkerDelta markerDelta : markerDeltas) {
						if (markerManager.isSubtype(markerDelta.getType(), IMarker.PROBLEM)) {
							if (severities != null && !severities.isDone()) {
								try {
									severities.cancel(true);
								} catch (CancellationException ex) {
									// expected exception
								}
								severities = null;
								return false;
							}
							dirtyResources.add(markerDelta.getResource());
						}
					}
					return true;
				});
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			}
			if (!dirtyResources.isEmpty()) {
				this.severities = refreshSeverities(dirtyResources);
			}
		};
		WorkbenchNavigatorPlugin.getWorkspace().addResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
	}

	@Override
	public void dispose() {
		WorkbenchNavigatorPlugin.getWorkspace().removeResourceChangeListener(refreshSeveritiesOnProblemMarkerChange);
		super.dispose();
	}

	private CompletableFuture<Map<IResource, Integer>> refreshSeverities(Set<IResource> dirty) {
		if (dirty != null) {
			supplier.markDirty(dirty);
		}
		return CompletableFuture.supplyAsync(supplier);
	}

	private final class ProblemsModelSupplier implements Supplier<Map<IResource, Integer>> {
		private Set<IResource> dirty = null;
		private Map<IResource, Integer> cache = new HashMap<>();

		@Override
		public Map<IResource, Integer> get() {
			Set<IResource> currentDirty = null;
			if (dirty != null) {
				// create a local copy to be thread safe
				currentDirty = new HashSet<>();
				currentDirty.addAll(this.dirty);
				this.dirty.removeAll(currentDirty);
				removeFromCache(currentDirty);
			}

			try {
				for (IMarker marker : WorkbenchNavigatorPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM,
						true, IResource.DEPTH_INFINITE)) {
					IResource resource = marker.getResource();
					if (currentDirty == null || currentDirty.contains(resource)) {
						int severity = marker.getAttribute(IMarker.SEVERITY, 0);
						if (severity >= 0) {
							propagateSeverityToCache(resource, severity);
						}
					}
				}
				// initialize dirty so we won't rebuild model from scratch
				markDirty(Collections.emptySet());
			} catch (CoreException e) {
				WorkbenchNavigatorPlugin.log(e.getMessage(),
						new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
				throw new RuntimeException(e);
			} catch (CancellationException e) {
				// ignore
				return Collections.emptyMap();
			}
			return cache;
		}

		/**
		 * Removes element from the cache and then fix the parent hierarchy for removed
		 * elements (remove from cache if no problem remain under node, or re-compute
		 * and propagate new highest severity)
		 *
		 * @param toRemove
		 */
		private void removeFromCache(Set<IResource> toRemove) {
			Set<IContainer> dirtyLeafContainers = new HashSet<>();
			for (IResource resource : toRemove) {
				final IContainer initialContainer = resource instanceof IContainer ? (IContainer) resource
						: resource.getParent();
				dirtyLeafContainers
						.removeIf(leafContainer -> leafContainer.getLocation()
								.isPrefixOf(initialContainer.getLocation()));
				if (dirtyLeafContainers.stream()
						.noneMatch(leafContainer -> initialContainer.getLocation()
								.isPrefixOf(leafContainer.getLocation()))) {
					dirtyLeafContainers.add(initialContainer);
				}
				if (resource.getType() == IResource.FILE) {
					cache.remove(resource);
				}
				IContainer container = initialContainer;
				while (container != null && cache.containsKey(container)) {
					cache.remove(container);
					container = getParentInView(container);
				}
			}
			dirtyLeafContainers.forEach(leafContainer -> {
				IContainer container = leafContainer;
				while (container != null) {
					int severity = getMaxChildrenSeverityInCache(container);
					if (severity >= 0) {
						propagateSeverityToCache(container, severity);
					}
					container = getParentInView(container);
				}
			});
		}

		private int getMaxChildrenSeverityInCache(IContainer container) {
			if (!container.isAccessible()) {
				return -1;
			}
			Set<IResource> children = new HashSet<>();
			try {
				children.addAll(Arrays.asList(container.members()));
			} catch (CoreException ex) {
				WorkbenchNavigatorPlugin.log("Cannot access members", //$NON-NLS-1$
						WorkbenchNavigatorPlugin.createErrorStatus(ex.getMessage(), ex));
			}
			children.addAll(Arrays.asList(NestedProjectManager.getInstance().getDirectChildrenProjects(container)));
			int[] severity = new int[] { -1 };
			children.forEach(child -> {
				if (cache.containsKey(child) && cache.get(child).intValue() > severity[0]) {
					severity[0] = cache.get(child).intValue();
				}
			});
			return severity[0];
		}

		private void propagateSeverityToCache(IResource resource, int severity) {
			while (resource != null) {
				if (!cache.containsKey(resource) || cache.get(resource).intValue() < severity) {
					cache.put(resource, Integer.valueOf(severity));
					resource = getParentInView(resource);
				} else {
					resource = null;
				}
			}
		}

		private IContainer getParentInView(IResource resource) {
			if (resource.getType() == IResource.PROJECT) {
				return NestedProjectManager.getInstance().getMostDirectOpenContainer((IProject) resource);
			}
			return resource.getParent();
		}

		public void markDirty(Set<IResource> dirty) {
			synchronized (this) {
				if (this.dirty == null) {
					this.dirty = new HashSet<>();
				}
			}
			this.dirty.addAll(dirty);
		}
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
			// TODO check whether container has nested projects (at any depth)
			try {
				// keep a snapshot to avoid the value to suddenly turn null
				final CompletableFuture<Map<IResource, Integer>> severitiesSnapshot = this.severities;
				if (severitiesSnapshot != null) {
					Integer severity = severitiesSnapshot.get(50000000, TimeUnit.MILLISECONDS).get(resource);
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
