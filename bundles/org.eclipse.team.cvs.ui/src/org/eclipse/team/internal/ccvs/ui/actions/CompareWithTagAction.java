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
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.Policy;
import org.eclipse.team.internal.ccvs.ui.TagSelectionDialog;
import org.eclipse.team.internal.ccvs.ui.subscriber.CompareParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class CompareWithTagAction extends WorkspaceAction {

	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
		final IResource[] resources = getSelectedResources();
		CVSTag tag = promptForTag(resources);
		if (tag == null)
			return;
		
		// Run the comparison
		CVSCompareSubscriber s = new CVSCompareSubscriber(resources, tag);
		CompareParticipant participant = new CompareParticipant(s);
		
		ISynchronizePageConfiguration configuration = participant.createPageConfiguration();
		configuration.setProperty(ISynchronizePageConfiguration.P_TOOLBAR_MENU, new String[] { 
				ISynchronizePageConfiguration.NAVIGATE_GROUP, 
				ISynchronizePageConfiguration.MODE_GROUP, 
				ISynchronizePageConfiguration.LAYOUT_GROUP });
		configuration.setProperty(ISynchronizePageConfiguration.P_OBJECT_CONTRIBUTION_ID, CVSCompareSubscriber.ID_MODAL);
			
		participant.refreshInDialog(
						getShell(),
						resources,
						Policy.bind("Participant.comparing"),
						configuration,
						null);
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