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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.sync.views.SyncResource;
import org.eclipse.team.ui.sync.SyncResourceSet;

/**
 * This action performs an update for the CVSWorkspaceSubscriber.
 */
public class WorkspaceUpdateAction extends SubscriberUpdateAction {

	// used to indicate how conflicts are to be updated
	private boolean onlyUpdateAutomergeable;

	/*
	 * (non-Javadoc) 
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#performPrompting(org.eclipse.team.ui.sync.SyncResourceSet)
	 */
	protected boolean performPrompting(SyncResourceSet syncSet) {
		// If there are conflicts or outgoing changes in the syncSet, we need to warn the user.
		onlyUpdateAutomergeable = false;
		if (syncSet.hasConflicts() || syncSet.hasOutgoingChanges()) {
			return promptForMergeableConflicts(syncSet);
		}
		return true;
	}
	
	/**
	 * Prompt for mergeable conflicts.
	 * Note: This method is designed to be overridden by test cases.
	 * 
	 * @return 0 to cancel, 1 to only update mergeable conflicts, 2 to overwrite if unmergeable
	 */
	protected boolean promptForMergeableConflicts(final SyncResourceSet syncSet) {
		final int[] result = new int[] {Dialog.CANCEL};
		final Shell shell = getShell();
		shell.getDisplay().syncExec(new Runnable() {
			public void run() {
				UpdateDialog dialog = new UpdateDialog(shell, syncSet);
				result[0] = dialog.open();
				// Need to record the choice so that the update will be performed
				// properly for automergable conflicts
				onlyUpdateAutomergeable = dialog.getAutomerge();
			}
		});
		return (result[0] == Dialog.OK);
	}
	
	/* (non-Javadoc)
	 * 
	 * Return true for conflicting changes that are automergable if the user has chosen the 
	 * appropriate operation.
	 * 
	 * @see org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberUpdateAction#supportsShallowUpdateFor(org.eclipse.team.internal.ui.sync.views.SyncResource)
	 */
	protected boolean supportsShallowUpdateFor(SyncResource changedNode) {
		return (changedNode.getChangeDirection() == SyncInfo.CONFLICTING
			&& ((changedNode.getKind() & SyncInfo.CHANGE_MASK) == SyncInfo.CHANGE)
			&& onlyUpdateAutomergeable 
			&& (changedNode.getKind() & SyncInfo.AUTOMERGE_CONFLICT) != 0);
	}
}
