/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.tests;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.net.URL;

import org.eclipse.core.internal.filebuffers.ContainerGenerator;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;

/**
 * @since 3.0
 */
public class TestHelper {
	
	private final static IProgressMonitor NULL_MONITOR= new NullProgressMonitor();
	private static final int MAX_RETRY= 5;
	
	public static File getFileInPlugin(Plugin plugin, IPath path) {
		try {
			URL installURL= new URL(plugin.getDescriptor().getInstallURL(), path.toString());
			URL localURL= Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}
	
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
	
	public static void delete(final IProject project) throws CoreException {
		delete(project, true);
	}
	
	public static void delete(final IProject project, boolean deleteContent) throws CoreException {
		for (int i= 0; i < MAX_RETRY; i++) {
			try {
				project.delete(deleteContent, true, NULL_MONITOR);
				i= MAX_RETRY;
			} catch (CoreException x) {
				if (i == MAX_RETRY - 1) {
					FilebuffersTestPlugin.getDefault().getLog().log(x.getStatus());
					throw x;
				}
				try {
					Thread.sleep(1000); // sleep a second
				} catch (InterruptedException e) {
				} 
			}
		}
	}
	
	public static IFolder createFolder(String folderPath) throws CoreException {
		ContainerGenerator generator= new ContainerGenerator(ResourcesPlugin.getWorkspace(), new Path(folderPath));
		IContainer container= generator.generateContainer(NULL_MONITOR);
		if (container instanceof IFolder)
			return (IFolder) container;
		return null;
	}

	public static IFile createFile(IFolder folder, String name, String contents) throws CoreException {
		IFile file= folder.getFile(name);
		if (contents == null)
			contents= "";
		InputStream inputStream= new StringBufferInputStream(contents);
		file.create(inputStream, true, NULL_MONITOR);
		return file;
	}
	
	public static IFile createLinkedFile(IContainer container, IPath filePath, Plugin plugin, IPath linkPath) throws CoreException {
		IFile iFile= container.getFile(filePath);
		File file= getFileInPlugin(plugin, linkPath);
		iFile.createLink(new Path(file.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return iFile;
	}
	
	public static IFolder createLinkedFolder(IContainer container, IPath folderPath, Plugin plugin, IPath linkPath) throws CoreException {
		IFolder iFolder= container.getFolder(folderPath);
		File file= getFileInPlugin(plugin, linkPath);
		iFolder.createLink(new Path(file.getAbsolutePath()), IResource.ALLOW_MISSING_LOCAL, NULL_MONITOR);
		return iFolder;
	}
	
	public static IProject createLinkedProject(String projectName, Plugin plugin, IPath linkPath) throws CoreException {
		IWorkspace workspace= ResourcesPlugin.getWorkspace();
		IProject project= workspace.getRoot().getProject(projectName);
		
		IProjectDescription desc= workspace.newProjectDescription(projectName);
		File file= getFileInPlugin(plugin, linkPath);
		IPath projectLocation= new Path(file.getAbsolutePath());
		if (Platform.getLocation().equals(projectLocation))
			projectLocation= null;
		desc.setLocation(projectLocation);
		
		project.create(desc, NULL_MONITOR);
		if (!project.isOpen())
			project.open(NULL_MONITOR);
		
		return project;
	}
}
