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
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareUI;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteFile;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ILogEntry;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.CVSCompareRevisionsInput;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class CompareWithRevisionAction extends WorkspaceAction {
	
	/**
	 * Returns the selected remote file
	 */
	protected ICVSRemoteFile getSelectedRemoteFile() {
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return null;
		if (!(resources[0] instanceof IFile)) return null;
		IFile file = (IFile)resources[0];
		try {
			return (ICVSRemoteFile)CVSWorkspaceRoot.getRemoteResourceFor(file);
		} catch (TeamException e) {
			handle(e, null, null);
			return null;
		}
	}

	/*
	 * @see CVSAction#execute(IAction)
	 */
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		
		// Setup holders
		final ICVSRemoteFile[] file = new ICVSRemoteFile[] { null };
		final ILogEntry[][] entries = new ILogEntry[][] { null };
		
		// Get the selected file
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				file[0] = getSelectedRemoteFile();
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		
		if (file[0] == null) {
			// No revisions for selected file
			MessageDialog.openWarning(getShell(), Policy.bind("CompareWithRevisionAction.noRevisions"), Policy.bind("CompareWithRevisionAction.noRevisionsLong")); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		// Fetch the log entries
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(Policy.bind("CompareWithRevisionAction.fetching"), 100); //$NON-NLS-1$
					entries[0] = file[0].getLogEntries(Policy.subMonitorFor(monitor, 100));
					monitor.done();
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
		
		if (entries[0] == null) return;
		
		// Show the compare viewer
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				CompareUI.openCompareEditorOnPage(
				  new CVSCompareRevisionsInput((IFile)getSelectedResources()[0], entries[0]),
				  getTargetPage());
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("CompareWithRevisionAction.compare"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return (!cvsResource.isFolder() && super.isEnabledForCVSResource(cvsResource));
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

}
