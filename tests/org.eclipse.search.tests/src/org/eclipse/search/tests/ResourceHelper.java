/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
package org.eclipse.search.tests;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * @since 3.0
 */
public class ResourceHelper {

	private final static IProgressMonitor NULL_MONITOR= new NullProgressMonitor();
	private static final int MAX_RETRY= 10;

	public static IProject createProject(String projectName) throws CoreException {

		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject project= root.getProject(projectName);
		if (!project.exists())
			project.create(NULL_MONITOR);
		else
			project.refreshLocal(IResource.DEPTH_INFINITE, null);

		if (!project.isOpen())
			project.open(NULL_MONITOR);

		return project;
	}

	public static void deleteProject(String projectName) throws CoreException {
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		IProject project= root.getProject(projectName);
		if (project.exists())
			delete(project);
	}

	public static void delete(final IResource resource) throws CoreException {
		IWorkspaceRunnable runnable= monitor -> {
			for (int i= 0; i < MAX_RETRY; i++) {
				try {
					resource.delete(true, null);
					i= MAX_RETRY;
				} catch (CoreException e) {
					if (i == MAX_RETRY - 1) {
						ILog.get().log(e.getStatus());
						throw e;
					}
					System.gc(); // help windows to really close file locks
					try {
						Thread.sleep(1000); // sleep a second
					} catch (InterruptedException e1) {
					}
				}
			}
		};
		ResourcesPlugin.getWorkspace().run(runnable, null);

	}

	/**
	 * Creates a folder and all parent folders if not existing. Project must exist.
	 *
	 * @param folder The folder to create
	 * @return Returns the input folder
	 * @throws CoreException if creation fails
	 */
	public static IFolder createFolder(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent= folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder)parent);
			}
			folder.create(true, true, NULL_MONITOR);
		}
		return folder;
	}

	public static IFile createFile(IFolder folder, String name, String contents, String encoding) throws CoreException, IOException {
		IFile file= folder.getFile(name);
		if (contents == null)
			contents= ""; //$NON-NLS-1$
		try (InputStream inputStream= new ByteArrayInputStream(contents.getBytes(encoding))) {
			file.create(inputStream, true, NULL_MONITOR);
			file.setCharset(encoding, null);
		}

		return file;
	}

	public static IFile createFile(IFolder folder, String name, String contents) throws CoreException, IOException {
		return createFile(folder, name, contents, "ISO-8859-1");
	}

	public static IFile createLinkedFile(IContainer container, IPath linkPath, File linkedFileTarget) throws CoreException {
		IFile iFile= container.getFile(linkPath);
		iFile.createLink(IPath.fromOSString(linkedFileTarget.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return iFile;
	}

	public static IFile createLinkedFile(IContainer container, IPath linkPath, Plugin plugin, IPath linkedFileTargetPath) throws CoreException {
		File file= FileTool.getFileInPlugin(plugin, linkedFileTargetPath);
		IFile iFile= container.getFile(linkPath);
		iFile.createLink(IPath.fromOSString(file.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return iFile;
	}

	public static IFolder createLinkedFolder(IContainer container, IPath linkPath, File linkedFolderTarget) throws CoreException {
		IFolder folder= container.getFolder(linkPath);
		folder.createLink(IPath.fromOSString(linkedFolderTarget.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return folder;
	}

	public static IFolder createLinkedFolder(IContainer container, IPath linkPath, Plugin plugin, IPath linkedFolderTargetPath) throws CoreException {
		File file= FileTool.getFileInPlugin(plugin, linkedFolderTargetPath);
		IFolder iFolder= container.getFolder(linkPath);
		iFolder.createLink(IPath.fromOSString(file.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return iFolder;
	}

	public static IProject createLinkedProject(String projectName, Plugin plugin, IPath linkPath) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IProject project= workspace.getRoot().getProject(projectName);

		IProjectDescription desc= workspace.newProjectDescription(projectName);
		File file= FileTool.getFileInPlugin(plugin, linkPath);
		IPath projectLocation= IPath.fromOSString(file.getAbsolutePath());
		if (Platform.getLocation().equals(projectLocation))
			projectLocation= null;
		desc.setLocation(projectLocation);

		project.create(desc, NULL_MONITOR);
		if (!project.isOpen())
			project.open(NULL_MONITOR);

		return project;
	}

	public static IProject createJUnitSourceProject(String projectName) throws CoreException, ZipException, IOException {
		IProject project= ResourceHelper.createProject(projectName);
		try (ZipFile zip= new ZipFile(FileTool.getFileInBundle(FrameworkUtil.getBundle(ResourceHelper.class), IPath.fromOSString("testresources/junit37-noUI-src.zip")))) { //$NON-NLS-1$
			FileTool.unzip(zip, project.getLocation().toFile());
		}
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		return project;
	}
}
