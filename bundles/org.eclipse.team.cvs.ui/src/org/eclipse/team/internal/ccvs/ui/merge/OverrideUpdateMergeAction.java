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
package org.eclipse.team.internal.ccvs.ui.merge;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.resources.CVSRemoteSyncElement;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.repo.RepositoryManager;
import org.eclipse.team.internal.ccvs.ui.sync.CVSSyncCompareInput;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;

public class OverrideUpdateMergeAction extends UpdateMergeAction {
	public OverrideUpdateMergeAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}
	/*
	 * Override removeNonApplicableNodes because conflicting nodes should not be removed from this set.
	 */	
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeOutgoingNodes();
		set.removeIncomingNodes();
	}
	protected boolean isEnabled(ITeamNode node) {
		// The force update action is enabled only for conflicting and outgoing changes
		SyncSet set = new SyncSet(new StructuredSelection(node));
		return (set.hasConflicts() && hasRealChanges(node, new int[] { ITeamNode.CONFLICTING }));
	}
	/**
	 * @see MergeAction#getHelpContextID()
	 */
	protected String getHelpContextID() {
		return IHelpContextIds.MERGE_FORCED_UPDATE_ACTION;
	}

	/**
	 * This method is the same as the inherited methd but it does not unmanage
	 * because merging should leave the files as outgoing deletions
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.sync.UpdateSyncAction#runLocalDeletions(ITeamNode[], RepositoryManager, IProgressMonitor)
	 */
	protected void runLocalDeletions(ITeamNode[] nodes, RepositoryManager manager, IProgressMonitor monitor) throws TeamException, CoreException {
		monitor.beginTask(null, nodes.length * 100);
		for (int i = 0; i < nodes.length; i++) {
			ITeamNode node = nodes[i];
			CVSRemoteSyncElement element = CVSSyncCompareInput.getSyncElementFrom(node);
			deleteAndKeepHistory(element.getLocal(), Policy.subMonitorFor(monitor, 100));
		}
		monitor.done();
	}
	
}
