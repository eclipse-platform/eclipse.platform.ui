/*******************************************************************************
 * Copyright (c) 2000, 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.sync;
 
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;
import org.eclipse.team.internal.ui.sync.SyncView;

public class ForceUpdateSyncAction extends UpdateSyncAction {

	public ForceUpdateSyncAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}
	
	protected boolean isEnabled(ITeamNode node) {
		// The force update action is enabled only for conflicting and outgoing changes
		SyncSet set = new SyncSet(new StructuredSelection(node));
		if (syncMode == SyncView.SYNC_INCOMING) {
			return (set.hasConflicts() && hasRealChanges(node, new int[] { ITeamNode.CONFLICTING }));
		} else {
			return ((set.hasOutgoingChanges() || set.hasConflicts()) && hasRealChanges(node, new int[] { ITeamNode.CONFLICTING, ITeamNode.OUTGOING }));
		}
	}

	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeIncomingNodes();
		if (syncMode != SyncView.SYNC_BOTH) {
			set.removeOutgoingNodes();
		}
	}
	protected boolean promptForConflicts() {
		return true;
	}
	/**
	 * @see MergeAction#getHelpContextID()
	 */
	protected String getHelpContextID() {
		return IHelpContextIds.SYNC_FORCED_UPDATE_ACTION;
	}

}
