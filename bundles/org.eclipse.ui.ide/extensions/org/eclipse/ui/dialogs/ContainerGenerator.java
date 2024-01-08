/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.ui.dialogs;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;

/**
 * For creating folder resources that currently do not exist, along a given
 * workspace path.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * ContainerGenerator gen = new ContainerGenerator(IPath.fromOSString("/A/B"));
 * IContainer res = null;
 * try {
 * 	res = gen.getContainer(monitor); // creates project A and folder B if required
 * } catch (CoreException e) {
 * 	// handle failure
 * } catch (OperationCanceledException e) {
 * 	// handle cancelation
 * }
 * </pre>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class ContainerGenerator {
	private IPath containerFullPath;

	private IContainer generatedContainer;

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
	 * @param monitor      the progress monitor to show visual progress
	 * @return the folder handle (<code>folderHandle</code>)
	 * @exception CoreException              if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	private IFolder createFolder(IFolder folderHandle, IProgressMonitor monitor) throws CoreException {
		folderHandle.create(false, true, monitor);

		return folderHandle;
	}

	/**
	 * Creates a folder resource handle for the folder with the given name. This
	 * method does not create the folder resource; this is the responsibility of
	 * <code>createFolder</code>.
	 *
	 * @param container  the resource container
	 * @param folderName the name of the folder
	 * @return the new folder resource handle
	 */
	private IFolder createFolderHandle(IContainer container, String folderName) {
		return container.getFolder(IPath.fromOSString(folderName));
	}

	/**
	 * Creates a project resource for the given project handle.
	 *
	 * @param projectHandle the handle to create a project resource
	 * @param monitor       the progress monitor to show visual progress
	 * @return the project handle (<code>projectHandle</code>)
	 * @exception CoreException              if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	private IProject createProject(IProject projectHandle, IProgressMonitor monitor) throws CoreException {
		SubMonitor subMonitor = SubMonitor.convert(monitor, 2);
		projectHandle.create(subMonitor.split(1));
		projectHandle.open(subMonitor.split(1));
		return projectHandle;
	}

	/**
	 * Creates a project resource handle for the project with the given name. This
	 * method does not create the project resource; this is the responsibility of
	 * <code>createProject</code>.
	 *
	 * @param root        the workspace root resource
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
	 * @exception CoreException              if the operation fails
	 * @exception OperationCanceledException if the operation is canceled
	 */
	public IContainer generateContainer(IProgressMonitor monitor) throws CoreException {
		IDEWorkbenchPlugin.getPluginWorkspace().run(monitor1 -> {
			SubMonitor subMonitor = SubMonitor.convert(monitor1,
					IDEWorkbenchMessages.ContainerGenerator_progressMessage, containerFullPath.segmentCount());
			if (generatedContainer != null) {
				return;
			}

			// Does the container exist already?
			IWorkspaceRoot root = getWorkspaceRoot();
			generatedContainer = (IContainer) root.findMember(containerFullPath);
			if (generatedContainer != null) {
				return;
			}

			// Create the container for the given path
			generatedContainer = root;
			for (int i = 0; i < containerFullPath.segmentCount(); i++) {
				String currentSegment = containerFullPath.segment(i);
				IResource resource = generatedContainer.findMember(currentSegment);
				if (resource != null) {
					if (resource.getType() == IResource.FILE) {
						String msg = NLS.bind(IDEWorkbenchMessages.ContainerGenerator_pathOccupied,
								resource.getFullPath().makeRelative());
						throw new CoreException(
								new Status(IStatus.ERROR, IDEWorkbenchPlugin.IDE_WORKBENCH, 1, msg, null));
					}
					generatedContainer = (IContainer) resource;
					subMonitor.worked(1);
				} else if (i == 0) {
					IProject projectHandle = createProjectHandle(root, currentSegment);
					generatedContainer = createProject(projectHandle, subMonitor.split(1));
				} else {
					IFolder folderHandle = createFolderHandle(generatedContainer, currentSegment);
					generatedContainer = createFolder(folderHandle, subMonitor.split(1));
				}
			}
		}, null, IResource.NONE, monitor);
		return generatedContainer;
	}

	/**
	 * Returns the workspace root resource handle.
	 *
	 * @return the workspace root resource handle
	 */
	private IWorkspaceRoot getWorkspaceRoot() {
		return IDEWorkbenchPlugin.getPluginWorkspace().getRoot();
	}
}
