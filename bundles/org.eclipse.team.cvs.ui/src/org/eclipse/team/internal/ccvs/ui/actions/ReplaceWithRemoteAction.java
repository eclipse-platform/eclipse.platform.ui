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
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

public class ReplaceWithRemoteAction extends WorkspaceAction {
	public void execute(IAction action)  throws InvocationTargetException, InterruptedException {
		
		run(new WorkspaceModifyOperation() {
			public void execute(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor = Policy.monitorFor(monitor);
					monitor.beginTask(null, 100);					
					IResource resources[] = checkOverwriteOfDirtyResources(getSelectedResources(), new InfiniteSubProgressMonitor(monitor, 20));
					if(resources.length > 0) {
						performReplace(resources, Policy.subMonitorFor(monitor, 80));
					}
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, true /* cancelable */, PROGRESS_DIALOG);
	}
	
	protected void performReplace(IResource[] resources, IProgressMonitor monitor) throws TeamException {
		try {
			Hashtable table = getProviderMapping(resources);
			Set keySet = table.keySet();
			monitor.beginTask(null, keySet.size() * 10); //$NON-NLS-1$
			monitor.setTaskName(Policy.bind("ReplaceWithRemoteAction.replacing")); //$NON-NLS-1$
			Iterator iterator = keySet.iterator();
			while (iterator.hasNext()) {
				IProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
				CVSTeamProvider provider = (CVSTeamProvider)iterator.next();
				List list = (List)table.get(provider);
				IResource[] providerResources = (IResource[])list.toArray(new IResource[list.size()]);
				provider.get(providerResources, IResource.DEPTH_INFINITE, subMonitor);
			}
		} finally {
			monitor.done();
		}
	}
	
	protected IPromptCondition getPromptCondition(IResource[] dirtyResources) {
		return getOverwriteLocalChangesPrompt(dirtyResources);
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
