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
package org.eclipse.team.examples.filesystem.ui;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

/**
 * Action to synchronize the selected resources. This results
 * in a file-system participant being added to the synchronize view.
 */
public class SynchronizeAction extends FileSystemAction {
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IResource[] resources = getSelectedResources();
		// First check if there is an existing matching participant
		FileSystemSynchronizeParticipant participant = (FileSystemSynchronizeParticipant)SubscriberParticipant.getMatchingParticipant(FileSystemSynchronizeParticipant.ID, resources);
		// If there isn't, create one and add to the manager
		if (participant == null) {
			participant = new FileSystemSynchronizeParticipant(new ResourceScope(resources));
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
		}
		participant.refresh(resources, null, null, getTargetPart().getSite());

	}

}
