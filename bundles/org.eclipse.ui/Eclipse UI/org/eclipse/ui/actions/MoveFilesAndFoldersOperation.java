package org.eclipse.ui.actions;

/*
 * Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
 * This file is made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.internal.WorkbenchMessages;

/**
 * Moves files and folders.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
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
	 * @param monitor a progress monitor for showing progress and for cancelation
	 */
	protected void copy(IResource[] resources, IPath destination, IProgressMonitor subMonitor) throws CoreException {
		for (int i = 0; i < resources.length; i++) {
			IResource source = resources[i];
			IPath destinationPath = destination.append(source.getName());
			IWorkspace workspace = source.getWorkspace();
			IWorkspaceRoot workspaceRoot = workspace.getRoot();
			boolean isFolder = source.getType() == IResource.FOLDER;
			boolean exists = workspaceRoot.exists(destinationPath);
			if (isFolder && exists) {
				// the resource is a folder and it exists in the destination, copy the
				// children of the folder
				IResource[] children = ((IContainer) source).members();
				copy(children, destinationPath, subMonitor);
				// need to explicitly delete the folder since we're not moving it
				delete(source, subMonitor);
			} else {
				// if we're merging folders, we could be overwriting an existing file
				IResource existing = workspaceRoot.findMember(destinationPath);
				if (existing != null) {
					delete(existing, subMonitor);
				}
				source.move(destinationPath, IResource.KEEP_HISTORY, new SubProgressMonitor(subMonitor, 0));
				subMonitor.worked(1);
				if (subMonitor.isCanceled()) {
					throw new OperationCanceledException();
				}
			}
		}
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
	/* (non-Javadoc)
	 * Overrides method in CopyFilesAndFoldersOperation
	 *
	 * Note this method is for internal use only. It is not API.
	 *
	 */
	public String validateDestination(IContainer destination, IResource[] sourceResources) {
		for (int i = 0; i < sourceResources.length; i++) {
			IResource sourceResource = sourceResources[i];

			// is the source being copied onto itself?
			if (sourceResource.getParent().equals(destination)) {
				return WorkbenchMessages.format(
					"MoveFilesAndFoldersOperation.sameSourceAndDest", //$NON-NLS-1$
					new Object[] {sourceResource.getName()});
			}
		}
		return super.validateDestination(destination, sourceResources);
	}
}