/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.history.IFileHistoryProvider;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.*;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.history.DialogHistoryPageSite;
import org.eclipse.team.ui.SaveablePartDialog;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.history.*;
import org.eclipse.ui.part.Page;

/**
 * Compare with revision will allow a user to browse the history of a file, compare with the
 * other revisions and merge changes from other revisions into the workspace copy.
 */
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
			MessageDialog.openWarning(getShell(), CVSUIMessages.CompareWithRevisionAction_noRevisions, CVSUIMessages.CompareWithRevisionAction_noRevisionsLong); // 
			return;
		}
		
		// Fetch the log entries
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					monitor.beginTask(CVSUIMessages.CompareWithRevisionAction_fetching, 100); 
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
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				IFile tempFile = (IFile) getSelectedResources()[0];
				IFileHistoryProvider fileHistoryProvider = RepositoryProvider.getProvider(tempFile.getProject()).getFileHistoryProvider();
				Object tempPageSource = Platform.getAdapterManager().getAdapter(fileHistoryProvider, IHistoryPageSource.class);
				
				if(CVSUIPlugin.getPlugin().getPreferenceStore().getBoolean(ICVSUIConstants.PREF_SHOW_COMPARE_REVISION_IN_DIALOG)) {
					HistoryPageSaveablePart input = null;
					if (tempPageSource instanceof IHistoryPageSource) {
						IHistoryPageSource pageSource = (IHistoryPageSource) tempPageSource;
						Page histPage = pageSource.createPage(tempFile);
						DialogHistoryPageSite dialogHistorySite = new DialogHistoryPageSite(getTargetPart().getSite(), false);
						((IHistoryPage)histPage).setSite(dialogHistorySite);
						((IHistoryPage)histPage).showHistory(tempFile, true);
						CompareConfiguration cc = new CompareConfiguration();
						cc.setLeftEditable(false);
						cc.setRightEditable(false);
						input = new HistoryPageSaveablePart(getShell(),cc,(IHistoryPage)histPage);
					}
					SaveablePartDialog cd = createCompareDialog(getShell(), input);
					cd.setBlockOnOpen(true);
					cd.open();
				} else {
					TeamUI.getHistoryView().showHistoryFor((IFile)getSelectedResources()[0]);
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
	}
	
	/**
	 * Return the compare dialog to use to show the compare input.
	 */
	protected SaveablePartDialog createCompareDialog(Shell shell, HistoryPageSaveablePart input) {
		return new SaveablePartDialog(shell, input); 
	}
	
	/**
	 * Return the text describing this action
	 */
	protected String getActionTitle() {
		return CVSUIMessages.CompareWithRevisionAction_4; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return CVSUIMessages.CompareWithRevisionAction_compare; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return (!cvsResource.isFolder() && super.isEnabledForCVSResource(cvsResource));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForMultipleResources()
	 */
	protected boolean isEnabledForMultipleResources() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}
}
