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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.core.subscribers.FilteredSyncInfoCollector;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;

/**
 * This action shows the CVS workspace participant into a model dialog. For single file
 * selection, the compare editor is shown instead.
 * 
 * @since 3.0
 */
public class CompareWithRemoteAction extends WorkspaceAction {
	
	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		
		SyncInfoFilter contentComparison = new SyncInfoFilter() {
			private SyncInfoFilter contentCompare = new SyncInfoFilter.ContentComparisonSyncInfoFilter();
			public boolean select(SyncInfo info, IProgressMonitor monitor) {
				// Want to select infos whose contents do not match
				for (int i = 0; i < resources.length; i++) {
					IResource resource = resources[i];
					if (resource.getFullPath().isPrefixOf(info.getLocal().getFullPath())) {
						return !contentCompare.select(info, monitor);
					}
				}
				return false;
			}
		};
		
		// Show the 3-way comparison in a model dialog
		WorkspaceSynchronizeParticipant participant = CVSUIPlugin.getPlugin().getCvsWorkspaceSynchronizeParticipant();
		SyncInfoTree syncInfoSet = new SyncInfoTree();
		
		FilteredSyncInfoCollector collector = new FilteredSyncInfoCollector(
				participant.getSubscriberSyncInfoCollector().getSubscriberSyncInfoSet(), 
				syncInfoSet, 
				contentComparison);	
		collector.start(new NullProgressMonitor());
		participant.refresh(resources, participant.getRefreshListeners().createModalDialogListener(getShell(), participant.getId(), participant, syncInfoSet), Policy.bind("Participant.comparing"), null); //$NON-NLS-1$
	}
	
	/*
	 * Update the text label for the action based on the tags in the selection.
	 * @see TeamAction#setActionEnablement(org.eclipse.jface.action.IAction)
	 */
	protected void setActionEnablement(IAction action) {
		super.setActionEnablement(action);
		action.setText(calculateActionTagValue());
	}
	
	/**
	 * Enable for resources that are managed (using super) or whose parent is a CVS folder.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForCVSResource(org.eclipse.team.internal.ccvs.core.ICVSResource)
	 */
	protected boolean isEnabledForCVSResource(ICVSResource cvsResource) throws CVSException {
		return super.isEnabledForCVSResource(cvsResource) || cvsResource.getParent().isCVSFolder();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
}
