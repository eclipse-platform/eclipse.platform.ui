/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class ProjectUnzipUtil {

	private final IPath zipLocation;
	private final String[] projectNames;
	private IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	private static final String META_PROJECT_NAME = ".project"; //$NON-NLS-1$

	public ProjectUnzipUtil(IPath aLocalZipFilePath, String[] aProjectNames) {
		zipLocation = getLocalPath(aLocalZipFilePath);
		projectNames = aProjectNames;

	}

	public IPath getLocalPath(IPath zipFilePath) {
		Bundle bundle = FrameworkUtil.getBundle(ProjectUnzipUtil.class);
		URL url = FileLocator.find(bundle, zipFilePath, null);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return IPath.fromOSString(url.getPath());
	}

	public boolean createProjects() {
		try {
			expandZip();
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			buildProjects();
		} catch (CoreException | IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean reset() {
		try {
			expandZip();
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException | IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private IProgressMonitor getProgessMonitor() {
		return new NullProgressMonitor();
	}

	private void expandZip() throws CoreException, IOException {
		IProgressMonitor monitor = getProgessMonitor();
		try (ZipFile zipFile = new ZipFile(zipLocation.toFile())) {
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = entries.nextElement();
				monitor.subTask(entry.getName());
				File aFile = computeLocation(entry.getName()).toFile();
				File parentFile = null;
				try {
					if (entry.isDirectory()) {
						aFile.mkdirs();
					} else {
						parentFile = aFile.getParentFile();
						if (!parentFile.exists())
							parentFile.mkdirs();
						if (!aFile.exists())
							aFile.createNewFile();
						try (InputStream in = zipFile.getInputStream(entry)) {
							Files.write(aFile.toPath(), in.readAllBytes());
						}
						if (entry.getTime() > 0)
							aFile.setLastModified(entry.getTime());
					}
				} catch (IOException e) {
					throw e;
				}
				monitor.worked(1);
			}
		}
	}

	private IPath computeLocation(String name) {
		return rootLocation.append(name);
	}

	public void setRootLocation(IPath rootLocation) {
		this.rootLocation = rootLocation;
	}

	private void buildProjects() throws IOException, CoreException {
		for (String projectName : projectNames) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath projectPath = IPath.fromOSString("/" + projectName + "/" + META_PROJECT_NAME); //$NON-NLS-1$//$NON-NLS-2$
			IPath path = rootLocation.append(projectPath);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IProjectDescription description = workspace.loadProjectDescription(path);
			project.create(description, (getProgessMonitor()));
			project.open(getProgessMonitor());

		}
	}

}
