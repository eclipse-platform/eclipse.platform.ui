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
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.team.IMoveDeleteHook;
import org.eclipse.core.resources.team.IResourceTree;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.ui.IWorkbenchWindow;

public class CVSMoveDeleteHook implements IMoveDeleteHook {

	public interface IRunnableWithShell {
		public void run(Shell shell);
	}
	
	public class MoveDeleteMessageDialog extends MessageDialog {
		Button dontShowAgain;
		boolean dontShow;
		boolean showOption;
		
		public MoveDeleteMessageDialog(Shell shell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
			this(shell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex, true);
		}
		
		public MoveDeleteMessageDialog(Shell shell, String dialogTitle, Image dialogTitleImage, String dialogMessage, int dialogImageType, String[] dialogButtonLabels, int defaultIndex, boolean showOption) {
			super(shell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels, defaultIndex);
			this.showOption = showOption;
		}
		
		protected Control createCustomArea(Composite composite) {
			if ( ! showOption) return null;
			dontShow = false;
			dontShowAgain = new Button(composite, SWT.CHECK);
			GridData data = new GridData();
			data.horizontalIndent = 50;
			dontShowAgain.setLayoutData(data);
			dontShowAgain.setText(Policy.bind("CVSMoveDeleteHook.dontShowAgain")); //$NON-NLS-1$
			dontShowAgain.setSelection(dontShow);
			dontShowAgain.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					dontShow = dontShowAgain.getSelection();
				}
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

			});
			return dontShowAgain;
		}
		
		public boolean isDontShowAgain() {
			return dontShow;
		}
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
	
	private void makeFileOutgoingDeletion(IResourceTree tree, IFile source, IFile destination, int updateFlags, IProgressMonitor monitor) throws CoreException {
		if (destination == null) {
			tree.standardDeleteFile(source, updateFlags, monitor);
		} else {
			tree.standardMoveFile(source, destination, updateFlags, monitor);
		}
	}
	
	/*
	 * Delete the files contained in the folder structure rooted at source.
	 * Return true if at least one file was deleted
	 */
	private boolean makeFolderOutgoingDeletion(final IResourceTree tree, final IFolder source, final IFolder destination, final int updateFlags, final IProgressMonitor monitor) throws CoreException {
		final boolean[] fileFound = new boolean[] {false};
		source.accept(new IResourceVisitor() {
			public boolean visit(IResource resource) throws CoreException {
				if (resource.isTeamPrivateMember()) {
					return false;
				}
				// XXX Compensate for above not working
				if (resource.getName().equals("CVS")) {
					return false;
				}
				if (resource.getType() == IResource.FOLDER) {
					if (destination != null) {
						IFolder destFolder = destination.getFolder(resource.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
						destFolder.create(false, true, monitor);
					}
				} else if (resource.getType() == IResource.FILE) {
					fileFound[0] = true;
					IFile destFile = null;
					if (destination != null) {
						destFile = destination.getFile(resource.getFullPath().removeFirstSegments(source.getFullPath().segmentCount()));
					}
					makeFileOutgoingDeletion(tree, (IFile)resource, destFile, updateFlags, monitor);
					return false;
				}
				return true;
			}

		}, IResource.DEPTH_INFINITE, false);
		
		return fileFound[0];
	}
	
	private boolean checkForTeamPrivate(IResource resource) {
		if (resource.isTeamPrivateMember()) {
			showDialog(new IRunnableWithShell() {
				public void run(Shell shell) {
					ErrorDialog.openError(shell, "Team Private Resource", "Deletion of team private resources is not permitted", null);
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
		if (cvsFile.isManaged()) {
			// prompt the user for choices: Mark as outgoing deletion or cancel
			final boolean[] performDelete = new boolean[] { ! CVSProviderPlugin.getPlugin().getPromptOnFileDelete()};
			if ( ! performDelete[0]){
				showDialog(new IRunnableWithShell() {
					public void run(Shell shell) {
						MoveDeleteMessageDialog dialog = new MoveDeleteMessageDialog(
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
		} else // XXX Until team-private resources are hiddent
			if (source.getParent().getName().equals("CVS")) {
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
						MoveDeleteMessageDialog dialog = new MoveDeleteMessageDialog(
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
					if ( ! makeFolderOutgoingDeletion(tree, source, destination, updateFlags, monitor) && ! dialogShown && destination == null) {
						showDialog(new IRunnableWithShell() {
							public void run(Shell shell) {
								MoveDeleteMessageDialog dialog = new MoveDeleteMessageDialog(
									shell,
									title, 
									null,	// accept the default window icon
									message, 
									MessageDialog.QUESTION, 
									new String[] {IDialogConstants.OK_LABEL}, 
									0, false); 	// yes is the default
								performDelete[0] = dialog.open() == 0;
								if (performDelete[0]) {
									CVSProviderPlugin.getPlugin().setPromptOnFolderDelete( ! dialog.isDontShowAgain());
									CVSUIPlugin.getPlugin().getPreferenceStore().setValue(ICVSUIConstants.PREF_PROMPT_ON_FOLDER_DELETE, CVSProviderPlugin.getPlugin().getPromptOnFolderDelete());
								}
							}
						});
					}
				} catch (final CoreException e) {
					showDialog(new IRunnableWithShell() {
						public void run(Shell shell) {
							ErrorDialog.openError(shell, null, null, e.getStatus());
						}
					});
				}
			}
			return true;
		} else // XXX Until team-private resources are hidden
			if (source.getName().equals("CVS")) {
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
			Policy.bind("CVSMoveDeleteHook.deleteFileMessage", file.getName()), //$NON-NLS-1$
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
			Policy.bind("CVSMoveDeleteHook.deleteFolderMessage", folder.getName()), //$NON-NLS-1$
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
			Policy.bind("CVSMoveDeleteHook.moveFileMessage", source.getName()), //$NON-NLS-1$
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
			Policy.bind("CVSMoveDeleteHook.moveFolderMessage", source.getName()), //$NON-NLS-1$
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
