/*******************************************************************************
 * Copyright (c) 2019 Red Hat Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CancellationException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;

/**
 * This class stores an updatable model of the problems in the workspace,
 * honoring nested projects so
 * {@link #getMaxSeverityIncludingNestedProjects(IResource)} will return the
 * severity to display when showing nested projects (that is the max severity of
 * problems on resource and all children, including from nested projects).
 */
public class NestedProjectsProblemsModel {

	private boolean neverRan = true;
	private final Set<IResource> dirty = new LinkedHashSet<>();
	private final Map<IResource, Integer> cache = new HashMap<>();
	private final Set<IResource> modifiedSeveritySinceLastRun = new LinkedHashSet<>();

	public void refreshModel() {
		modifiedSeveritySinceLastRun.clear();
		Set<IResource> localDirty;
		synchronized (dirty) {
			localDirty = new LinkedHashSet<>(dirty);
			dirty.clear();
		}
		removeFromCache(localDirty);
		modifiedSeveritySinceLastRun.addAll(localDirty);

		try {
			for (IMarker marker : WorkbenchNavigatorPlugin.getWorkspace().getRoot().findMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE)) {
				IResource resource = marker.getResource();
				if (resource != null && (neverRan || localDirty.contains(resource))) {
					int severity = marker.getAttribute(IMarker.SEVERITY, -1);
					if (severity >= 0) {
						propagateSeverityToCache(resource, severity);
					}
				}
			}
			neverRan = false;
		} catch (CoreException e) {
			WorkbenchNavigatorPlugin.log(e.getMessage(),
					new Status(IStatus.ERROR, WorkbenchNavigatorPlugin.PLUGIN_ID, e.getMessage(), e));
			throw new RuntimeException(e);
		} catch (CancellationException e) {
			// ignore
		}
	}

	/**
	 * Removes element from the cache and then fix the parent hierarchy for removed
	 * elements (remove from cache if no problem remain under node, or re-compute
	 * and propagate new highest severity)
	 *
	 * @param toRemove
	 */
	private void removeFromCache(Set<IResource> toRemove) {
		Set<IContainer> dirtyLeafContainers = new LinkedHashSet<>();
		for (IResource resource : toRemove) {
			final IContainer currentContainer = resource instanceof IContainer ? (IContainer) resource
					: resource.getParent();
			if (currentContainer == null) {
				continue;
			}
			IPath currentLocation = currentContainer.getLocation();
			if (currentLocation == null) {
				continue;
			}
			dirtyLeafContainers
					.removeIf(leafContainer -> {
						IPath leafLocation = leafContainer.getLocation();
						return leafLocation != null && leafLocation.isPrefixOf(currentLocation);
					});
			if (dirtyLeafContainers.stream().noneMatch(
					leafContainer -> {
						IPath leafLocation = leafContainer.getLocation();
						return leafLocation != null && currentLocation.isPrefixOf(leafLocation);
					})) {
				dirtyLeafContainers.add(currentContainer);
			}
			if (resource.getType() == IResource.FILE) {
				cache.remove(resource);
				modifiedSeveritySinceLastRun.add(resource);
			}
			IContainer container = currentContainer;
			while (container != null && cache.containsKey(container)) {
				cache.remove(container);
				modifiedSeveritySinceLastRun.add(container);
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
		Set<IResource> children = new LinkedHashSet<>();
		try {
			children.addAll(Arrays.asList(container.members()));
		} catch (CoreException ex) {
			WorkbenchNavigatorPlugin.log("Cannot access members", //$NON-NLS-1$
					WorkbenchNavigatorPlugin.createErrorStatus(ex.getMessage(), ex));
		}
		children.addAll(Arrays.asList(NestedProjectManager.getInstance().getDirectChildrenProjects(container)));
		int severity = -1;
		for (IResource child : children) {
			Integer cachedSeverity = cache.get(child);
			if (cachedSeverity != null && cachedSeverity.intValue() > severity) {
				severity = cachedSeverity.intValue();
				if (severity >= IMarker.SEVERITY_ERROR) {
					return severity;
				}
			}
		}
		return severity;
	}

	private void propagateSeverityToCache(IResource resource, int severity) {
		while (resource != null) {
			Integer cachedSeverity = cache.get(resource);
			if (cachedSeverity == null || cachedSeverity.intValue() < severity) {
				cache.put(resource, Integer.valueOf(severity));
				modifiedSeveritySinceLastRun.add(resource);
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

	public void markDirty(IResource resource) {
		synchronized (dirty) {
			dirty.add(resource);
		}
	}

	public Collection<IResource> getResourcesWithModifiedSeverity() {
		return Collections.unmodifiableSet(modifiedSeveritySinceLastRun);
	}

	/**
	 *
	 * @param resource
	 * @return the severity of problem to display on the resource. It is the maximum
	 *         severity of all children resources, including those from nested
	 *         projects. In case no problem is found for the resource and its
	 *         descendants, return <code>-1</code>
	 */
	public Integer getMaxSeverityIncludingNestedProjects(IResource resource) {
		return cache.getOrDefault(resource, -1);
	}

	public boolean isDirty() {
		return neverRan || !this.dirty.isEmpty();
	}
}
