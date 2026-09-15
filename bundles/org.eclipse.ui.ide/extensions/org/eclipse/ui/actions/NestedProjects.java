/*******************************************************************************
 * Copyright (c) 2026 Lars Vogel and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Lars Vogel <Lars.Vogel@vogella.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

/**
 * Finds projects located inside other projects, such as Maven or Gradle
 * modules below their root.
 */
final class NestedProjects {

	private NestedProjects() {
	}

	/**
	 * Returns the open or closed projects located inside the given ones but not
	 * among them, parents before their children.
	 */
	static List<IProject> below(List<? extends IResource> projects, boolean open) {
		List<IPath> locations = new ArrayList<>(projects.size());
		for (IResource project : projects) {
			IPath location = project.getLocation();
			if (location != null) {
				locations.add(location);
			}
		}
		if (locations.isEmpty()) {
			return Collections.emptyList();
		}
		List<IProject> nestedProjects = new ArrayList<>();
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen() != open || projects.contains(project)) {
				continue;
			}
			IPath location = project.getLocation();
			if (location == null) {
				continue;
			}
			for (IPath selected : locations) {
				if (!selected.equals(location) && selected.isPrefixOf(location)) {
					nestedProjects.add(project);
					break;
				}
			}
		}
		nestedProjects.sort(Comparator.comparingInt(project -> project.getLocation().segmentCount()));
		return nestedProjects;
	}
}
