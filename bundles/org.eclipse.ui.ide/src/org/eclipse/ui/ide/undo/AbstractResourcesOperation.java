/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.ide.undo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.undo.ContainerDescription;
import org.eclipse.ui.internal.ide.undo.FileDescription;
import org.eclipse.ui.internal.ide.undo.ResourceDescription;
import org.eclipse.ui.internal.ide.undo.UndoMessages;

/**
 * An AbstractResourcesOperation represents an undoable operation that
 * manipulates resources. It provides implementations for resource rename,
 * delete, creation, and modification. It also assigns the workspace undo
 * context as the undo context for operations of this type.
 * 
 * This class is not intended to be subclassed by clients.
 * 
 * <strong>EXPERIMENTAL</strong> This class or interface has been added as part
 * of a work in progress. This API may change at any given time. Please do not
 * use this API without consulting with the Platform/UI team.
 * 
 * @since 3.3
 * 
 */
public abstract class AbstractResourcesOperation extends
		AbstractWorkspaceOperation {

	/**
	 * Delete all of the specified resources, returning resource descriptions
	 * that can be used to restore them.
	 * 
	 * This method is static to ensure "statelessness." The caller is typically
	 * an operation instance method responsible for maintaining the current
	 * state of resources and resource descriptions for the operation.
	 * 
	 * @param resourcesToDelete
	 *            an array of resources to be deleted
	 * @param monitor
	 *            the progress monitor to use to show the operation's progress
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * 
	 * @param deleteContent
	 *            a boolean indicating whether project content should be deleted
	 *            when a project resource is to be deleted
	 * @return an array of ResourceDescriptions that can be used to restore the
	 *         deleted resources.
	 * @throws CoreException
	 */
	protected static ResourceDescription[] delete(
			IResource[] resourcesToDelete, IProgressMonitor monitor,
			IAdaptable uiInfo, boolean deleteContent) throws CoreException {
		final List exceptions = new ArrayList();
		boolean forceOutOfSyncDelete = false;
		ResourceDescription[] returnedResourceDescriptions = new ResourceDescription[resourcesToDelete.length];
		monitor.beginTask("", resourcesToDelete.length); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_DeleteResourcesProgress);
		try {
			for (int i = 0; i < resourcesToDelete.length; ++i) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				IResource resource = resourcesToDelete[i];
				try {
					returnedResourceDescriptions[i] = delete(resource,
							new SubProgressMonitor(monitor, 1), uiInfo,
							forceOutOfSyncDelete, deleteContent);
				} catch (CoreException e) {
					if (resource.getType() == IResource.FILE) {
						IStatus[] children = e.getStatus().getChildren();
						if (children.length == 1
								&& children[0].getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
							int result = queryDeleteOutOfSync(resource, uiInfo);

							if (result == IDialogConstants.YES_ID) {
								// retry the delete with a force out of sync
								delete(resource, new SubProgressMonitor(
										monitor, 1), uiInfo, true,
										deleteContent);
							} else if (result == IDialogConstants.YES_TO_ALL_ID) {
								// all future attempts should force out of
								// sync
								forceOutOfSyncDelete = true;
								delete(resource, new SubProgressMonitor(
										monitor, 1), uiInfo,
										forceOutOfSyncDelete, deleteContent);
							} else if (result == IDialogConstants.CANCEL_ID) {
								throw new OperationCanceledException();
							} else {
								exceptions.add(e);
							}
						} else {
							exceptions.add(e);
						}
					} else {
						exceptions.add(e);
					}
				}
			}
			IStatus result = createResult(exceptions);
			if (!result.isOK()) {
				throw new CoreException(result);
			}
		} finally {
			monitor.done();
		}
		return returnedResourceDescriptions;
	}

	/**
	 * Recreate the resources from the specified resource descriptions.
	 * 
	 * This method is static to ensure "statelessness." The caller is typically
	 * an operation instance method responsible for maintaining the current
	 * state of resources and resource descriptions for the operation.
	 * 
	 * @param resourcesToRecreate
	 *            the ResourceDescriptions describing resources to be recreated
	 * @param monitor
	 *            the progress monitor used to show progress
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return an array of resources that were created
	 * @throws CoreException
	 */
	protected static IResource[] recreate(
			ResourceDescription[] resourcesToRecreate,
			IProgressMonitor monitor, IAdaptable uiInfo) throws CoreException {
		final List exceptions = new ArrayList();
		IResource[] resourcesToReturn = new IResource[resourcesToRecreate.length];
		monitor.beginTask("", resourcesToRecreate.length); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_CreateResourcesProgress);
		try {
			for (int i = 0; i < resourcesToRecreate.length; ++i) {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				try {
					resourcesToReturn[i] = resourcesToRecreate[i]
							.createResource(new SubProgressMonitor(monitor, 1));
				} catch (CoreException e) {
					exceptions.add(e);
				}
			}
			IStatus result = createResult(exceptions);
			if (!result.isOK()) {
				throw new CoreException(result);
			}
		} finally {
			monitor.done();
		}
		return resourcesToReturn;
	}

	/**
	 * Move the specified resource to the new path provided. The new path
	 * includes any new name desired for the moved resource. Return the resource
	 * descriptions for any resources that were overwritten as part of the move.
	 * 
	 * This method is static to ensure "statelessness." The caller is typically
	 * an operation instance method responsible for maintaining the current
	 * state of resources and resource descriptions for the operation.
	 * 
	 * @param resourceToMove
	 *            the resource to be moved
	 * @param newPath
	 *            the destination path for the resource, including its name
	 * @param monitor
	 *            the progress monitor used to show progress
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return an array of ResourceDescriptions describing any resources that
	 *         were overwritten by the move operation
	 * @throws CoreException
	 */

	protected static ResourceDescription[] move(IResource resourceToMove,
			IPath newPath, IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		monitor.beginTask("", 3); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_MovingResources);
		IWorkspaceRoot workspaceRoot = WorkspaceUndoSupport.getWorkspaceRoot();
		List overwrittenResources = new ArrayList();

		// Some moves are optimized and recorded as complete in this flag
		boolean moved = false;
		IResource newResource = workspaceRoot.findMember(newPath);
		// If a resource already exists at the new location, we must
		// overwrite it.
		if (newResource != null) {
			// File on file overwrite is optimized as a reset of the
			// target file's contents
			if (resourceToMove.getType() == IResource.FILE
					&& newResource.getType() == IResource.FILE) {
				IFile file = (IFile) resourceToMove;
				IFile existingFile = (IFile) newResource;
				ResourceDescription resourceDescription = copyOverExistingResource(
						file, existingFile, monitor, uiInfo, true);
				if (resourceDescription != null) {
					overwrittenResources.add(resourceDescription);
					moved = true;
				}
			} else {
				// Any other overwrite is simply a delete of the resource
				// that is being overwritten, followed by the move
				ResourceDescription[] deleted = delete(
						new IResource[] { newResource },
						new SubProgressMonitor(monitor, 1), uiInfo, false);
				for (int j = 0; j < deleted.length; j++) {
					overwrittenResources.add(deleted[j]);
				}
			}
		} else {
			monitor.worked(100);
		}
		// Overwrites have been handled. If there is still a move to do, do
		// so now.
		if (!moved) {
			if (resourceToMove.getType() == IResource.PROJECT) {
				IProject project = (IProject) resourceToMove;
				IProjectDescription description = project.getDescription();
				description.setName(newPath.lastSegment());
				// If the location is the default then set the location
				// to null
				IPath projectLocation = newPath.removeLastSegments(1);
				if (projectLocation.equals(Platform.getLocation())) {
					description.setLocation(null);
				} else {
					description.setLocation(projectLocation);
				}
				project.move(description, IResource.FORCE | IResource.SHALLOW,
						new SubProgressMonitor(monitor, 2));
			} else {
				generateContainers(newPath.removeLastSegments(1));
				resourceToMove
						.move(newPath, IResource.KEEP_HISTORY
								| IResource.SHALLOW, new SubProgressMonitor(
								monitor, 2));

			}
		}
		monitor.done();
		return (ResourceDescription[]) overwrittenResources
				.toArray(new ResourceDescription[overwrittenResources.size()]);
	}

	/**
	 * Copies the resources to the given destination. This method can be called
	 * recursively to merge folders during folder copy.
	 * 
	 * This method is static to ensure "statelessness." The caller is typically
	 * an operation instance method responsible for maintaining the current
	 * state of resources and resource descriptions for the operation.
	 * 
	 * @param resources
	 *            the resources to be copied
	 * @param destination
	 *            the destination path for the resources
	 * @param monitor
	 *            the progress monitor used to show progress
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @param pathIncludesName
	 *            a boolean that indicates whether the specified path includes
	 *            the resource's name at the destination. If this value is
	 *            <code>true</code>, the destination will contain the desired name of the
	 *            resource (usually only desired when only one resource is being
	 *            copied). If this value is <code>false</code>, each
	 *            resource's name will be appended to the destination.
	 * @return an array of ResourceDescriptions describing any resources that
	 *         were overwritten by the move operation
	 * @throws CoreException
	 */
	protected static ResourceDescription[] copy(IResource[] resources,
			IPath destination, IProgressMonitor monitor, IAdaptable uiInfo,
			boolean pathIncludesName) throws CoreException {

		monitor.beginTask("", resources.length); //$NON-NLS-1$
		monitor
				.setTaskName(UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress);
		List overwrittenResources = new ArrayList();
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath;
			if (pathIncludesName) {
				destinationPath = destination;
			} else {
				destinationPath = destination.append(source.getName());
			}
			IWorkspaceRoot workspaceRoot = WorkspaceUndoSupport
					.getWorkspaceRoot();
			IResource existing = workspaceRoot.findMember(destinationPath);
			if (source.getType() == IResource.FOLDER && existing != null) {
				// The resource is a folder and it exists in the destination.
				// Copy its children to the existing destination.
				if (homogenousResources(source, existing)) {
					IResource[] children = ((IContainer) source).members();
					ResourceDescription[] overwritten = copy(children,
							destinationPath,
							new SubProgressMonitor(monitor, 1), uiInfo, false);
					for (int j = 0; j < overwritten.length; j++) {
						overwrittenResources.add(overwritten[j]);
					}

				} else {
					// delete the destination folder, copying a linked folder
					// over an unlinked one or vice versa. Fixes bug 28772.
					ResourceDescription[] deleted = delete(
							new IResource[] { existing },
							new SubProgressMonitor(monitor, 0), uiInfo, false);
					source.copy(destinationPath, IResource.SHALLOW,
							new SubProgressMonitor(monitor, 1));
					for (int j = 0; j < deleted.length; j++) {
						overwrittenResources.add(deleted[j]);
					}
				}
			} else {
				if (existing != null) {
					if (homogenousResources(source, existing)) {
						overwrittenResources.add(copyOverExistingResource(
								source, existing, new SubProgressMonitor(
										monitor, 1), uiInfo, false));
					} else {
						// Copying a linked resource over unlinked or vice
						// versa. Can't use setContents here. Fixes bug 28772.
						ResourceDescription[] deleted = delete(
								new IResource[] { existing },
								new SubProgressMonitor(monitor, 0), uiInfo,
								false);
						source.copy(destinationPath, IResource.SHALLOW,
								new SubProgressMonitor(monitor, 1));
						for (int j = 0; j < deleted.length; j++) {
							overwrittenResources.add(deleted[j]);
						}
					}
				} else {
					// no resources are being overwritten
					if (source.getType() == IResource.PROJECT) {
						// Get a copy of the current description and modify it
						IProjectDescription newDescription = ((IProject) source)
								.getDescription();
						newDescription.setName(destinationPath.lastSegment());
						// If the location is the default then set the location
						// to null
						IPath projectLocation = destinationPath
								.removeLastSegments(1);
						if (projectLocation.equals(Platform.getLocation())) {
							newDescription.setLocation(null);
						} else {
							newDescription.setLocation(projectLocation);
						}
						source.copy(newDescription, IResource.SHALLOW
								| IResource.FORCE, monitor);
					} else {
						// ensure the destination path exists
						IPath parentPath = destination;
						if (pathIncludesName) {
							parentPath = destination.removeLastSegments(1);
						}
						generateContainers(parentPath);
						source.copy(destinationPath, IResource.SHALLOW,
								new SubProgressMonitor(monitor, 1));
					}
				}

				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
		return (ResourceDescription[]) overwrittenResources
				.toArray(new ResourceDescription[overwrittenResources.size()]);

	}

	/**
	 * Delete the specified resources, returning a resource description that can
	 * be used to restore it.
	 * 
	 * This method is static to ensure "statelessness." The caller is typically
	 * an operation instance method responsible for maintaining the current
	 * state of resources and resource descriptions for the operation.
	 * 
	 * @param resourceToDelete
	 *            the resource to be deleted
	 * @param monitor
	 *            the progress monitor to use to show the operation's progress
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @param forceOutOfSyncDelete
	 *            a boolean indicating whether a resource should be deleted even
	 *            if it is out of sync with the file system
	 * @param deleteContent
	 *            a boolean indicating whether project content should be deleted
	 *            when a project resource is to be deleted
	 * @return a ResourceDescription that can be used to restore the deleted
	 *         resource.
	 * @throws CoreException
	 */
	protected static ResourceDescription delete(IResource resourceToDelete,
			IProgressMonitor monitor, IAdaptable uiInfo,
			boolean forceOutOfSyncDelete, boolean deleteContent)
			throws CoreException {
		ResourceDescription resourceDescription = ResourceDescription
				.fromResource(resourceToDelete);
		if (resourceToDelete.getType() == IResource.PROJECT) {
			// it is a project
			monitor
					.setTaskName(UndoMessages.AbstractResourcesOperation_DeleteResourcesProgress);
			IProject project = (IProject) resourceToDelete;
			project.delete(deleteContent, forceOutOfSyncDelete, monitor);
		} else {
			// if it's not a project, just delete it
			monitor.beginTask("", 2); //$NON-NLS-1$
			monitor
					.setTaskName(UndoMessages.AbstractResourcesOperation_DeleteResourcesProgress);
			int updateFlags;
			if (forceOutOfSyncDelete) {
				updateFlags = IResource.KEEP_HISTORY | IResource.FORCE;
			} else {
				updateFlags = IResource.KEEP_HISTORY;
			}
			resourceToDelete.delete(updateFlags, new SubProgressMonitor(
					monitor, 1));
			resourceDescription.recordLastHistory(resourceToDelete,
					new SubProgressMonitor(monitor, 1));
			monitor.done();
		}

		return resourceDescription;
	}

	/*
	 * Copy the content of the specified resource to the existing resource,
	 * returning a ResourceDescription that can be used to restore the original
	 * content. Do nothing if the resources are not files.
	 */
	private static ResourceDescription copyOverExistingResource(
			IResource source, IResource existing, IProgressMonitor monitor,
			IAdaptable uiInfo, boolean deleteSourceFile) throws CoreException {
		IFile file = getFile(source);
		IFile existingFile = getFile(existing);
		monitor
				.beginTask(
						UndoMessages.AbstractResourcesOperation_CopyingResourcesProgress,
						3);
		if (file != null && existingFile != null) {
			if (validateEdit(file, existingFile, getShell(uiInfo))) {
				// Remember the state of the existing file so it can be
				// restored.
				FileDescription fileDescription = new FileDescription(
						existingFile);
				// Reset the contents to that of the file being moved
				existingFile.setContents(file.getContents(),
						IResource.KEEP_HISTORY, new SubProgressMonitor(monitor,
								1));
				fileDescription.recordLastHistory(existingFile,
						new SubProgressMonitor(monitor, 1));
				// Now delete the source file if requested
				// We don't need to remember anything about it, because
				// any undo involving this operation will move the original
				// content back to it.
				if (deleteSourceFile) {
					file.delete(IResource.KEEP_HISTORY, new SubProgressMonitor(
							monitor, 1));
				}
				monitor.done();
				return fileDescription;
			}
		}
		monitor.done();
		return null;
	}

	/*
	 * Returns the given resource either casted to or adapted to an IFile, or
	 * <code>null</code> if the resource cannot be adapted to an IFile.
	 * 
	 */
	private static IFile getFile(IResource resource) {
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		return (IFile) ((IAdaptable) resource).getAdapter(IFile.class);
	}

	/*
	 * Check for existence of the specified path and generate any containers
	 * that do not yet exist.
	 */
	private static void generateContainers(IPath path) throws CoreException {
		IContainer container;
		if (path.segmentCount() == 1) {
			container = ResourcesPlugin.getWorkspace().getRoot().getProject(
					path.segment(0));
		} else {
			container = ResourcesPlugin.getWorkspace().getRoot()
					.getFolder(path);
		}
		if (container != null) {
			ContainerDescription containerDescription = ContainerDescription
					.fromContainer(container);
			containerDescription.createExistentResourceFromHandle(container,
					new NullProgressMonitor());
		}

	}

	/*
	 * Ask the user whether the given resource should be deleted despite being
	 * out of sync with the file system.
	 * 
	 * Return one of the IDialogConstants constants indicating which of the Yes,
	 * Yes to All, No, Cancel options has been selected by the user.
	 */
	private static int queryDeleteOutOfSync(IResource resource,
			IAdaptable uiInfo) {
		Shell shell = getShell(uiInfo);
		final MessageDialog dialog = new MessageDialog(
				shell,
				UndoMessages.AbstractResourcesOperation_deletionMessageTitle,
				null,
				NLS
						.bind(
								UndoMessages.AbstractResourcesOperation_outOfSyncQuestion,
								resource.getName()), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		int result = dialog.getReturnCode();
		if (result == 0) {
			return IDialogConstants.YES_ID;
		}
		if (result == 1) {
			return IDialogConstants.YES_TO_ALL_ID;
		}
		if (result == 2) {
			return IDialogConstants.NO_ID;
		}
		return IDialogConstants.CANCEL_ID;
	}

	/*
	 * Creates and return a result status appropriate for the given list of
	 * exceptions.
	 */
	private static IStatus createResult(List exceptions) {
		if (exceptions.isEmpty()) {
			return Status.OK_STATUS;
		}
		final int exceptionCount = exceptions.size();
		if (exceptionCount == 1) {
			return ((CoreException) exceptions.get(0)).getStatus();
		}
		CoreException[] children = (CoreException[]) exceptions
				.toArray(new CoreException[exceptionCount]);
		boolean outOfSync = false;
		for (int i = 0; i < children.length; i++) {
			if (children[i].getStatus().getCode() == IResourceStatus.OUT_OF_SYNC_LOCAL) {
				outOfSync = true;
				break;
			}
		}
		String title = outOfSync ? UndoMessages.AbstractResourcesOperation_outOfSyncError
				: UndoMessages.AbstractResourcesOperation_deletionExceptionMessage;
		final MultiStatus multi = new MultiStatus(
				IDEWorkbenchPlugin.IDE_WORKBENCH, 0, title, null);
		for (int i = 0; i < exceptionCount; i++) {
			CoreException exception = children[i];
			IStatus status = exception.getStatus();
			multi.add(new Status(status.getSeverity(), status.getPlugin(),
					status.getCode(), status.getMessage(), exception));
		}
		return multi;
	}

	/*
	 * Validate the destination file if it is read-only and additionally the
	 * source file if both are read-only. Returns true if both files could be
	 * made writeable.
	 */
	private static boolean validateEdit(IFile source, IFile destination,
			Shell shell) {
		if (destination.isReadOnly()) {
			IWorkspace workspace = WorkspaceUndoSupport.getWorkspace();
			IStatus status;
			if (source.isReadOnly()) {
				status = workspace.validateEdit(new IFile[] { source,
						destination }, shell);
			} else {
				status = workspace.validateEdit(new IFile[] { destination },
						shell);
			}
			return status.isOK();
		}
		return true;
	}

	/*
	 * Returns whether the given resources are either both linked or both
	 * unlinked.
	 */
	private static boolean homogenousResources(IResource source,
			IResource destination) {
		boolean isSourceLinked = source.isLinked();
		boolean isDestinationLinked = destination.isLinked();

		return (isSourceLinked && isDestinationLinked || isSourceLinked == false
				&& isDestinationLinked == false);
	}

	/*
	 * The array of resource descriptions known by this operation to create or
	 * restore overwritten resources.
	 */
	protected ResourceDescription[] resourceDescriptions;

	/**
	 * Create an Abstract Resources Operation
	 * 
	 * @param resources
	 *            the resources to be modified
	 * @param label
	 *            the label of the operation
	 */
	AbstractResourcesOperation(IResource[] resources, String label) {
		super(label);
		this.addContext(WorkspaceUndoSupport.getWorkspaceUndoContext());

		setTargetResources(resources);
	}

	/**
	 * Create an Abstract Resources Operation
	 * 
	 * @param resourceDescriptions
	 *            the resourceDescriptions describing resources to be created
	 * @param label
	 *            the label of the operation
	 */
	AbstractResourcesOperation(ResourceDescription[] resourceDescriptions,
			String label) {
		super(label);
		addContext(WorkspaceUndoSupport.getWorkspaceUndoContext());
		setResourceDescriptions(resourceDescriptions);
	}

	/**
	 * Delete any resources known by this operation. Store enough information to
	 * undo and redo the operation.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @param deleteContent
	 *            <code>true</code> if the content of any known projects
	 *            should be deleted along with the project. <code>false</code>
	 *            if project content should not be deleted.
	 * @throws CoreException
	 */
	protected void delete(IProgressMonitor monitor, IAdaptable uiInfo,
			boolean deleteContent) throws CoreException {
		setResourceDescriptions(AbstractResourcesOperation.delete(resources,
				monitor, uiInfo, deleteContent));
		setTargetResources(new IResource[0]);
	}

	/**
	 * Recreate any resources known by this operation. Store enough information
	 * to undo and redo the operation.
	 * 
	 * @param monitor
	 *            the progress monitor to use for the operation
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user if necessary. When this parameter is not
	 *            <code>null</code>, it contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @throws CoreException
	 */
	protected void recreate(IProgressMonitor monitor, IAdaptable uiInfo)
			throws CoreException {
		setTargetResources(AbstractResourcesOperation.recreate(
				resourceDescriptions, monitor, uiInfo));
		setResourceDescriptions(new ResourceDescription[0]);
	}

	/**
	 * Compute the status for creating resources from the descriptions. A status
	 * severity of <code>OK</code> indicates that the create is likely to be
	 * successful. A status severity of <code>ERROR</code> indicates that the
	 * operation is no longer valid. Other status severities are open to
	 * interpretation by the caller.
	 * 
	 * Note this method may be called on initial creation of a resource, or when
	 * a create or delete operation is being undone or redone. Therefore, this
	 * method should check conditions that can change over the life of the
	 * operation, such as the existence of the information needed to carry out
	 * the operation. One-time static checks should typically be done by the
	 * caller (such as the action that creates the operation) so that the user
	 * is not continually prompted or warned about conditions that were
	 * acceptable at the time of original execution.
	 */
	protected IStatus computeCreateStatus() {
		if (resourceDescriptions == null || resourceDescriptions.length == 0) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		for (int i = 0; i < resourceDescriptions.length; i++) {
			if (!resourceDescriptions[i].isValid()) {
				markInvalid();
				return getErrorStatus(UndoMessages.AbstractResourcesOperation_InvalidRestoreInfo);
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Compute the status for deleting resources. A status severity of
	 * <code>OK</code> indicates that the delete is likely to be successful. A
	 * status severity of <code>ERROR</code> indicates that the operation is
	 * no longer valid. Other status severities are open to interpretation by
	 * the caller.
	 * 
	 * Note this method may be called on initial deletion of a resource, or when
	 * a create or delete operation is being undone or redone. Therefore, this
	 * method should check conditions that can change over the life of the
	 * operation, such as the existence of the resources to be deleted. One-time
	 * static checks should typically be done by the caller (such as the action
	 * that creates the operation) so that the user is not continually prompted
	 * or warned about conditions that were acceptable at the time of original
	 * execution.
	 */
	protected IStatus computeDeleteStatus() {
		if (resources == null || resources.length == 0) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_NotEnoughInfo);
		}
		if (!resourcesExist()) {
			markInvalid();
			return getErrorStatus(UndoMessages.AbstractResourcesOperation_ResourcesDoNotExist);
		}
		return Status.OK_STATUS;
	}

	/**
	 * Ask the user whether the given resource should be overwritten.
	 * 
	 * @param resource
	 *            the resource to be overwritten
	 * @param uiInfo
	 *            the IAdaptable (or <code>null</code>) provided by the
	 *            caller in order to supply UI information for prompting the
	 *            user. When this parameter is not <code>null</code>, it
	 *            contains an adapter for the
	 *            org.eclipse.swt.widgets.Shell.class
	 * @return One of the IDialogConstants constants indicating which of the
	 *         Yes, Yes to All, No, Cancel options has been selected by the
	 *         user.
	 */
	protected int queryOverwrite(IResource resource, IAdaptable uiInfo) {
		Shell shell = getShell(uiInfo);
		final MessageDialog dialog = new MessageDialog(
				shell,
				UndoMessages.AbstractResourcesOperation_overwriteTitle,
				null,
				NLS
						.bind(
								UndoMessages.AbstractResourcesOperation_overwriteQuestion,
								resource.getName()), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL }, 0);
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				dialog.open();
			}
		});
		int result = dialog.getReturnCode();
		if (result == 0) {
			return IDialogConstants.YES_ID;
		}
		if (result == 1) {
			return IDialogConstants.YES_TO_ALL_ID;
		}
		if (result == 2) {
			return IDialogConstants.NO_ID;
		}
		return IDialogConstants.CANCEL_ID;
	}

	/**
	 * Set the array of resource descriptions describing resources to be
	 * restored when undoing or redoing this operation.
	 * 
	 * @param descriptions
	 *            the array of resource descriptions
	 */
	protected void setResourceDescriptions(ResourceDescription[] descriptions) {
		if (descriptions == null) {
			resourceDescriptions = new ResourceDescription[0];
		} else {
			resourceDescriptions = descriptions;
		}
	}
}
