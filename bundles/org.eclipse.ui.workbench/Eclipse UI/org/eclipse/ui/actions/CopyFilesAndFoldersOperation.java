/************************************************************************
Copyright (c) 2000, 2003 IBM Corporation and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    IBM - Initial implementation
************************************************************************/
package org.eclipse.ui.actions;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.*;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerGenerator;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Perform the copy of file and folder resources from the clipboard 
 * when paste action is invoked.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 */
public class CopyFilesAndFoldersOperation {

	/**
	 * Status containing the errors detected when running the operation or
	 * <code>null</code> if no errors detected.
	 */
	private MultiStatus errorStatus;

	/**
	 * The parent shell used to show any dialogs.
	 */
	private Shell parentShell;

	/**
	 * The destination of the resources to be copied.
	 */
	private IResource destination;

	/**
	 * A list of all resources against which copy errors are reported.
	 */
	private ArrayList errorResources = new ArrayList();

	/**
	 * Whether or not the copy has been canceled by the user.
	 */
	private boolean canceled = false;

	/**
	 * Overwrite all flag.
	 */
	private boolean alwaysOverwrite = false;

	/**
	 * Returns a new name for a copy of the resource at the given path in 
	 * the given workspace. This name is determined automatically. 
	 *
	 * @param originalName the full path of the resource
	 * @param workspace the workspace
	 * @return the new full path for the copy
	 */
	static IPath getAutoNewNameFor(IPath originalName, IWorkspace workspace) {
		int counter = 1;
		String resourceName = originalName.lastSegment();
		IPath leadupSegment = originalName.removeLastSegments(1);

		while (true) {
			String nameSegment;

			if (counter > 1)
				nameSegment = WorkbenchMessages.format("CopyFilesAndFoldersOperation.copyNameTwoArgs", new Object[] { new Integer(counter), resourceName }); //$NON-NLS-1$
			else
				nameSegment = WorkbenchMessages.format("CopyFilesAndFoldersOperation.copyNameOneArg", new Object[] { resourceName }); //$NON-NLS-1$

			IPath pathToTry = leadupSegment.append(nameSegment);

			if (!workspace.getRoot().exists(pathToTry))
				return pathToTry;

			counter++;
		}
	}
	/** 
	 * Creates a new operation initialized with a shell.
	 * 
	 * @param shell parent shell for error dialogs
	 */
	public CopyFilesAndFoldersOperation(Shell shell) {
		parentShell = shell;
	}
	/**
	 * Returns whether this operation is able to perform on-the-fly 
	 * auto-renaming of resources with name collisions.
	 *
	 * @return <code>true</code> if auto-rename is supported, 
	 * 	and <code>false</code> otherwise
	 */
	protected boolean canPerformAutoRename() {
		return true;
	}
	/**
	 * Returns the message for querying deep copy/move of a linked 
	 * resource.
	 *
	 * @param source resource the query is made for
	 * @return the deep query message
	 */
	protected String getDeepCheckQuestion(IResource source) {
		return WorkbenchMessages.format(
			"CopyFilesAndFoldersOperation.deepCopyQuestion", //$NON-NLS-1$
			new Object[] {source.getFullPath().makeRelative()});
	}
	/**
	 * Checks whether the files with the given names exist. 
	 *
	 * @param names path to the file. must not be null.
	 * 	If the path is not valid it will not be tested. 
	 * @return Multi status with one error message for each missing file.
	 */
	IStatus checkExist(String[] names) {
		MultiStatus multiStatus = new MultiStatus(
			PlatformUI.PLUGIN_ID, 
			IStatus.OK,
			getProblemsMessage(),
			null);
			 		
		for (int i = 0; i < names.length; i++) {
			IPath path = new Path(names[i]);
			File file = path.toFile();
			
			if (file != null && file.exists() == false) {
				String message = WorkbenchMessages.format(
					"CopyFilesAndFoldersOperation.resourceDeleted",	//$NON-NLS-1$
					new Object[] {file.getName()});				
				IStatus status = new Status(
					IStatus.ERROR, 
					PlatformUI.PLUGIN_ID, 
					IStatus.OK, 
					message, 
					null);
				multiStatus.add(status);
			}
		}
		return multiStatus;
	}
	/**
	 * Checks whether the files with the given names exist. 
	 *
	 * @param names path to the file. must not be null.
	 * 	If the path is not valid it will not be tested. 
	 * @return Multi status with one error message for each missing file.
	 */
	IStatus checkExist(IResource[] resources) {
		MultiStatus multiStatus = new MultiStatus(
			PlatformUI.PLUGIN_ID, 
			IStatus.OK,
			getProblemsMessage(),
			null);
			 		
		for (int i = 0; i < resources.length; i++) {
			if (resources[i] != null && resources[i].exists() == false) {
				String message = WorkbenchMessages.format(
					"CopyFilesAndFoldersOperation.resourceDeleted",	//$NON-NLS-1$
					new Object[] {resources[i].getName()});				
				IStatus status = new Status(
					IStatus.ERROR, 
					PlatformUI.PLUGIN_ID, 
					IStatus.OK, 
					message, 
					null);
				multiStatus.add(status);
			}
		}
		return multiStatus;
	}
	/**
	 * Check if the user wishes to overwrite the supplied resource or 
	 * all resources.
	 * 
	 * @param shell the shell to create the overwrite prompt dialog in 
	 * @param destination the resource to be overwritten
	 * @return one of IDialogConstants.YES_ID, IDialogConstants.YES_TO_ALL_ID,
	 * 	IDialogConstants.NO_ID, IDialogConstants.CANCEL_ID indicating whether
	 * 	the current resource or all resources can be overwritten, or if the 
	 * 	operation should be canceled.
	 */
	private int checkOverwrite(final Shell shell, final IResource destination) {
		final int[] result = new int[1];

		// Dialogs need to be created and opened in the UI thread
		Runnable query = new Runnable() {
			public void run() {
				String message;
				int resultId[] = {
					IDialogConstants.YES_ID,
					IDialogConstants.YES_TO_ALL_ID,
					IDialogConstants.NO_ID,
					IDialogConstants.CANCEL_ID};
 
				if (destination.getType() == IResource.FOLDER) {
					message = WorkbenchMessages.format(
						"CopyFilesAndFoldersOperation.overwriteMergeQuestion", //$NON-NLS-1$
						new Object[] { destination.getFullPath().makeRelative()});
				} else {
					message = WorkbenchMessages.format(
						"CopyFilesAndFoldersOperation.overwriteQuestion", //$NON-NLS-1$
						new Object[] { destination.getFullPath().makeRelative()});
				}
				MessageDialog dialog = new MessageDialog(
					shell, 
					WorkbenchMessages.getString("CopyFilesAndFoldersOperation.resourceExists"), //$NON-NLS-1$
					null,
					message,
					MessageDialog.QUESTION,
					new String[] {
						IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL },
					0);
				dialog.open();
				result[0] = resultId[dialog.getReturnCode()];
			}
		};
		shell.getDisplay().syncExec(query);
		return result[0];
	}
	/**
	 * Recursively collects existing files in the specified destination path.
	 * 
	 * @param destinationPath destination path to check for existing files
	 * @param copyResources resources that may exist in the destination
	 * @param existing holds the collected existing files 
	 */
	private void collectExistingReadonlyFiles(IPath destinationPath, IResource[] copyResources, ArrayList existing) {
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

		for (int i = 0; i < copyResources.length; i++) {
			IResource source = copyResources[i];
			IPath newDestinationPath = destinationPath.append(source.getName());
			IResource newDestination = workspaceRoot.findMember(newDestinationPath);
			IFolder folder;
						
			if (newDestination == null) {
				continue;
			}
			folder = getFolder(newDestination);
			if (folder != null) {
				IFolder sourceFolder = getFolder(source);
			
				if (sourceFolder != null) {
					try {
						collectExistingReadonlyFiles(newDestinationPath, sourceFolder.members(), existing);
					}
					catch (CoreException exception) {
						recordError(exception); 
					}
				}
			}			
			else {
				IFile file = getFile(newDestination);
				
				if (file != null) {
					if (file.isReadOnly()) {
						existing.add(file);
					}
					if (getValidateConflictSource()) {
						IFile sourceFile = getFile(source);
						if (sourceFile != null) {
							existing.add(sourceFile);
						}
					}
				}
			}
		}
	} 
	/**
	 * Copies the resources to the given destination.  This method is 
	 * called recursively to merge folders during folder copy.
	 * 
	 * @param resources the resources to copy
	 * @param destination destination to which resources will be copied
	 * @param subMonitor a progress monitor for showing progress and for cancelation
	 */
	protected void copy(IResource[] resources, IPath destination, IProgressMonitor subMonitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath = destination.append(source.getName());
			IWorkspace workspace = source.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			if (source.getType() == IResource.FOLDER && workspaceRoot.exists(destinationPath)) {
				// the resource is a folder and it exists in the destination, copy the
				// children of the folder
				IResource[] children = ((IContainer) source).members();
				copy(children, destinationPath, subMonitor);
			} else {
				// if we're merging folders, we could be overwriting an existing file
				IResource existing = workspaceRoot.findMember(destinationPath);
				
				if (existing != null) {					
					copyExisting(source, existing, subMonitor);
				} else {
					source.copy(destinationPath, IResource.SHALLOW, new SubProgressMonitor(subMonitor, 0));
				}
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
	}
	/**
	 * Sets the content of the existing file to the source file content.
	 * 
	 * @param source source file to copy
	 * @param existing existing file to set the source content in
	 * @param subMonitor a progress monitor for showing progress and for cancelation
	 * @throws CoreException setContents failed
	 */
	private void copyExisting(IResource source, IResource existing, IProgressMonitor subMonitor) throws CoreException {
		IFile existingFile = getFile(existing);

		if (existingFile != null) {
			IFile sourceFile = getFile(source);

			if (sourceFile != null) {
				existingFile.setContents(sourceFile.getContents(), IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
			}
		}
	}
	/**
	 * Copies the given resources to the destination. 
	 * 
	 * @param resources the resources to copy
	 * @param destination destination to which resources will be copied
	 */
	public IResource[] copyResources(final IResource[] resources, IContainer destination) {
		final IPath destinationPath = destination.getFullPath();
		final IResource[][] copiedResources = new IResource[1][0];

		// test resources for existence separate from validate API.
		// Validate is performance critical and resource exists
		// check is potentially slow. Fixes bugs 16129/28602. 
		IStatus resourceStatus = checkExist(resources);
		if (resourceStatus.getSeverity() != IStatus.OK) {
			ErrorDialog.openError(
				parentShell, 
				getProblemsTitle(),
				null, // no special message
				resourceStatus);
			return copiedResources[0];
		}
		String errorMsg = validateDestination(destination, resources);
		if (errorMsg != null) {
			displayError(errorMsg);
			return copiedResources[0];
		}

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
				IResource[] copyResources = resources;

				// Checks only required if this is an exisiting container path.
				monitor.beginTask(
					WorkbenchMessages.getString("CopyFilesAndFoldersOperation.operationTitle"), //$NON-NLS-1$
					100);
					monitor.worked(10); // show some initial progress
				boolean copyWithAutoRename = false;
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				if (root.exists(destinationPath)) {
					IContainer container = (IContainer) root.findMember(destinationPath);
					// If we're copying to the source container then perform
					// auto-renames on all resources to avoid name collisions.
					if (isDestinationSameAsSource(copyResources, container) && canPerformAutoRename()) {
						copyWithAutoRename = true;
					} else {
						// If no auto-renaming will be happening, check for
						// potential name collisions at the target resource
						copyResources = validateNoNameCollisions(container, copyResources, monitor);
						if (copyResources == null) {
							if (canceled)
								return;
							displayError(WorkbenchMessages.getString("CopyFilesAndFoldersOperation.nameCollision")); //$NON-NLS-1$
							return;
						}
						if (validateEdit(container, copyResources) == false)
							return;
					}
				}

				errorStatus = null;
				if (copyResources.length > 0) {
					if (copyWithAutoRename)
						performCopyWithAutoRename(copyResources, destinationPath, monitor);
					else
						performCopy(copyResources, destinationPath, monitor);
				}
				copiedResources[0] = copyResources;
			}
		};

		try {
			new ProgressMonitorDialog(parentShell).run(true, true, op);
		} catch (InterruptedException e) {
			return copiedResources[0];
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(StatusUtil.newStatus(
					IStatus.ERROR, 
					MessageFormat.format(
						"Exception in {0}.performCopy(): {1}", //$NON-NLS-1$
						new Object[] {getClass().getName(), e.getTargetException()}), 
					null));
			displayError(WorkbenchMessages.format(
				"CopyFilesAndFoldersOperation.internalError", //$NON-NLS-1$
				new Object[] { e.getTargetException().getMessage()}));
		}

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
			ErrorDialog.openError(
				parentShell, 
				getProblemsTitle(), 
				null, // no special message
				errorStatus);
			errorStatus = null;
		}
		return copiedResources[0];
	}
	/**
	 * Copies the given files and folders to the destination. 
	 * 
	 * @param fileNames names of the files to copy
	 * @param destination destination to which files will be copied
	 */
	public void copyFiles(final String[] fileNames, IContainer destination) {
		alwaysOverwrite = false;

		// test files for existence separate from validate API 
		// because an external file may not exist until the copy actually 
		// takes place (e.g., WinZip contents).
		IStatus fileStatus = checkExist(fileNames);
		if (fileStatus.getSeverity() != IStatus.OK) {
			ErrorDialog.openError(
				parentShell, 
				getProblemsTitle(),
				null, // no special message
				fileStatus);
			return;
		}
		String errorMsg = validateImportDestination(destination, fileNames);
		if (errorMsg != null) {
			displayError(errorMsg);
			return;
		}
		final IPath destinationPath = destination.getFullPath();

		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) {
				// Checks only required if this is an exisiting container path.
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				if (root.exists(destinationPath)) {
					IContainer container = (IContainer) root.findMember(destinationPath);
				
					performFileImport(getFiles(fileNames), container, monitor);
				}
			}
		};
		try {
			new ProgressMonitorDialog(parentShell).run(true, true, op);
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected above, but unexpected runtime exceptions and errors may still occur.
			Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(StatusUtil.newStatus(IStatus.ERROR, MessageFormat.format("Exception in {0}.performCopy(): {1}", //$NON-NLS-1$
			new Object[] { getClass().getName(), e.getTargetException()}), null));
			displayError(WorkbenchMessages.format("CopyFilesAndFoldersOperation.internalError", new Object[] { e.getTargetException().getMessage()})); //$NON-NLS-1$
		}

		// If errors occurred, open an Error dialog
		if (errorStatus != null) {
			ErrorDialog.openError(parentShell, getProblemsTitle(), //$NON-NLS-1$
			null, // no special message
			errorStatus);
			errorStatus = null;
		}
	}
	/**
	 * Creates a file or folder handle for the source resource as if 
	 * it were to be created in the destination container.
	 * 
	 * @param destination destination container
	 * @param source source resource
	 * @return IResource file or folder handle, depending on the source 
	 * 	type.
	 */
	IResource createLinkedResourceHandle(IContainer destination, IResource source) {
		IWorkspace workspace = destination.getWorkspace();
		IWorkspaceRoot workspaceRoot = workspace.getRoot();
		IPath linkPath = destination.getFullPath().append(source.getName());
		IResource linkHandle;
		
		if (source.getType() == IResource.FOLDER) {
			linkHandle = workspaceRoot.getFolder(linkPath);
		}
		else {
			linkHandle = workspaceRoot.getFile(linkPath);
		}
		return linkHandle;
	}
	/**
	 * Removes the given resource from the workspace. 
	 *  
	 * @param resource resource to remove from the workspace
	 * @param monitor a progress monitor for showing progress and for cancelation
	 * @return 
	 * 	true the resource was deleted successfully
	 * 	false the resource was not deleted because a CoreException occurred
	 */
	boolean delete(IResource resource, IProgressMonitor monitor) throws CoreException {
		boolean force = false; // don't force deletion of out-of-sync resources

		if (resource.getType() == IResource.PROJECT) {
			// if it's a project, ask whether content should be deleted too
			IProject project = (IProject) resource;
			try {
				project.delete(true, force, monitor);
			} catch (CoreException e) {
				recordError(e); // log error
				return false;
			}
		} else {
			// if it's not a project, just delete it
			int flags = IResource.KEEP_HISTORY;
			if (force) {
				flags = flags | IResource.FORCE;
			}
			try {
				resource.delete(flags, monitor);
			} catch (CoreException e) {
				recordError(e); // log error
				return false;
			}				
		}
		return true;
	}
	/**
	 * Opens an error dialog to display the given message.
	 *
	 * @param message the error message to show
	 */
	private void displayError(final String message) {
		parentShell.getDisplay().syncExec(new Runnable() {
			public void run() {
				MessageDialog.openError(parentShell, getProblemsTitle(), message);
			}
		});
	}
	/**
	 * Returns the resource either casted to or adapted to an IFile. 
	 * 
	 * @param resource resource to cast/adapt
	 * @return the resource either casted to or adapted to an IFile.
	 * 	<code>null</code> if the resource does not adapt to IFile 
	 */
	protected IFile getFile(IResource resource) {
		if (resource instanceof IFile) {
			return (IFile) resource;
		}
		if (resource instanceof IAdaptable) {
			return (IFile) ((IAdaptable) resource).getAdapter(IFile.class);
		}
		return null;
	}
	/**
	 * Returns java.io.File objects for the given file names.
	 * 
	 * @param fileNames files to return File object for.
	 * @return java.io.File objects for the given file names.
	 */
	protected File[] getFiles(String[] fileNames) {
		File[] files = new File[fileNames.length];
		
		for (int i = 0; i < fileNames.length; i++) {
			files[i] = new File(fileNames[i]);
		}
		return files;
	}
	/**
	 * Returns the resource either casted to or adapted to an IFolder. 
	 * 
	 * @param resource resource to cast/adapt
	 * @return the resource either casted to or adapted to an IFolder.
	 * 	<code>null</code> if the resource does not adapt to IFolder 
	 */
	protected IFolder getFolder(IResource resource) {
		if (resource instanceof IFolder) {
			return (IFolder) resource;
		}
		if (resource instanceof IAdaptable) {
			return (IFolder) ((IAdaptable) resource).getAdapter(IFolder.class);
		}
		return null;
	}
	/**
	 * Returns a new name for a copy of the resource at the given path in the 
	 * given workspace.
	 *
	 * @param originalName the full path of the resource
	 * @param workspace the workspace
	 * @return the new full path for the copy, or <code>null</code> if the 
	 * 	resource should not be copied
	 */
	private IPath getNewNameFor(final IPath originalName, final IWorkspace workspace) {
		final IResource resource = workspace.getRoot().findMember(originalName);
		final IPath prefix = resource.getFullPath().removeLastSegments(1);
		final String returnValue[] = { "" }; //$NON-NLS-1$

		parentShell.getDisplay().syncExec(new Runnable() {
			public void run() {
				IInputValidator validator = new IInputValidator() {
					public String isValid(String string) {
						if (resource.getName().equals(string)) {
							return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.nameMustBeDifferent"); //$NON-NLS-1$
						}
						IStatus status = workspace.validateName(string, resource.getType());
						if (!status.isOK()) {
							return status.getMessage();
						}
						if (workspace.getRoot().exists(prefix.append(string))) {
							return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.nameExists"); //$NON-NLS-1$
						}
						return null;
					}
				};

				InputDialog dialog = new InputDialog(parentShell, WorkbenchMessages.getString("CopyFilesAndFoldersOperation.inputDialogTitle"), //$NON-NLS-1$
				WorkbenchMessages.format("CopyFilesAndFoldersOperation.inputDialogMessage", new String[] { resource.getName()}), //$NON-NLS-1$
				getAutoNewNameFor(originalName, workspace).lastSegment().toString(), validator);
				dialog.setBlockOnOpen(true);
				dialog.open();
				if (dialog.getReturnCode() == Window.CANCEL) {
					returnValue[0] = null;
				} else {
					returnValue[0] = dialog.getValue();
				}
			}
		});
		if (returnValue[0] == null) {
			throw new OperationCanceledException();
		}
		return prefix.append(returnValue[0]);
	}
	/**
	 * Returns the message for this operation's problems dialog.
	 *
	 * @return the problems message
	 */
	protected String getProblemsMessage() {
		return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.problemMessage"); //$NON-NLS-1$
	}
	/**
	 * Returns the title for this operation's problems dialog.
	 *
	 * @return the problems dialog title
	 */
	protected String getProblemsTitle() {
		return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.copyFailedTitle"); //$NON-NLS-1$
	}
	/**
	 * Returns the rejected files based on the given multi status.
	 *  
	 * @param multiStatus multi status to use to determine file rejection
	 * @param files source files
	 * @return list of rejected files as absolute paths. Object type IPath.
	 */
	private ArrayList getRejectedFiles(IStatus multiStatus, IFile[] files) {
		ArrayList rejectedFiles = new ArrayList();

		IStatus[] status = multiStatus.getChildren();
		for (int i = 0; i < status.length; i++) {
			if (status[i].isOK() == false) {
				rejectedFiles.add(files[i].getFullPath());
			}
		}
		return rejectedFiles;
	}
	/**
	 * Returns whether the source file in a destination collision
	 * will be validateEdited together with the collision itself.
	 * Returns false. Should return true if the source file is to be
	 * deleted after the operation.
	 * 
	 * @return boolean <code>true</code> if the source file in a 
	 * 	destination collision should be validateEdited. 
	 *	<code>false</code> if only the destination should be validated.  
	 */
	protected boolean getValidateConflictSource() {
		return false;
	}
	/**
	 * Returns whether the given resource is accessible.
	 * Files and folders are always considered accessible and a project is 
	 * accessible if it is open.
	 *
	 * @param resource the resource
	 * @return <code>true</code> if the resource is accessible, and 
	 *   <code>false</code> if it is not
	 */
	private boolean isAccessible(IResource resource) {
		switch (resource.getType()) {
			case IResource.FILE :
				return true;
			case IResource.FOLDER :
				return true;
			case IResource.PROJECT :
				return ((IProject) resource).isOpen();
			default :
				return false;
		}
	}
	/**
	 * Returns whether any of the given source resources are being 
	 * recopied to their current container.
	 *
	 * @param sourceResources the source resources 
	 * @param destination the destination container
	 * @return <code>true</code> if at least one of the given source 
	 *   resource's parent container is the same as the destination 
	 */
	boolean isDestinationSameAsSource(IResource[] sourceResources, IContainer destination) {
		IPath destinationLocation = destination.getLocation();
		
		for (int i = 0; i < sourceResources.length; i++) {
			IResource sourceResource = sourceResources[i]; 
			if (sourceResource.getParent().equals(destination)) {
				return true;
			} else if (destinationLocation != null) {
				// do thorough check to catch linked resources. Fixes bug 29913.
				IPath sourceLocation = sourceResource.getLocation();
				IPath destinationResource = destinationLocation.append(sourceResource.getName());
				if (sourceLocation != null && sourceLocation.isPrefixOf(destinationResource)) {
					return true;
				}
			}
		}
		return false;
	}
	/**
	 * Copies the given resources to the destination container with 
	 * the given name.
	 * <p>
	 * Note: the destination container may need to be created prior to 
	 * copying the resources.
	 * </p>
	 *
	 * @param resources the resources to copy
	 * @param destination the path of the destination container
	 * @param monitor a progress monitor for showing progress and for cancelation
	 * @return <code>true</code> if the copy operation completed without 
	 * 	errors
	 */
	private boolean performCopy(IResource[] resources, IPath destination, IProgressMonitor monitor) {
		try {
			monitor.subTask(WorkbenchMessages.getString("CopyFilesAndFoldersOperation.copying")); //$NON-NLS-1$
			ContainerGenerator generator = new ContainerGenerator(destination);
			generator.generateContainer(new SubProgressMonitor(monitor, 10));
			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 75);
			copy(resources, destination, subMonitor);
		} catch (CoreException e) {
			recordError(e); // log error
			return false;
		} finally {
			monitor.done();
		}
		return true;
	}

	/**
	 * Individually copies the given resources to the specified destination
	 * container checking for name collisions. If a collision is detected, 
	 * it is saved with a new name. 
	 * <p>
	 * Note: the destination container may need to be created prior to 
	 * copying the resources.
	 * </p>
	 *
	 * @param resources the resources to copy
	 * @param destination the path of the destination container
	 * @return <code>true</code> if the copy operation completed without errors.
	 */
	private boolean performCopyWithAutoRename(IResource[] resources, IPath destination, IProgressMonitor monitor) {
		IWorkspace workspace = resources[0].getWorkspace();

		try {
			ContainerGenerator generator = new ContainerGenerator(destination);
			generator.generateContainer(new SubProgressMonitor(monitor, 10));

			IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 75);
			subMonitor.beginTask(
				WorkbenchMessages.getString(
					"CopyFilesAndFoldersOperation.copying"), //$NON-NLS-1$
					resources.length);

			for (int i = 0; i < resources.length; i++) {
				IResource source = resources[i];
				IPath destinationPath = destination.append(source.getName());

				if (workspace.getRoot().exists(destinationPath)) {
					destinationPath = getNewNameFor(destinationPath, workspace);
				}
				if (destinationPath != null) {
					try {
						source.copy(destinationPath, IResource.SHALLOW, new SubProgressMonitor(subMonitor, 0));
					} catch (CoreException e) {
						recordError(e); // log error
						return false;
					}
				}
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		} catch (CoreException e) {
			recordError(e); // log error
			return false;
		} finally {
			monitor.done();
		}

		return true;
	}
	/**
	 * Performs an import of the given files into the provided container.
	 * Returns a status indicating if the import was successful.
	 * 
	 * @param files files that are to be imported
	 * @param target container to which the import will be done
	 * @param monitor a progress monitor for showing progress and for cancelation
	 */
	private void performFileImport(File[] files, IContainer target, IProgressMonitor monitor) {
		IOverwriteQuery query = new IOverwriteQuery() {
			public String queryOverwrite(String pathString) {
				if (alwaysOverwrite)
					return ALL;

				final String returnCode[] = { CANCEL };
				final String msg = WorkbenchMessages.format("CopyFilesAndFoldersOperation.overwriteQuestion", new Object[] { pathString }); //$NON-NLS-1$
				final String[] options =
					{
						IDialogConstants.YES_LABEL,
						IDialogConstants.YES_TO_ALL_LABEL,
						IDialogConstants.NO_LABEL,
						IDialogConstants.CANCEL_LABEL };
				parentShell.getDisplay().syncExec(new Runnable() {
					public void run() {
						MessageDialog dialog = new MessageDialog(parentShell, WorkbenchMessages.getString("CopyFilesAndFoldersOperation.question"), null, msg, MessageDialog.QUESTION, options, 0); //$NON-NLS-1$
						dialog.open();
						int returnVal = dialog.getReturnCode();
						String[] returnCodes = { YES, ALL, NO, CANCEL };
						returnCode[0] = returnVal == -1 ? CANCEL : returnCodes[returnVal];
					}
				});
				if (returnCode[0] == ALL) {
					alwaysOverwrite = true;
				} else if (returnCode[0] == CANCEL) {
					canceled = true;
				}
				return returnCode[0];
			}
		};

		ImportOperation op =
			new ImportOperation(
				target.getFullPath(),
				null,
				FileSystemStructureProvider.INSTANCE,
				query,
				Arrays.asList(files));
		op.setContext(parentShell);
		op.setCreateContainerStructure(false);
		try {
			op.run(monitor);
		} catch (InterruptedException e) {
			return;
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				final IStatus status = ((CoreException) e.getTargetException()).getStatus();
				parentShell.getDisplay().syncExec(new Runnable() {
					public void run() {
						ErrorDialog.openError(parentShell, WorkbenchMessages.getString("CopyFilesAndFoldersOperation.importErrorDialogTitle"), //$NON-NLS-1$
						null, // no special message
						status);
					}
				});
			} else {
				// CoreExceptions are handled above, but unexpected runtime exceptions and errors may still occur.
				Platform.getPlugin(PlatformUI.PLUGIN_ID).getLog().log(StatusUtil.newStatus(IStatus.ERROR, MessageFormat.format("Exception in {0}.performFileImport(): {1}", //$NON-NLS-1$
				new Object[] { getClass().getName(), e.getTargetException()}), null));
				displayError(WorkbenchMessages.format("CopyFilesAndFoldersOperation.internalError", //$NON-NLS-1$
				new Object[] { e.getTargetException().getMessage()}));
			}
			return;
		}
		// Special case since ImportOperation doesn't throw a CoreException on
		// failure.
		IStatus status = op.getStatus();
		if (!status.isOK()) {
			if (errorStatus == null)
				errorStatus = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.ERROR, getProblemsMessage(), null); //$NON-NLS-1$
			errorStatus.merge(status);
		}
	}
	/**
	 * Records the core exception to be displayed to the user
	 * once the action is finished.
	 *
	 * @param error a <code>CoreException</code>
	 */
	private void recordError(CoreException error) {
		if (errorStatus == null)
			errorStatus = new MultiStatus(PlatformUI.PLUGIN_ID, IStatus.ERROR, getProblemsMessage(), error); //$NON-NLS-1$

		errorStatus.merge(error.getStatus());
	}
	/**
	 * Checks whether the destination is valid for copying the source 
	 * resources.
	 * <p>
	 * Note this method is for internal use only. It is not API.
	 * </p>
	 *
	 * @param destination the destination container
	 * @param sourceResources the source resources
	 * @return an error message, or <code>null</code> if the path is valid
	 */
	public String validateDestination(IContainer destination, IResource[] sourceResources) {
		if (!isAccessible(destination)) {
			return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.destinationAccessError"); //$NON-NLS-1$
		}
		String destinationMessage = validateDestinationLocation(destination);
		if (destinationMessage != null) {
			return destinationMessage;
		}
		
		IPath destinationLocation = destination.getLocation();
		for (int i = 0; i < sourceResources.length; i++) {
			IResource sourceResource = sourceResources[i];
			IPath sourceLocation = sourceResource.getLocation();

			if (sourceLocation == null) {
				if (sourceResource.isLinked()) {
					// Don't allow copying linked resources with undefined path 
					// variables. See bug 28754.
					return WorkbenchMessages.format(
						"CopyFilesAndFoldersOperation.missingPathVariable",		//$NON-NLS-1$
						new Object[] {sourceResource.getName()});				
				}
				else {
					return WorkbenchMessages.format(
						"CopyFilesAndFoldersOperation.resourceDeleted",		//$NON-NLS-1$
						new Object[] {sourceResource.getName()});				
				}
			}			
			if (sourceLocation != null) {
				if (sourceLocation.equals(destinationLocation)) {
					return WorkbenchMessages.format(
						"CopyFilesAndFoldersOperation.sameSourceAndDest", 	//$NON-NLS-1$
						new Object[] {sourceResource.getName()});
				}
				// is the source a parent of the destination?
				if (sourceLocation.isPrefixOf(destinationLocation)) {
					return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.destinationDescendentError"); //$NON-NLS-1$
				}
			}
			String linkedResourceMessage = validateLinkedResource(destination, sourceResource);
			if (linkedResourceMessage != null) {
				return linkedResourceMessage;
			}
		}
		return null;
	}
	/**
	 * Validates whether the destination location exists.
	 * Linked resources created on undefined path variables have
	 * an undefined location. 
	 * 
	 * @param destination destination container
	 * @return error message or null if destination location is 
	 * 	valid (non-<code>null</code>)
	 */
	private String validateDestinationLocation(IContainer destination) {
		IPath destinationLocation = destination.getLocation();
		
		if (destinationLocation == null) {
			if (destination.isLinked()) {
				return WorkbenchMessages.format(
					"CopyFilesAndFoldersOperation.missingPathVariable",		//$NON-NLS-1$
					new Object[] {destination.getName()});				
			}
			else {
				return WorkbenchMessages.format(
					"CopyFilesAndFoldersOperation.resourceDeleted",			//$NON-NLS-1$
					new Object[] {destination.getName()});				
			}
		}
		return null;		
	}
	/**
	 * Validates that the given source resources can be copied to the 
	 * destination as decided by the VCM provider.
	 * 
	 * @param destination copy destination
	 * @param sourceResources source resources
	 * @return <code>true</code> all files passed validation or there 
	 * 	were no files to validate. <code>false</code> one or more files
	 * 	did not pass validation. 
	 */
	private boolean validateEdit(IContainer destination, IResource[] sourceResources) {
		ArrayList copyFiles = new ArrayList();
		ArrayList rejectedFiles = new ArrayList();
		
		collectExistingReadonlyFiles(destination.getFullPath(), sourceResources, copyFiles);
		if (copyFiles.size() > 0) {
			IFile[] files = (IFile[]) copyFiles.toArray(new IFile[copyFiles.size()]);
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateEdit(files, parentShell);
			
			canceled = status.isOK() == false;
			return status.isOK();
		}
		return true;
	}
	/**
	 * Checks whether the destination is valid for copying the source 
	 * files.
	 * <p>
	 * Note this method is for internal use only. It is not API.
	 * </p>
	 *
	 * @param destination the destination container
	 * @param sourceNames the source file names
	 * @return an error message, or <code>null</code> if the path is valid
	 */
	public String validateImportDestination(IContainer destination, String[] sourceNames) {
		if (!isAccessible(destination)) {
			return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.destinationAccessError"); //$NON-NLS-1$
		}
		String destinationMessage = validateDestinationLocation(destination);
		if (destinationMessage != null) {
			return destinationMessage;
		}
		// work around bug 16202. revert when fixed.
		IPath destinationPath = destination.getLocation();
		File destinationFile = destinationPath.toFile();		
		for (int i = 0; i < sourceNames.length; i++) {
			IPath sourcePath = new Path(sourceNames[i]);
			File sourceFile = sourcePath.toFile();
			File sourceParentFile = sourcePath.removeLastSegments(1).toFile();			
			if (sourceFile != null) {
				if (destinationFile.compareTo(sourceFile) == 0 || 
					(sourceParentFile != null && destinationFile.compareTo(sourceParentFile) == 0)) {
					return WorkbenchMessages.format("CopyFilesAndFoldersOperation.importSameSourceAndDest", //$NON-NLS-1$
					new Object[] {sourceFile.getName()});
				}
				// work around bug 16202. replacement for sourcePath.isPrefixOf(destinationPath)
				IPath destinationParent = destinationPath.removeLastSegments(1);
				while (destinationParent.isEmpty() == false && destinationParent.isRoot() == false) {
					destinationFile = destinationParent.toFile();
					if (sourceFile.compareTo(destinationFile) == 0) {
						return WorkbenchMessages.getString("CopyFilesAndFoldersOperation.destinationDescendentError"); //$NON-NLS-1$
					}
					destinationParent = destinationParent.removeLastSegments(1);
				}
			}
		}
		return null;
	}
	/**
	 * Check if the destination is valid for the given source resource. 
	 * 
	 * @param destination destination container of the operation
	 * @param source source resource
	 * @return String error message or null if the destination is valid
	 */
	private String validateLinkedResource(IContainer destination, IResource source) {
		if (source.isLinked() == false) {
			return null;
		}
		IWorkspace workspace = destination.getWorkspace();
		IResource linkHandle = createLinkedResourceHandle(destination, source);
		IStatus locationStatus = workspace.validateLinkLocation(linkHandle,	source.getRawLocation());
		
		if (locationStatus.getSeverity() == IStatus.ERROR) {
			return locationStatus.getMessage();
		}
		IPath sourceLocation = source.getLocation();
		if (source.getProject().equals(destination.getProject()) == false &&
			source.getType() == IResource.FOLDER &&
			sourceLocation != null) {
			// prevent merging linked folders that point to the same
			// file system folder 
			try {
				IResource[] members = destination.members();
				for (int j = 0; j < members.length; j++) {
					if (sourceLocation.equals(members[j].getLocation()) && 
						source.getName().equals(members[j].getName())) {
						return WorkbenchMessages.format(
							"CopyFilesAndFoldersOperation.sameSourceAndDest", //$NON-NLS-1$
							new Object[] {source.getName()});
					}
				}
			}
			catch (CoreException exception) {
				displayError(WorkbenchMessages.format(
					"CopyFilesAndFoldersOperation.internalError", 				//$NON-NLS-1$
					new Object[] {exception.getMessage()}));
			}
		}
		return null;
	}
	/**
	 * Returns whether moving all of the given source resources to the given
	 * destination container could be done without causing name collisions.
	 * 
	 * @param destination the destination container
	 * @param sourceResources the list of resources 
	 * @param monitor a progress monitor for showing progress and for 
	 * 	cancelation
	 * @return <code>true</code> if there would be no name collisions, and
	 *   <code>false</code> if there would
	 */
	private IResource[] validateNoNameCollisions(
		IContainer destination,
		IResource[] sourceResources,
		IProgressMonitor monitor) {
		List copyItems = new ArrayList();
		IWorkspaceRoot workspaceRoot = destination.getWorkspace().getRoot();
		int overwrite = IDialogConstants.NO_ID;

		// Check to see if we would be overwriting a parent folder.
		// Cancel entire copy operation if we do.
		for (int i = 0; i < sourceResources.length; i++) {
			final IResource sourceResource = sourceResources[i];
			final IPath destinationPath = destination.getFullPath().append(sourceResource.getName());
			final IPath sourcePath = sourceResource.getFullPath();

			IResource newResource = workspaceRoot.findMember(destinationPath);
			if (newResource != null && destinationPath.isPrefixOf(sourcePath)) {
				//Run it inside of a runnable to make sure we get to parent off of the shell as we are not
				//in the UI thread.
				Runnable notice = new Runnable() {
					public void run() {
						MessageDialog.openError(
							parentShell, 
							WorkbenchMessages.getString("CopyFilesAndFoldersOperation.overwriteProblemTitle"), //$NON-NLS-1$
							WorkbenchMessages.format(
								"CopyFilesAndFoldersOperation.overwriteProblem", //$NON-NLS-1$
								new Object[] {destinationPath, sourcePath}
							)
						);
					}
				};
				parentShell.getDisplay().syncExec(notice);
				canceled = true;
				return null;
			}
		}
		// Check for overwrite conflicts
		for (int i = 0; i < sourceResources.length; i++) {
			final IResource sourceResource = sourceResources[i];
			final IPath destinationPath = destination.getFullPath().append(sourceResource.getName());

			IResource newResource = workspaceRoot.findMember(destinationPath);
			if (newResource != null) {
				if (overwrite != IDialogConstants.YES_TO_ALL_ID) {
					overwrite = checkOverwrite(parentShell, newResource);
				}
				if (overwrite == IDialogConstants.YES_ID || overwrite == IDialogConstants.YES_TO_ALL_ID) {
					copyItems.add(sourceResource);
				} else if (overwrite == IDialogConstants.CANCEL_ID) {
					canceled = true;
					return null;
				}
			} else {
				copyItems.add(sourceResource);
			}
		}
		return (IResource[]) copyItems.toArray(new IResource[copyItems.size()]);
	}
}