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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ui.IPromptCondition;
import org.eclipse.team.internal.ui.PromptingDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ReplaceWithRemoteAction extends WorkspaceAction {
	public void execute(IAction action)  throws InvocationTargetException, InterruptedException {
		PromptingDialog dialog = new PromptingDialog(getShell(), getSelectedResources(), 
			getPromptCondition(), Policy.bind("ReplaceWithAction.confirmOverwrite"));//$NON-NLS-1$
		final IResource[] resources = dialog.promptForMultiple();
		if(resources.length == 0) {
			// nothing to do
			return;
		}
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					// Do the replace
					Hashtable table = getProviderMapping(resources);
					Set keySet = table.keySet();
					monitor.beginTask(null, keySet.size() * 1000); //$NON-NLS-1$
					monitor.setTaskName(Policy.bind("ReplaceWithRemoteAction.replacing")); //$NON-NLS-1$
					Iterator iterator = keySet.iterator();
					while (iterator.hasNext()) {
						IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1000);
						CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
						List list = (List)table.get(provider);
						IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
						provider.get(providerResources, IResource.DEPTH_INFINITE, subMonitor);
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}

	/**
	 * Note: This method is designed to be overridden by test cases.
	 */
	protected IPromptCondition getPromptCondition() {
		return getOverwriteLocalChangesPrompt();
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#getErrorTitle()
	 */
	protected String getErrorTitle() {
		return Policy.bind("ReplaceWithRemoteAction.problemMessage"); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForAddedResources()
	 */
	protected boolean isEnabledForAddedResources() {
		return false;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		if (super.isEnabledForCVSResource(cvsResource)) {
			// Don't enable if there are sticky file revisions in the lineup
			if (!cvsResource.isFolder()) {
				ResourceSyncInfo info = cvsResource.getSyncInfo();
				if (info != null && info.getTag() != null) {
					String revision = info.getRevision();
					String tag = info.getTag().getName();
					if (revision.equals(tag)) return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	/* 
	 * Update the text label for the action based on the tags in the
	 * selection.
	 * 
	 * @see TeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
	 */
	protected void setActionEnablement(IAction action) {
		super.setActionEnablement(action);
		
		action.setText(calculateActionTagValue());
	}
}
