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
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ui.sync.ITeamNode;
import org.eclipse.team.internal.ui.sync.SyncSet;

/**
 * Override ForceCommitSyncAction to only work on outgoing nodes
 */
public class CommitSyncAction extends ForceCommitSyncAction {
	public CommitSyncAction(CVSSyncCompareInput model, ISelectionProvider sp, String label, Shell shell) {
		super(model, sp, label, shell);
	}

	protected boolean isEnabled(ITeamNode node) {
		// The commit action is enabled only for non-conflicting outgoing changes
		CVSSyncSet set = new CVSSyncSet(new StructuredSelection(node));
		try {
			return set.hasCommitableChanges();
		} catch (CVSException e) {
			CVSUIPlugin.log(e.getStatus());
			return false;
		}
	}
	
	protected void removeNonApplicableNodes(SyncSet set, int syncMode) {
		set.removeConflictingNodes();
		set.removeIncomingNodes();
		((CVSSyncSet)set).removeNonAddedChanges();
	}
}
