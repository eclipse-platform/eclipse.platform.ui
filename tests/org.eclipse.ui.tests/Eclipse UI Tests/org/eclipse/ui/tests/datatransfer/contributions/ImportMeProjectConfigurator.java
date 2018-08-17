/*******************************************************************************
 * Copyright (c) 2016 Red Hat Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * - Mickael Istria (Red Hat Inc.)
 *******************************************************************************/
package org.eclipse.ui.tests.datatransfer.contributions;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

public class ImportMeProjectConfigurator implements org.eclipse.ui.wizards.datatransfer.ProjectConfigurator {

	private static final String IMPORTME_FILENAME = "importme";
	public static final Set<IProject> configuredProjects = new HashSet<>();

	@Override
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
		Set<File> res = new HashSet<>();
		Queue<File> queue = new LinkedList<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			File current = queue.poll();
			if (new File(current, IMPORTME_FILENAME).isFile()) {
				res.add(current);
			}
			if (current.isDirectory()) {
				File[] files = current.listFiles();
				if (files != null) {
					queue.addAll(Arrays.asList(files));
				}
			}
		}
		return res;
	}

	@Override
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
		return container.getFile(new Path(IMPORTME_FILENAME)).exists();
	}

	@Override
	public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
		return Collections.emptySet();
	}

	@Override
	public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
		if (shouldBeAnEclipseProject(project, monitor)) {
			return true;
		}
		try {
			for (IResource child : project.members()) {
				boolean ignore = false;
				for (IPath ignoredPath : ignoredPaths) {
					ignore |= ignoredPath.isPrefixOf(child.getLocation());
					if (ignore) {
						continue;
					}
				}
				if (!ignore && child.getType() == IResource.FOLDER && ((IFolder) child).findMember(IMPORTME_FILENAME) != null) {
					return true;
				}
			}
		} catch (CoreException e) {
			// Nothing
		}
		return false;
	}

	@Override
	public void configure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
		configuredProjects.add(project);
	}

}
