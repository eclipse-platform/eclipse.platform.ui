/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.actions;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
import org.eclipse.team.internal.ccvs.ui.HistoryView;
import org.eclipse.team.internal.ccvs.ui.Policy;

public class ShowResourceInHistoryAction extends WorkspaceAction {
	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void execute(IAction action) throws InterruptedException, InvocationTargetException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				IResource[] resources = getSelectedResources();
				if (resources.length != 1) return;
				HistoryView view = HistoryView.openInActivePerspective();
				if (view != null) {
					view.showHistory(resources[0]);
				}
			}
		}, false /* cancelable */, this.PROGRESS_BUSYCURSOR);
	}
	/*
	 * @see TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		// Show Resource In History is enabled for resources which have been committed.
		IResource[] resources = getSelectedResources();
		if (resources.length != 1) return false;
		IResource resource = resources[0];
		if (!(resource instanceof IFile)) return false;
		ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
		if (!cvsResource.isManaged()) return false;
		if (cvsResource.getSyncInfo().isAdded()) return false;
		return true;
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ShowHistoryAction.showHistory"); //$NON-NLS-1$
	}

}