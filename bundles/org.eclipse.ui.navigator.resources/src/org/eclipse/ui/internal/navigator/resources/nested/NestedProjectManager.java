/*******************************************************************************
 * Copyright (c) 2014, 2016 Red Hat Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *     Ivica Loncar - Projects open from inside parent inherit working sets
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @since 3.3
 *
 */
public class NestedProjectManager {

	private static NestedProjectManager INSTANCE = new NestedProjectManager();

	/**
	 * This structure sorts project by location, so we can assume that:
	 * <ul>
	 * <li>If a project is nested under another, then the parent project is the
	 * previous item in the map. So getting the parent is just about checking
	 * the previous project for parency.</li>
	 * <li>the children project of a project (with any depth not only direct
	 * ones) are the immediately following items in the map.</li>
	 * </ul>
	 */
	private SortedMap<IPath, IProject> locationsToProjects = new TreeMap<>(new PathComparator());

	private NestedProjectManager() {
		refreshProjectsList();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				IResourceDelta delta = event.getDelta();
				IResource resource = null;
				if (delta != null) {
					resource = delta.getResource();
				}
				if (resource != null
						&& (resource.getType() == IResource.PROJECT || resource.getType() == IResource.ROOT)) {
					refreshProjectsList();
				}
			}
		}, IResourceChangeEvent.POST_CHANGE);
	}

	private void refreshProjectsList() {
		IProject[] knownProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		synchronized (locationsToProjects) {
			locationsToProjects.clear();
			for (IProject project : knownProjects) {
				IPath location = project.getLocation();
				if (location != null) {
					locationsToProjects.put(location, project);
				}
			}
		}
	}

	public static NestedProjectManager getInstance() {
		return INSTANCE;
	}

	/**
	 * @param folder a folder to decide about
	 * @return an {@link IProject} that or {@code null}
	 */
	public IProject getProject(IFolder folder) {
		if (folder == null) {
			return null;
		}
		IPath location = folder.getLocation();
		if (location == null) {
			return null;
		}
		IProject res;
		synchronized (locationsToProjects) {
			res = locationsToProjects.get(location);
		}
		if (res != null && (!res.exists() || !location.equals(res.getLocation()))) {
			// project was deleted and state not refreshed
			refreshProjectsList();
			return getProject(folder);
		}
		return res;
	}

	/**
	 * A shorthand for {@code getProject(folder) != null}.
	 *
	 * @param folder
	 * @return {@code true} if project having the same location as {@code folder} exists and nested is enabled, {@code false} otherwise
	 */
	public boolean isShownAsProject(IFolder folder) {
		return getProject(folder) != null;
	}

	public boolean isShownAsNested(IProject project) {
		if (!project.exists()) {
			return false;
		}
		IPath location = project.getLocation();
		if (location == null) {
			return false;
		}
		IPath queriedLocation = location.removeLastSegments(1);
		synchronized (locationsToProjects) {
			while (queriedLocation.segmentCount() > 0) {
				if (locationsToProjects.containsKey(queriedLocation)) {
					return true;
				}
				queriedLocation = queriedLocation.removeLastSegments(1);
			}
		}
		return false;
	}

	public IContainer getMostDirectOpenContainer(IProject project) {
		IPath location = project.getLocation();
		if (location == null) {
			return null;
		}
		IProject mostDirectParentProject = null;
		IPath queriedLocation = location.removeLastSegments(1);
		synchronized (locationsToProjects) {
			while (mostDirectParentProject == null && queriedLocation.segmentCount() > 0) {
				mostDirectParentProject = locationsToProjects.get(queriedLocation);
				if (mostDirectParentProject != null && mostDirectParentProject.getLocation() == null) {
					mostDirectParentProject = null;
				}
				queriedLocation = queriedLocation.removeLastSegments(1);
			}
		}
		if (mostDirectParentProject != null) {
			IPath parentContainerAbsolutePath = location.removeLastSegments(1);
			IPath location2 = mostDirectParentProject.getLocation();
			if (location2 == null) {
				return null;
			}
			if (parentContainerAbsolutePath.equals(location2)) {
				return mostDirectParentProject;
			}
			IPath parentFolderPathRelativeToProject = parentContainerAbsolutePath
					.removeFirstSegments(location2.segmentCount());
			return mostDirectParentProject.getFolder(parentFolderPathRelativeToProject);
		}
		return null;
	}

	/**
	 * @param container
	 *            a container to ask for nested projects
	 * @return the direct children projects for given container
	 */
	public IProject[] getDirectChildrenProjects(IContainer container) {
		if (container instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) container;
			return root.getProjects();
		}
		Set<IProject> res = new HashSet<>();
		IPath containerLocation = container.getLocation();
		IPath projectLocation = container.getProject().getLocation();
		if (containerLocation == null || projectLocation == null) {
			return res.toArray(new IProject[res.size()]);
		}
		synchronized (locationsToProjects) {
			for (Entry<IPath, IProject> entry : locationsToProjects.tailMap(containerLocation).entrySet()) {
				if (entry.getValue().equals(container.getProject())) {
					// ignore current project
				} else if (containerLocation.isPrefixOf(entry.getKey())) {
					if (entry.getKey().segmentCount() == containerLocation.segmentCount() + 1) {
						res.add(entry.getValue());
					}
				} else { // moved to another branch, not worth continuing
					break;
				}
			}
		}
		return res.toArray(new IProject[res.size()]);
	}

	/**
	 * @param container
	 * @return whether the container has some projects as direct children
	 */
	public boolean hasDirectChildrenProjects(IContainer container) {
		if (container instanceof IWorkspaceRoot) {
			IWorkspaceRoot root = (IWorkspaceRoot) container;
			return root.getProjects().length > 0;
		}
		IPath containerLocation = container.getLocation();
		IPath projectLocation = container.getProject().getLocation();
		if (containerLocation == null || projectLocation == null) {
			return false;
		}
		synchronized (locationsToProjects) {
			for (Entry<IPath, IProject> entry : locationsToProjects.tailMap(containerLocation).entrySet()) {
				if (entry.getValue().equals(container.getProject())) {
					// ignore current project
				} else if (containerLocation.isPrefixOf(entry.getKey())) {
					if (entry.getKey().segmentCount() == containerLocation.segmentCount() + 1) {
						return true;
					}
				} else { // moved to another branch, not worth continuing
					break;
				}
			}
		}
		return false;
	}
}
