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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.subscriber.CompareParticipant;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class CompareWithTagAction extends WorkspaceAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		IResource[] resources = getSelectedResources();
		CVSTag tag = promptForTag(resources);
		if (tag == null)
			return;
		IWorkbenchWindow wWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage activePage = null;
		if (wWindow != null) {
			activePage = wWindow.getActivePage();
		}
		// Create the synchronize view participant
		CVSCompareSubscriber s = new CVSCompareSubscriber(resources, tag);
		CompareParticipant participant = (CompareParticipant)SubscriberParticipant.find(s);
		boolean addParticipant = false;
		if(participant == null) {
			participant = new CompareParticipant(s);
			addParticipant = true;
		}
		participant.refreshWithRemote(s.roots(), addParticipant);
	}

	protected CVSTag promptForTag(IResource[] resources) {
		IProject[] projects = new IProject[resources.length];
		for (int i = 0; i < resources.length; i++) {
			projects[i] = resources[i].getProject();
		}
		CVSTag tag = TagSelectionDialog.getTagToCompareWith(getShell(), projects);
		return tag;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.actions.WorkspaceAction#isEnabledForNonExistantResources()
	 */
	protected boolean isEnabledForNonExistantResources() {
		return true;
	}
}