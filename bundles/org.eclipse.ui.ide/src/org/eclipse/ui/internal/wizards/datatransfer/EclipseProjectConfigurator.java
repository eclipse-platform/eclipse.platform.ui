/*******************************************************************************
 * Copyright (c) 2014-2016 Red Hat Inc., and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.wizards.datatransfer;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.wizards.datatransfer.ProjectConfigurator;

/**
 * A {@link ProjectConfigurator} that detects Eclipse projects (folder with
 * .project)
 *
 * @since 3.12
 *
 */
public class EclipseProjectConfigurator implements ProjectConfigurator {

	@Override
	public Set<File> findConfigurableLocations(File root, IProgressMonitor monitor) {
		Set<File> projectFiles = new LinkedHashSet<>();
		Set<String> visitedDirectories = new HashSet<>();
		WizardProjectsImportPage.collectProjectFilesFromDirectory(projectFiles, root, visitedDirectories, true,
				monitor);
		Set<File> res = new LinkedHashSet<>();
		for (File projectFile : projectFiles) {
			res.add(projectFile.getParentFile());
		}
		return res;
	}

	@Override
	public boolean shouldBeAnEclipseProject(IContainer container, IProgressMonitor monitor) {
		return container.getFile(new Path(IProjectDescription.DESCRIPTION_FILE_NAME)).exists();
	}

	@Override
	public Set<IFolder> getFoldersToIgnore(IProject project, IProgressMonitor monitor) {
		return null;
	}

	@Override
	public boolean canConfigure(IProject project, Set<IPath> ignoredPaths, IProgressMonitor monitor) {
		return true;
	}

	@Override
	public void removeDirtyDirectories(Map<File, List<ProjectConfigurator>> proposals) {
		// nothing to do: we cannot infer that a directory is dirty from
		// .project
	}

	@Override
	public void configure(IProject project, Set<IPath> excludedDirectories, IProgressMonitor monitor) {
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException ex) {
			IDEWorkbenchPlugin.log(ex.getMessage(), ex);
		}
	}

}
