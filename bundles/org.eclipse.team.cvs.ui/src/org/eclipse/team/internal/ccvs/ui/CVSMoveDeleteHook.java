/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.ui.IWorkbenchWindow;

public class CVSMoveDeleteHook implements IMoveDeleteHook {

	public interface IRunnableWithShell {
		public void run(Shell shell);
	}

	private void showDialog(final IRunnableWithShell runnable) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				IWorkbenchWindow window = CVSUIPlugin.getPlugin().getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					runnable.run(window.getShell());
				} else {
					Display display = Display.getCurrent();
					Shell shell = new Shell(display);
					runnable.run(shell);
				}
			}
		});
	}
	
	/*
	 * Delete the file and return true if an outgoing deletion will result
	 * and the parent folder needs to remain 
	 */
	private boolean makeFileOutgoingDeletion(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		// Delete or move the file
		if (destination == null) {
			tree.standardDeleteFile(source, updateFlags, monitor);
		} else {
			tree.standardMoveFile(source, destination, updateFlags, monitor);
		}
		// Indicate whether the parent folder must remain for outgoing deletions
		ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(source);
		boolean mustRemain;
		try {
			mustRemain = (cvsFile.isManaged() && ! cvsFile.getSyncInfo().isAdded());
		} catch (CVSException e) {
			tree.failed(e.getStatus());
			mustRemain = true;
		}
		return mustRemain;
	}
	
	/*
	 * Delete the files contained in the folder structure rooted at source.
	 * Return true if at least one file has been marked as an outgoing deletion and the parent folder must remain
	 */
	private boolean makeFolderOutgoingDeletion(IResourceTree tree, IFolder source, IFolder destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		boolean fileFound = false;

		// Create the destination for a move
		if (destination != null && ! destination.exists()) {
			destination.create(false, true, monitor);
		}
		
		// Move or delete the children
		IResource[] members = source.members();
		for (int i = 0; i < members.length; i++) {
			IResource child = members[i];
			if (child.getType() == IResource.FOLDER) {
				// Determine the corresponding destination folder
				IFolder destFolder = null;
				if (destination != null) {
					destFolder = destination.getFolder(child.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
				}
				
				// Try to delete/move the child
				if (makeFolderOutgoingDeletion(tree, (IFolder)child, destFolder, updateFlags, monitor)) {
					fileFound = true;
					// XXX Below line commented out for now
					// tree.failed(new CVSStatus(IStatus.WARNING, CVSStatus.FOLDER_NEEDED_FOR_FILE_DELETIONS, Policy.bind("CVSMoveDeleteHook.folderDeletionFailure", resource.getFullPath().toString()))); //$NON-NLS-1$
				}
			} else if (child.getType() == IResource.FILE) {
				IFile destFile = null;
				if (destination != null) {
					destFile = destination.getFile(child.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
				}
				fileFound = makeFileOutgoingDeletion(tree, (IFile)child, destFile, updateFlags, monitor);
			}
		}

		// If there were no files, delete the folder
		if ( ! fileFound) {
			try {
				ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(source);
				// We we need to check if the folder already has outgoing deletions
				ICVSFile[] files = folder.getFiles();
				for (int i = 0; i < files.length; i++) {
					ICVSFile cvsFile = files[i];
					if (cvsFile.isManaged() && ! cvsFile.getSyncInfo().isAdded()) {
						fileFound = true;
						break;
					}		
				}
				// If there is still no file, we can delete the folder
				if ( ! fileFound) {
					tree.standardDeleteFolder(source, updateFlags, monitor);
					folder.unmanage(null);
				}
			} catch (CVSException e) {
				tree.failed(e.getStatus());
			}
		}
		return fileFound;
	}
	
	private boolean checkForTeamPrivate(final IResource resource) {
		if (resource.isTeamPrivateMember()) {
			showDialog(new IRunnableWithShell() {
				public void run(Shell shell) {
					ErrorDialog.openError(shell, Policy.bind("CVSMoveDeleteHook.Team_Private_Resource_1"), Policy.bind("CVSMoveDeleteHook.Deletion_of_team_private_resources_is_not_permitted_2", resource.getFullPath().toString()), null); //$NON-NLS-1$ //$NON-NLS-2$
				}
			});
			return true;
		} 
		return false;
	}
	
	private boolean deleteFile(
		final IResourceTree tree,
		final IFile source,
		final IFile destination,
		final int updateFlags,
		final String title,
		final String message,
		final IProgressMonitor monitor) {
		
		if (checkForTeamPrivate(source)) {
			return true;
		}
		
		final ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(source);
		ResourceSyncInfo info = null;
		try {
			info = cvsFile.getSyncInfo();
		} catch (CVSException e) {
		}
		if (info != null && ! info.isAdded()) {
			// prompt the user for choices: Mark as outgoing deletion or cancel
			final boolean[] performDelete = new boolean[] { ! CVSProviderPlugin.getPlugin().getPromptOnFileDelete()};
			if ( ! performDelete[0]){
				showDialog(new IRunnableWithShell() {
					public void run(Shell shell) {
						AvoidableMessageDialog dialog = new AvoidableMessageDialog(
							shell,
							title, 
							null,	// accept the default window icon
							message, 
							MessageDialog.QUESTION, 
							new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
							0); 	// yes is the default
						performDelete[0] = dialog.open() == 0;
						if (performDelete[0]) {
							// The CVS core delta listener will mark the file as an outgoing deletion
							// so we just need to say that we didn't handle it. Core will delete the
							// file and the delta listener will do it's thing.
							CVSProviderPlugin.getPlugin().setPromptOnFileDelete( ! dialog.isDontShowAgain());
							CVSUIPlugin.getPlugin().getPreferenceStore().setValue(ICVSUIConstants.PREF_PROMPT_ON_FILE_DELETE, CVSProviderPlugin.getPlugin().getPromptOnFileDelete());
						}
					}
				});
			}
			if (performDelete[0]) {
				// Issue a delete for all child files
				// The CVS core delta listener will mark each file as an outgoing deletion
				// so we just need to delete each file and let the delta listener do its thing
				try {
					makeFileOutgoingDeletion(tree, source, destination, updateFlags, monitor);
				} catch (final CoreException e) {
					showDialog(new IRunnableWithShell() {
						public void run(Shell shell) {
							ErrorDialog.openError(shell, null, null, e.getStatus());
						}
					});
				}
			}
			return true;
		}
		return false;
	}
	
	private boolean deleteFolder(
		final IResourceTree tree,
		final IFolder source,
		final IFolder destination,
		final int updateFlags,
		final String title,
		final String message,
		final IProgressMonitor monitor) {
		
		if (checkForTeamPrivate(source)) {
			return true;
		}
		
		final ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(source);
		if (cvsFolder.isManaged()) {
			// prompt the user for choices: Mark as outgoing deletion or cancel
			final boolean[] performDelete = new boolean[] { ! CVSProviderPlugin.getPlugin().getPromptOnFolderDelete()};
			boolean dialogShown = false;
			if ( ! performDelete[0]) {
				dialogShown = true;
				showDialog(new IRunnableWithShell() {
					public void run(Shell shell) {
						AvoidableMessageDialog dialog = new AvoidableMessageDialog(
							shell,
							title, 
							null,	// accept the default window icon
							message, 
							MessageDialog.QUESTION, 
							new String[] {IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL}, 
							0); 	// yes is the default
						performDelete[0] = dialog.open() == 0;
						if (performDelete[0]) {
							CVSProviderPlugin.getPlugin().setPromptOnFolderDelete( ! dialog.isDontShowAgain());
							CVSUIPlugin.getPlugin().getPreferenceStore().setValue(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE, CVSProviderPlugin.getPlugin().getPromptOnFolderDelete());
						}
					}
				});
			}
			if (performDelete[0]) {
				// Issue a delete for all child files
				// The CVS core delta listener will mark each file as an outgoing deletion
				// so we just need to delete each file and let the delta listener do its thing
				try {
					makeFolderOutgoingDeletion(tree, source, destination, updateFlags, monitor);
				} catch (final CoreException e) {
					showDialog(new IRunnableWithShell() {
						public void run(Shell shell) {
							ErrorDialog.openError(shell, null, null, e.getStatus());
						}
					});
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * @see IMoveDeleteHook#deleteFile(IResourceTree, IFile, int, IProgressMonitor)
	 */
	public boolean deleteFile(
		final IResourceTree tree,
		final IFile file,
		int updateFlags,
		final IProgressMonitor monitor) {
		
		return deleteFile(tree, file, null, updateFlags, 
			Policy.bind("CVSMoveDeleteHook.deleteFileTitle"), //$NON-NLS-1$
			Policy.bind("CVSMoveDeleteHook.deleteFileMessage", file.getFullPath().toString()), //$NON-NLS-1$
			monitor);
	}

	/**
	 * @see IMoveDeleteHook#deleteFolder(IResourceTree, IFolder, int, IProgressMonitor)
	 */
	public boolean deleteFolder(
		final IResourceTree tree,
		final IFolder folder,
		final int updateFlags,
		final IProgressMonitor monitor) {
		
		return deleteFolder(tree, folder, null, updateFlags,
			Policy.bind("CVSMoveDeleteHook.deleteFolderTitle"), //$NON-NLS-1$
			Policy.bind("CVSMoveDeleteHook.deleteFolderMessage", folder.getFullPath().toString()), //$NON-NLS-1$
			monitor);
	}

	/**
	 * @see IMoveDeleteHook#deleteProject(IResourceTree, IProject, int, IProgressMonitor)
	 */
	public boolean deleteProject(
		IResourceTree tree,
		IProject project,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// No special action needed
		return false;
	}

	/**
	 * @see IMoveDeleteHook#moveFile(IResourceTree, IFile, IFile, int, IProgressMonitor)
	 */
	public boolean moveFile(
		IResourceTree tree,
		IFile source,
		IFile destination,
		int updateFlags,
		IProgressMonitor monitor) {
			
		return deleteFile(tree, source, destination, updateFlags,
			Policy.bind("CVSMoveDeleteHook.moveFileTitle"), //$NON-NLS-1$
			Policy.bind("CVSMoveDeleteHook.moveFileMessage", source.getFullPath().toString()), //$NON-NLS-1$
			monitor);
	}

	/**
	 * @see IMoveDeleteHook#moveFolder(IResourceTree, IFolder, IFolder, int, IProgressMonitor)
	 */
	public boolean moveFolder(
		IResourceTree tree,
		IFolder source,
		IFolder destination,
		int updateFlags,
		IProgressMonitor monitor) {
			
		return deleteFolder(tree, source, destination, updateFlags,
			Policy.bind("CVSMoveDeleteHook.moveFolderTitle"), //$NON-NLS-1$
			Policy.bind("CVSMoveDeleteHook.moveFolderMessage", source.getFullPath().toString()), //$NON-NLS-1$
			monitor);
	}

	/**
	 * @see IMoveDeleteHook#moveProject(IResourceTree, IProject, IProjectDescription, int, IProgressMonitor)
	 */
	public boolean moveProject(
		IResourceTree tree,
		IProject source,
		IProjectDescription description,
		int updateFlags,
		IProgressMonitor monitor) {
			
		// No special action
		return false;
	}
}
