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

import java.util.SortedMap;
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

	private SortedMap<IPath, IProject> locationsToProjects = new TreeMap<IPath, IProject>(new PathComparator());

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
		if (this.locationsToProjects.size() != ResourcesPlugin.getWorkspace().getRoot().getProjects().length) {
			// TODO: find other cheap checks to try in condition
			// Need to find a cheap way to react to project refactoring (moved
			// or renamed...)
			refreshProjectsList();
		}
	}

	private void refreshProjectsList() {
		this.locationsToProjects.clear();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			this.locationsToProjects.put(project.getLocation(), project);
		}
	}

	public static NestedProjectManager getInstance() {
		synchronized (INSTANCE) {
			if (INSTANCE == null) {
				INSTANCE = new NestedProjectManager();
			}
		}
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
		IProject res = this.locationsToProjects.get(folder.getLocation());
		if (res != null && (!res.exists() || !res.getLocation().equals(folder.getLocation()))) {
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
		IPath queriedLocation = project.getLocation().removeLastSegments(1);
		while (queriedLocation.segmentCount() > 0) {
			if (this.locationsToProjects.containsKey(queriedLocation)) {
				return true;
			}
			queriedLocation = queriedLocation.removeLastSegments(1);
		}
		return false;
	}

	public IContainer getMostDirectOpenContainer(IProject project) {
		IProject mostDirectParentProject = null;
		IPath queriedLocation = project.getLocation().removeLastSegments(1);
		while (mostDirectParentProject == null && queriedLocation.segmentCount() > 0) {
			if (this.locationsToProjects.containsKey(queriedLocation)) {
				mostDirectParentProject = this.locationsToProjects.get(queriedLocation);
			}
			queriedLocation = queriedLocation.removeLastSegments(1);
		}
		if (mostDirectParentProject != null) {
			IPath parentContainerAbsolutePath = project.getLocation().removeLastSegments(1);
			if (parentContainerAbsolutePath.equals(mostDirectParentProject.getLocation())) {
				return mostDirectParentProject;
			} else {
				IPath parentFolderPathRelativeToProject = parentContainerAbsolutePath.removeFirstSegments(mostDirectParentProject.getLocation().segmentCount());
				return mostDirectParentProject.getFolder(parentFolderPathRelativeToProject);
			}
		}
		return null;
	}

}
