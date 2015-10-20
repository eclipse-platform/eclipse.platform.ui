/*******************************************************************************
 * Copyright (c) 2014, 2015 Red Hat Inc., and others.
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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * @since 3.3
 *
 */
public class NestedProjectManager {

	private static NestedProjectManager INSTANCE = new NestedProjectManager();

	private Map<IPath, IProject> locationsToProjects = Collections
			.synchronizedMap(new TreeMap<IPath, IProject>(new PathComparator()));

	private int knownProjectsCount;

	private NestedProjectManager() {
		refreshProjectsList();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener() {
			@Override
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getType() == IResourceChangeEvent.POST_CHANGE
						&& event.getDelta().getResource().getType() == IResource.PROJECT) {
					refreshProjectsList();
				}
			}
		});
	}

	private void refreshProjectsListIfNeeded() {
		if (knownProjectsCount != ResourcesPlugin.getWorkspace().getRoot().getProjects().length) {
			// TODO: find other cheap checks to try in condition
			// Need to find a cheap way to react to project refactoring (moved
			// or renamed...)
			refreshProjectsList();
		}
	}

	private void refreshProjectsList() {
		locationsToProjects.clear();
		IProject[] knownProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		knownProjectsCount = knownProjects.length;
		for (IProject project : knownProjects) {
			IPath location = project.getLocation();
			if (location != null) {
				locationsToProjects.put(location, project);
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
		refreshProjectsListIfNeeded();
		IPath location = folder.getLocation();
		if (location == null) {
			return null;
		}
		IProject res = locationsToProjects.get(location);
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
		while (queriedLocation.segmentCount() > 0) {
			if (locationsToProjects.containsKey(queriedLocation)) {
				return true;
			}
			queriedLocation = queriedLocation.removeLastSegments(1);
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
		while (mostDirectParentProject == null && queriedLocation.segmentCount() > 0) {
			mostDirectParentProject = locationsToProjects.get(queriedLocation);
			if (mostDirectParentProject != null && mostDirectParentProject.getLocation() == null) {
				mostDirectParentProject = null;
			}
			queriedLocation = queriedLocation.removeLastSegments(1);
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

}
