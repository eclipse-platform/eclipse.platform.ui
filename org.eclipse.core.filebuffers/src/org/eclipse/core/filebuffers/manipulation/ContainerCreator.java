/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.filebuffers.manipulation;

import org.eclipse.core.internal.filebuffers.FileBuffersPlugin;
import org.eclipse.core.internal.filebuffers.NLSUtility;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;


/**
 * Helper class to create a container and all missing
 * parent containers.
 *
 * @since 3.1
 */
public class ContainerCreator {

	private IPath fContainerFullPath;
	private IContainer fContainer;
	private IWorkspace fWorkspace;

	/**
	 * Constructs a container creator for the given path
	 * in the given workspace.
	 *
	 * @param workspace the workspace in which to create the container
	 * @param fullPath the full path of the container, must not denote a file
	 */
	public ContainerCreator(IWorkspace workspace, IPath fullPath) {
		fWorkspace= workspace;
		fContainerFullPath = fullPath;
	}


	/**
	 * Creates this container.
	 *
	 * @param progressMonitor the progress monitor or <code>null</code> if none
	 * @return the container specified by this container creator's full path
	 * @throws CoreException if this container creator's full path denotes a file or creating
	 * 							either the project or folders for the given container fails
	 */
	public IContainer createContainer(IProgressMonitor progressMonitor) throws CoreException {
		IWorkspaceRunnable runnable= new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				monitor.beginTask(FileBuffersMessages.ContainerCreator_task_creatingContainer, fContainerFullPath.segmentCount());
				if (fContainer != null)
					return;

				// Does the container exist already?
				IWorkspaceRoot root= fWorkspace.getRoot();
				IResource found= root.findMember(fContainerFullPath);
				if (found instanceof IContainer) {
					fContainer= (IContainer) found;
					return;
				} else if (found != null) {
					// fContainerFullPath specifies a file as directory
					throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, NLSUtility.format(FileBuffersMessages.ContainerCreator_destinationMustBeAContainer, fContainerFullPath), null));
				}

				// Create the containers for the given path
				fContainer= root;
				for (int i = 0; i < fContainerFullPath.segmentCount(); i++) {
					String currentSegment = fContainerFullPath.segment(i);
					IResource resource = fContainer.findMember(currentSegment);
					if (resource != null) {
						if (resource instanceof IContainer) {
							fContainer= (IContainer) resource;
							monitor.worked(1);
						} else {
							// fContainerFullPath specifies a file as directory
							throw new CoreException(new Status(IStatus.ERROR, FileBuffersPlugin.PLUGIN_ID, IStatus.OK, NLSUtility.format(FileBuffersMessages.ContainerCreator_destinationMustBeAContainer, resource.getFullPath()), null));
						}
					}
					else {
						if (i == 0) {
							IProject projectHandle= createProjectHandle(root, currentSegment);
							IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
							fContainer= createProject(projectHandle, subMonitor);
							subMonitor.done();
						}
						else {
							IFolder folderHandle= createFolderHandle(fContainer, currentSegment);
							IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 1);
							fContainer= createFolder(folderHandle, subMonitor);
							subMonitor.done();
						}
					}
				}
			}
		};

		// Get scheduling rule
		IWorkspaceRoot root= fWorkspace.getRoot();
		IPath existingParentPath= fContainerFullPath;
		while (!root.exists(existingParentPath))
			existingParentPath= existingParentPath.removeLastSegments(1);

		IResource schedulingRule= root.findMember(existingParentPath);
		fWorkspace.run(runnable, schedulingRule, IWorkspace.AVOID_UPDATE, progressMonitor);
		return fContainer;
	}

	private IFolder createFolder(IFolder folderHandle, IProgressMonitor monitor) throws CoreException {
		folderHandle.create(false, true, monitor);
		if (monitor.isCanceled())
			throw new OperationCanceledException();
		return folderHandle;
	}

	private IFolder createFolderHandle(IContainer container, String folderName) {
		return container.getFolder(new Path(folderName));
	}

	private IProject createProject(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("", 100);//$NON-NLS-1$
		try {

			IProgressMonitor subMonitor= new SubProgressMonitor(monitor, 50);
			projectHandle.create(subMonitor);
			subMonitor.done();

			if (monitor.isCanceled())
				throw new OperationCanceledException();

			subMonitor= new SubProgressMonitor(monitor, 50);
			projectHandle.open(subMonitor);
			subMonitor.done();

			if (monitor.isCanceled())
				throw new OperationCanceledException();

		} finally {
			monitor.done();
		}

		return projectHandle;
	}

	private IProject createProjectHandle(IWorkspaceRoot root, String projectName) {
		return root.getProject(projectName);
	}
}
