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
package org.eclipse.team.internal.ccvs.ui.actions;
 
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.operations.ReplaceOperation;
import org.eclipse.team.internal.core.InfiniteSubProgressMonitor;
import org.eclipse.team.internal.ui.dialogs.IPromptCondition;

public class ReplaceWithRemoteAction extends WorkspaceAction {
	public void execute(IAction action)  throws InvocationTargetException, InterruptedException {
		
		final IResource[][] resources = new IResource[][] {null};
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InterruptedException, InvocationTargetException {
				try {
					monitor = Policy.monitorFor(monitor);
					monitor.beginTask(null, 100);					
					resources[0] = checkOverwriteOfDirtyResources(getSelectedResources(), new InfiniteSubProgressMonitor(monitor, 100));
				} catch (TeamException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		}, false /* cancelable */, PROGRESS_BUSYCURSOR);
		
		if (resources[0] == null || resources[0].length == 0) return;
		
		// Peform the replace in the background
		new ReplaceOperation(getTargetPart(), resources[0], null, true).run();
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
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
