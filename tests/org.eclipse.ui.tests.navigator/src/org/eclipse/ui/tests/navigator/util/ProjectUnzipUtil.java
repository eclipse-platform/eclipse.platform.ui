/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.tests.navigator.NavigatorTestsPlugin;

public class ProjectUnzipUtil {

	private IPath zipLocation;
	private String[] projectNames;
	private IPath rootLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	private static final String META_PROJECT_NAME = ".project"; //$NON-NLS-1$

	public ProjectUnzipUtil(IPath aLocalZipFilePath, String[] aProjectNames) {
		zipLocation = getLocalPath(aLocalZipFilePath);
		projectNames = aProjectNames;

	}

	public IPath getLocalPath(IPath zipFilePath) {
		URL url = FileLocator.find(NavigatorTestsPlugin.getDefault().getBundle(), zipFilePath, null);
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Path(url.getPath());
	}

	public boolean createProjects() {
		try {
			expandZip();
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
			buildProjects();
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean reset() {
		try {
			expandZip();
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
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
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(zipLocation.toFile());
		} catch (IOException e1) {
			throw e1;
		}
		try {
			Enumeration entries = zipFile.entries();
			while (entries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) entries.nextElement();
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
						copy(zipFile.getInputStream(entry), new FileOutputStream(aFile));
						if (entry.getTime() > 0)
							aFile.setLastModified(entry.getTime());
					}
				} catch (IOException e) {
					throw e;
				}
				monitor.worked(1);
			}
		} finally {
			zipFile.close();
		}
	}

	private IPath computeLocation(String name) {
		return rootLocation.append(name);
	}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		try {
			int n = in.read(buffer);
			while (n > 0) {
				out.write(buffer, 0, n);
				n = in.read(buffer);
			}
		} finally {
			in.close();
			out.close();
		}
	}

	public void setRootLocation(IPath rootLocation) {
		this.rootLocation = rootLocation;
	}

	private void buildProjects() throws IOException, CoreException {
		for (int i = 0; i < projectNames.length; i++) {

			IWorkspace workspace = ResourcesPlugin.getWorkspace();

			IPath projectPath = new Path("/" + projectNames[i] + "/" + META_PROJECT_NAME); //$NON-NLS-1$//$NON-NLS-2$
			IPath path = rootLocation.append(projectPath);
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectNames[i]);
			IProjectDescription description = workspace.loadProjectDescription(path);
			project.create(description, (getProgessMonitor()));
			project.open(getProgessMonitor());

		}
	}

}
