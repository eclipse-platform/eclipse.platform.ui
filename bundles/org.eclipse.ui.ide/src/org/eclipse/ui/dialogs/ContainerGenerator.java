/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.dialogs;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * For creating non-existing folder resources along a given workspace path.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * ContainerGenerator gen = new ContainerGenerator(new Path("/A/B"));
 * IContainer res = null;
 * try {
 *   res = gen.getContainer(monitor); // creates project A and folder B if required
 * } catch (CoreException e) {
 *   // handle failure
 * } catch (OperationCanceledException e) {
 *   // handle cancelation
 * }
 * </pre>
 * </p>
 */
public class ContainerGenerator {
	private IPath containerFullPath;
	private IContainer container;
/**
 * Creates a generator for the container resource (folder or project) at the
 * given workspace path. Assumes the path has already been validated.
 * <p>
 * Call <code>getContainer</code> to create any missing resources along the
 * path.
 * </p>
 *
 * @param containerPath the workspace path of the container
 */
public ContainerGenerator(IPath containerPath) {
	super();
	this.containerFullPath = containerPath;
}
/**
 * Creates a folder resource for the given folder handle.
 *
 * @param folderHandle the handle to create a folder resource
 * @param monitor the progress monitor to show visual progress
 * @return the folder handle (<code>folderHandle</code>)
 * @exception CoreException if the operation fails
 * @exception OperationCanceledException if the operation is canceled
 */
private IFolder createFolder(IFolder folderHandle, IProgressMonitor monitor) throws CoreException {
	folderHandle.create(false, true, monitor);

	if (monitor.isCanceled())
		throw new OperationCanceledException();

	return folderHandle;
}
/**
 * Creates a folder resource handle for the folder with the given name.
 * This method does not create the folder resource; this is the responsibility
 * of <code>createFolder</code>.
 *
 * @param container the resource container
 * @param folderName the name of the folder
 * @return the new folder resource handle
 */
private IFolder createFolderHandle(IContainer container, String folderName) {
	return container.getFolder(new Path(folderName));
}
/**
 * Creates a project resource for the given project handle.
 *
 * @param projectHandle the handle to create a project resource
 * @param monitor the progress monitor to show visual progress
 * @return the project handle (<code>projectHandle</code>)
 * @exception CoreException if the operation fails
 * @exception OperationCanceledException if the operation is canceled
 */
private IProject createProject(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
	try {
		monitor.beginTask("",2000);//$NON-NLS-1$
	
		projectHandle.create(new SubProgressMonitor(monitor, 1000));
		if (monitor.isCanceled())
			throw new OperationCanceledException();

		projectHandle.open(new SubProgressMonitor(monitor, 1000));
		if (monitor.isCanceled())
			throw new OperationCanceledException();
	}
	finally {
		monitor.done();
	}

	return projectHandle;
}
/**
 * Creates a project resource handle for the project with the given name.
 * This method does not create the project resource; this is the responsibility
 * of <code>createProject</code>.
 *
 * @param root the workspace root resource
 * @param projectName the name of the project
 * @return the new project resource handle
 */
private IProject createProjectHandle(IWorkspaceRoot root, String projectName) {
	return root.getProject(projectName);
}
/**
 * Ensures that this generator's container resource exists. Creates any missing
 * resource containers along the path; does nothing if the container resource
 * already exists.
 * <p>
 * Note: This method should be called within a workspace modify operation since
 * it may create resources.
 * </p>
 *
 * @param monitor a progress monitor
 * @return the container resource
 * @exception CoreException if the operation fails
 * @exception OperationCanceledException if the operation is canceled
 */
public IContainer generateContainer(IProgressMonitor monitor) throws CoreException {
	WorkbenchPlugin.getPluginWorkspace().run(new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			monitor.beginTask(WorkbenchMessages.getString("ContainerGenerator.progressMessage"), 1000 * containerFullPath.segmentCount()); //$NON-NLS-1$
			if (container != null)
				return;

			// Does the container exist already?
			IWorkspaceRoot root = getWorkspaceRoot();
			container = (IContainer) root.findMember(containerFullPath);
			if (container != null)
				return;

			// Create the container for the given path
			container = root;
			for (int i = 0; i < containerFullPath.segmentCount(); i++) {
				String currentSegment = containerFullPath.segment(i);
				IResource resource = container.findMember(currentSegment);
				if (resource != null) {
					container = (IContainer) resource;
					monitor.worked(1000);
				}
				else {
					if (i == 0) {
						IProject projectHandle = createProjectHandle(root, currentSegment);
						container = createProject(projectHandle, new SubProgressMonitor(monitor,1000));
					}
					else {
						IFolder folderHandle = createFolderHandle(container, currentSegment);
						container = createFolder(folderHandle, new SubProgressMonitor(monitor,1000));
					}
				}
			}
		}
	}, monitor);
	return container;
}
/**
 * Returns the workspace root resource handle.
 *
 * @return the workspace root resource handle
 */
private IWorkspaceRoot getWorkspaceRoot() {
	return WorkbenchPlugin.getPluginWorkspace().getRoot();
}
}
