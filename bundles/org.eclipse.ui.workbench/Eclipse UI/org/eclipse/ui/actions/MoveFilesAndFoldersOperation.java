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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Moves files and folders.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 2.1
 */
public class MoveFilesAndFoldersOperation extends CopyFilesAndFoldersOperation {

	/** 
	 * Creates a new operation initialized with a shell.
	 * 
	 * @param shell parent shell for error dialogs
	 */
	public MoveFilesAndFoldersOperation(Shell shell) {
		super(shell);
	}
	/**
	 * Returns whether this operation is able to perform on-the-fly 
	 * auto-renaming of resources with name collisions.
	 *
	 * @return <code>true</code> if auto-rename is supported, 
	 * 	and <code>false</code> otherwise
	 */
	protected boolean canPerformAutoRename() {
		return false;
	}
	/**
	 * Moves the resources to the given destination.  This method is 
	 * called recursively to merge folders during folder move.
	 * 
	 * @param resources the resources to move
	 * @param destination destination to which resources will be moved
	 * @param subMonitor a progress monitor for showing progress and for cancelation
	 */
	protected void copy(IResource[] resources, IPath destination, IProgressMonitor subMonitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath = destination.append(source.getName());
			IWorkspace workspace = source.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			IResource existing = workspaceRoot.findMember(destinationPath);
			if (source.getType() == IResource.FOLDER && existing != null) {
				// the resource is a folder and it exists in the destination, move the
				// children of the folder.
				if (homogenousResources(source, existing)) {									
					IResource[] children = ((IContainer) source).members();
					copy(children, destinationPath, subMonitor);
					delete(source, subMonitor);					
				}
				else {
					// delete the destination folder, moving a linked folder
					// over an unlinked one or vice versa. Fixes bug 28772. 
					delete(existing, new SubProgressMonitor(subMonitor, 0));
					source.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));					
				}
			} else {
				// if we're merging folders, we could be overwriting an existing file				
				if (existing != null) {
					if (homogenousResources(source, existing))			
						moveExisting(source, existing, subMonitor);
					else  {
						// Moving a linked resource over unlinked or vice versa.
						// Can't use setContents here. Fixes bug 28772.
						delete(existing, new SubProgressMonitor(subMonitor, 0));
						source.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
					}
				} else {
					source.move(destinationPath, IResource.SHALLOW | IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
				}
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
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
			"CopyFilesAndFoldersOperation.deepMoveQuestion", //$NON-NLS-1$
			new Object[] {source.getFullPath().makeRelative()});
	}
	/**
	 * Returns the task title for this operation's progress dialog.
	 *
	 * @return the task title
	 */
	protected String getOperationTitle() {
		return WorkbenchMessages.getString("MoveFilesAndFoldersOperation.operationTitle"); //$NON-NLS-1$
	}
	/**
	 * Returns the message for this operation's problems dialog.
	 *
	 * @return the problems message
	 */
	protected String getProblemsMessage() {
		return WorkbenchMessages.getString("MoveFilesAndFoldersOperation.problemMessage"); //$NON-NLS-1$
	}
	/**
	 * Returns the title for this operation's problems dialog.
	 *
	 * @return the problems dialog title
	 */
	protected String getProblemsTitle() {
		return WorkbenchMessages.getString("MoveFilesAndFoldersOperation.moveFailedTitle"); //$NON-NLS-1$
	}
	/**
	 * Returns whether the source file in a destination collision
	 * will be validateEdited together with the collision itself.
	 * Returns true.
	 * 
	 * @return boolean <code>true</code>, the source file in a  
	 * 	destination collision should be validateEdited.  
	 */
	protected boolean getValidateConflictSource() {
		return true;
	}
	/**
	 * Sets the content of the existing file to the source file content.
	 * Deletes the source file.
	 * 
	 * @param source source file to move
	 * @param existing existing file to set the source content in
	 * @param subMonitor a progress monitor for showing progress and for cancelation
	 * @throws CoreException setContents failed
	 */
	private void moveExisting(IResource source, IResource existing, IProgressMonitor subMonitor) throws CoreException {
		IFile existingFile = getFile(existing);

		if (existingFile != null) {
			IFile sourceFile = getFile(source);

			if (sourceFile != null) {
				existingFile.setContents(sourceFile.getContents(), IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
				delete(sourceFile, subMonitor);
			}
		}
	}
	/* (non-Javadoc)
	 * Overrides method in CopyFilesAndFoldersOperation
	 *
	 * Note this method is for internal use only. It is not API.
	 *
	 */
	public String validateDestination(IContainer destination, IResource[] sourceResources) {
		IPath destinationLocation = destination.getLocation();
		
		for (int i = 0; i < sourceResources.length; i++) {
			IResource sourceResource = sourceResources[i];

			// is the source being copied onto itself?
			if (sourceResource.getParent().equals(destination)) {
				return WorkbenchMessages.format(
					"MoveFilesAndFoldersOperation.sameSourceAndDest", //$NON-NLS-1$
					new Object[] {sourceResource.getName()});
			}
			// test if linked source is copied onto itself. Fixes bug 29913.
			if (destinationLocation != null) {
				IPath sourceLocation = sourceResource.getLocation();
				IPath destinationResource = destinationLocation.append(sourceResource.getName());
				if (sourceLocation != null && sourceLocation.isPrefixOf(destinationResource)) {
					return WorkbenchMessages.format(
						"MoveFilesAndFoldersOperation.sameSourceAndDest", //$NON-NLS-1$
						new Object[] {sourceResource.getName()});
				}
			}
		}
		return super.validateDestination(destination, sourceResources);
	}
}