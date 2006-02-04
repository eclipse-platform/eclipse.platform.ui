/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutWizard;
import org.eclipse.team.internal.ui.synchronize.SubscriberParticipantWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

/**
 * This is the class registered with the org.eclipse.team.ui.synchronizeWizard
 */
public class CVSSynchronizeWizard extends SubscriberParticipantWizard {
	
	protected IResource[] getRootResources() {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().roots();
	}

	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SubscriberParticipantWizard#getName()
	 */
	protected String getName() {
		ISynchronizeParticipantDescriptor desc = TeamUI.getSynchronizeManager().getParticipantDescriptor(WorkspaceSynchronizeParticipant.ID);
		if(desc != null) {
			return desc.getName();
		} else {
			return CVSUIMessages.CVSSynchronizeWizard_0; 
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SubscriberParticipantWizard#createParticipant(org.eclipse.core.resources.IResource[])
	 */
	protected SubscriberParticipant createParticipant(ISynchronizeScope scope) {
		// First check if there is an existing matching participant
		IResource[] roots = scope.getRoots();
		if (roots == null) {
			roots = CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().roots();
		}
		WorkspaceSynchronizeParticipant participant = (WorkspaceSynchronizeParticipant)SubscriberParticipant.getMatchingParticipant(WorkspaceSynchronizeParticipant.ID, roots);	
		// If there isn't, create one and add to the manager
		if (participant == null) {
			return new WorkspaceSynchronizeParticipant(scope);
		} else {
			return participant;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.synchronize.SubscriberParticipantWizard#getImportWizard()
	 */
	protected IWizard getImportWizard() {
		return new CheckoutWizard();
	}
}
