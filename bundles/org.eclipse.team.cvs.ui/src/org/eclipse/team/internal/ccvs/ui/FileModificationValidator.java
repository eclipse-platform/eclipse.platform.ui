/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSFile;
import org.eclipse.team.internal.ccvs.core.ICVSFileModificationValidator;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.actions.EditorsAction;

/**
 * IFileModificationValidator that is pluged into the CVS Repository Provider
 */
public class FileModificationValidator implements ICVSFileModificationValidator {

	public static final IStatus OK = new Status(IStatus.OK, CVSUIPlugin.ID, 0, Policy.bind("ok"), null); //$NON-NLS-1$
	private static final int HIGHJACK = 1;
	
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
		
	private IStatus edit(final IFile[] files, Shell shell) {
		try {
			if (!promptToEditFiles(files, shell)) {
				return OK;
			}
		} catch (InvocationTargetException e) {
			return getStatus(e);
		} catch (InterruptedException e) {
			// Is it correct to answer OK?????
			return OK;
		}

		// Create a runnable to edit the file
		try {
			run(shell, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						edit(files, monitor);
					} catch (CVSException e) {
						new InvocationTargetException(e);
					}
				}
			});
			return OK;
		} catch (InvocationTargetException e) {
			return getStatus(e);
		} catch (InterruptedException e) {
			return new Status(IStatus.ERROR, CVSUIPlugin.ID, 0, Policy.bind("FileModificationValidator.vetoMessage"), null); //$NON-NLS-1$;
		}
	}

	private boolean promptToEditFiles(IFile[] files, Shell shell) throws InvocationTargetException, InterruptedException {
		if (files.length == 0)
			return true;
		
		EditorsAction editors = new EditorsAction(getProvider(files),files);
		if (editors.isPerformEdit()) {
			// determine if there are any editors of the file registered on the server
			run(shell, editors);
			// prompt if there are
			return editors.promptToEdit(shell);
		} else {
			// Allow the files to be edited without notifying the server
			for (int i = 0; i < files.length; i++) {
				IFile file = files[i];
				file.setReadOnly(false);
			}
			return false;
		}

	}
	
	private void run(Shell shell, final IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException {
		final InvocationTargetException[] exception = new InvocationTargetException[] { null };
		CVSUIPlugin.runWithProgress(shell, false, runnable, CVSUIPlugin.PERFORM_SYNC_EXEC);
	}
	
	private void edit(IFile[] files, IProgressMonitor monitor) throws CVSException {
		getProvider(files).edit(files, false /* recurse */, true /* notify server */, ICVSFile.NO_NOTIFICATION, monitor);
	}
}
