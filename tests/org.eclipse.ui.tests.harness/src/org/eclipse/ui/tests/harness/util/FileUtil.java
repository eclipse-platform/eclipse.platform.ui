/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
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
package org.eclipse.ui.tests.harness.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <code>FileUtil</code> contains methods to create and
 * delete files and projects.
 */
public class FileUtil {

	private static final int MAX_RETRY = 5;


	/**
	 * Creates a new project.
	 *
	 * @param name the project name
	 */
	public static IProject createProject(String name) throws CoreException {
		IWorkspace ws = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		IProject proj = root.getProject(name);
		if (!proj.exists())
			proj.create(null);
		if (!proj.isOpen())
			proj.open(null);
		return proj;
	}

	/**
	 * Deletes a project.
	 *
	 * @param proj the project
	 */
	public static void deleteProject(IProject proj) throws CoreException {
		proj.delete(true, null);
	}

    /**
	 * Creates a folder and all parent folders if not existing. Project must exist.
	 * <code> org.eclipse.ui.dialogs.ContainerGenerator</code> is too heavy (creates
	 * a runnable)<br/>
	 * <br/>
	 * This method was copied from
	 * {@link org.eclipse.jdt.internal.ui.util.CoreUtility#createFolder(IFolder, boolean, boolean, IProgressMonitor)
	 * CoreUtility#createFolder}.
	 *
	 * @param folder  the folder to create
	 * @param force   a flag controlling how to deal with resources that are not in
	 *                sync with the local file system
	 * @param local   a flag controlling whether or not the folder will be local
	 *                after the creation
	 * @param monitor the progress monitor
	 * @throws CoreException thrown if the creation failed
	 */
	public static void createFolder(IFolder folder, boolean force, boolean local, IProgressMonitor monitor)
			throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolder((IFolder) parent, force, local, null);
			}
			folder.create(force, local, monitor);
		}
	}

	/**
	 * Creates a new file in a project.
	 *
	 * @param name the new file name
	 * @param proj the existing project
	 * @return the new file
	 */
	public static IFile createFile(String name, IProject proj) throws CoreException {
		IFile file = proj.getFile(name);
		if (!file.exists()) {
			String str = " ";
			InputStream in = new ByteArrayInputStream(str.getBytes());
			file.create(in, true, null);
		}
		return file;
	}

	public static void delete(IResource resource) throws CoreException {
		for (int i= 0; i < MAX_RETRY; i++) {
			try {
				resource.delete(true, null);
				i= MAX_RETRY;
			} catch (CoreException e) {
				if (i == MAX_RETRY - 1) {
					throw e;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
		}
	}



}

