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
package org.eclipse.team.ui.synchronize.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

/**
 * Action to remove the given participant from the synchronize manager.
 * @since 3.0 
 */
public class RemoveSynchronizeParticipantAction extends Action {
	private ISynchronizeParticipant participant;

	/**
	 * Creates the action to remove the participant from the synchronize
	 * manager.
	 * @param participant the participant to remove from the synchronize
	 * manager.
	 */
	public RemoveSynchronizeParticipantAction(ISynchronizeParticipant participant) {
		this.participant = participant;
		Utils.initAction(this, "action.removePage.", Policy.getBundle()); //$NON-NLS-1$
	}
	
	public void run() {
		TeamUI.getSynchronizeManager().removeSynchronizeParticipants(
				new ISynchronizeParticipant[] {participant});
	}
}
