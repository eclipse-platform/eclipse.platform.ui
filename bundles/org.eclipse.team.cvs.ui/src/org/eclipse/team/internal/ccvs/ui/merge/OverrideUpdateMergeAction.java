package org.eclipse.team.internal.ccvs.ui.merge;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.ui.IHelpContextIds;
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

}
