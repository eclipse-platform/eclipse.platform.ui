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
import org.eclipse.jface.action.IAction;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CompareParticipant;
import org.eclipse.team.internal.ccvs.ui.tags.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.tags.TagSource;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

public class CompareWithTagAction extends WorkspaceAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		CVSTag tag = promptForTag(resources);
		if (tag == null)
			return;
		
		CVSCompareSubscriber compareSubscriber = new CVSCompareSubscriber(resources, tag);
		if (SyncAction.isSingleFile(resources)) {
			SyncAction.showSingleFileComparison(getShell(), compareSubscriber, resources[0]);
			compareSubscriber.dispose();
		} else {
			try {
			compareSubscriber.primeRemoteTree();
			} catch(CVSException e) {
				// ignore, the compare will fail if there is a real problem.
			}
			//	First check if there is an existing matching participant, if so then re-use it
			CompareParticipant participant = CompareParticipant.getMatchingParticipant(resources, tag);
			if (participant == null) {
				CVSCompareSubscriber s = compareSubscriber;
				participant = new CompareParticipant(s);
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
			}
			participant.refresh(resources, null, null, null); //$NON-NLS-1$
		}
	}
	
	protected CVSTag promptForTag(IResource[] resources) {
		CVSTag tag = TagSelectionDialog.getTagToCompareWith(getShell(), TagSource.create(resources), TagSelectionDialog.INCLUDE_ALL_TAGS);
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
}
