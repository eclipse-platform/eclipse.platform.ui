/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.EditorsAction;

/**
 * IFileModificationValidator that is pluged into the CVS Repository Provider
 */
public class FileModificationValidator implements ICVSFileModificationValidator {

	public static final IStatus OK = new Status(IStatus.OK, CVSUIPlugin.ID, 0, Policy.bind("ok"), null); //$NON-NLS-1$
	
	public FileModificationValidator() {
	}
	
	/**
	 * @see org.eclipse.core.resources.IFileModificationValidator#validateEdit(org.eclipse.core.resources.IFile, java.lang.Object)
	 */
	public IStatus validateEdit(IFile[] files, Object context) {
		IFile[] readOnlyFiles = getManagedReadOnlyFiles(files);
		if (readOnlyFiles.length == 0) return OK;
		return edit(readOnlyFiles, getShell(context));
	}

	/**
	 * @see org.eclipse.core.resources.IFileModificationValidator#validateSave(org.eclipse.core.resources.IFile)
	 */
	public IStatus validateSave(IFile file) {
		if (!needsCheckout(file)) return OK;
		return edit(new IFile[] {file}, (Shell)null);
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.ICVSFileModificationValidator#validateMoveDelete(org.eclipse.core.resources.IFile[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	public IStatus validateMoveDelete(IFile[] files, IProgressMonitor monitor) {
		IFile[] readOnlyFiles = getManagedReadOnlyFiles(files);
		if (readOnlyFiles.length == 0) return OK;

		try {
			edit(readOnlyFiles, monitor);
			return OK;
		} catch (CVSException e) {
			return e.getStatus();
		}
	}
	
	private IFile[] getManagedReadOnlyFiles(IFile[] files) {
		List readOnlys = new ArrayList();
		for (int i = 0; i < files.length; i++) {
			IFile iFile = files[i];
			if (needsCheckout(iFile)) {
				readOnlys.add(iFile);
			}
		}
		return (IFile[]) readOnlys.toArray(new IFile[readOnlys.size()]);
	}
	
	private boolean needsCheckout(IFile file) {
		try {
			if (file.isReadOnly()) {
				ICVSFile cvsFile = CVSWorkspaceRoot.getCVSFileFor(file);
				return cvsFile.isManaged();
			}
		} catch (CVSException e) {
			// Log the exception and assume we don't need a checkout
			CVSUIPlugin.log(e);
		}
		return false;
	}
	
	private CVSTeamProvider getProvider(IFile[] files) {
		CVSTeamProvider provider = (CVSTeamProvider)RepositoryProvider.getProvider(files[0].getProject(), CVSProviderPlugin.getTypeId());
		return provider;
	}
	
	private Shell getShell(Object context) {
		if (context instanceof Shell)
			return (Shell)context;
		return null;
	}

	private IStatus getStatus(InvocationTargetException e) {
		Throwable target = e.getTargetException();
		if (target instanceof TeamException) {
			return ((TeamException) target).getStatus();
		} else if (target instanceof CoreException) {
			return ((CoreException) target).getStatus();
		}
		return new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("internal"), target); //$NON-NLS-1$
	}
		
	private IStatus edit(final IFile[] files, final Shell shell) {
		if (isPerformEdit()) {
			try {
				if (shell != null && !promptToEditFiles(files, shell)) {
					// The user didn't want to edit.
					// OK is returned but the file remains read-only
					throw new InterruptedException();
				}
				
				// Run the edit in a runnable in order to get a busy cursor.
				// This runnable is syncExeced in order to get a busy cursor
				CVSUIPlugin.runWithProgress(shell, false, new IRunnableWithProgress() {
					public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
						try {
							edit(files, monitor);
						} catch (CVSException e) {
							new InvocationTargetException(e);
						}
					}
				}, CVSUIPlugin.PERFORM_SYNC_EXEC);
			} catch (InvocationTargetException e) {
				return getStatus(e);
			} catch (InterruptedException e) {
				// Must return an error to indicate that it is not OK to edit the files
				return new Status(IStatus.CANCEL, CVSUIPlugin.ID, 0, Policy.bind("FileModificationValidator.vetoMessage"), null); //$NON-NLS-1$;
			}
		} else {
			// Allow the files to be edited without notifying the server
			for (int i = 0; i < files.length; i++) {
				IFile file = files[i];
				file.setReadOnly(false);
			}
		}

		return OK;
		
	}

	private boolean promptToEditFiles(IFile[] files, Shell shell) throws InvocationTargetException, InterruptedException {
		if (files.length == 0)
			return true;		

		if(isNeverPrompt())	
			return true;

		// Contact the server to see if anyone else is editing the files
		EditorsAction editors = fetchEditors(files, shell);
		if (editors.isEmpty()) {
			if (isAlwaysPrompt()) 
				return (promptEdit(shell));
			return true;
		} else {
			return (editors.promptToEdit(shell));
		}
	}
	
	private boolean promptEdit(Shell shell) {
		// Open the dialog using a sync exec (there are no guarentees that we
		// were called from the UI thread
		final boolean[] result = new boolean[] { false };
		CVSUIPlugin.openDialog(shell, new CVSUIPlugin.IOpenableInShell() {
			public void open(Shell shell) {
				result[0] = MessageDialog.openQuestion(shell,Policy.bind("FileModificationValidator.3"),Policy.bind("FileModificationValidator.4")); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}, CVSUIPlugin.PERFORM_SYNC_EXEC);
		return result[0];
	}

	private boolean isPerformEdit() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_EDIT.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_ACTION));
	}
	
	private EditorsAction fetchEditors(IFile[] files, Shell shell) throws InvocationTargetException, InterruptedException {
		final EditorsAction editors = new EditorsAction(getProvider(files), files);
		// Fetch the editors in a runnable in order to get the busy cursor
		CVSUIPlugin.runWithProgress(shell, false, new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				editors.run(monitor);
			}
		}, CVSUIPlugin.PERFORM_SYNC_EXEC);
		return editors;
	}

	private boolean isNeverPrompt() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_NEVER.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_PROMPT));
	}

	private boolean isAlwaysPrompt() {
		return ICVSUIConstants.PREF_EDIT_PROMPT_ALWAYS.equals(CVSUIPlugin.getPlugin().getPreferenceStore().getString(ICVSUIConstants.PREF_EDIT_PROMPT));
	}	
	
	private void edit(IFile[] files, IProgressMonitor monitor) throws CVSException {
		getProvider(files).edit(files, false /* recurse */, true /* notify server */, ICVSFile.NO_NOTIFICATION, monitor);
	}
}
