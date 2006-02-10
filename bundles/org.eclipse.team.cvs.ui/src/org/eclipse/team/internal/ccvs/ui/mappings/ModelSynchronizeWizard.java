/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.core.subscribers.SubscriberScopeManager;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.wizards.CheckoutWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

public class ModelSynchronizeWizard extends ModelParticipantWizard {

	public ModelSynchronizeWizard() {
		super();
	}

	protected ISynchronizeParticipant createParticipant(ResourceMapping[] selectedMappings) {
		ISynchronizationScopeManager manager = new SubscriberScopeManager(
				CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().getName(), selectedMappings, 
				CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber(), true);
		WorkspaceModelParticipant p =  new WorkspaceModelParticipant( 
				WorkspaceSubscriberContext.createContext(manager, ISynchronizationContext.THREE_WAY));
		return p;
	}

	protected String getPageTitle() {
		ISynchronizeParticipantDescriptor desc = TeamUI.getSynchronizeManager().getParticipantDescriptor(WorkspaceModelParticipant.ID);
		if(desc != null) {
			return desc.getName();
		} else {
			return CVSUIMessages.CVSSynchronizeWizard_0; 
		}
	}

	protected IWizard getImportWizard() {
		return new CheckoutWizard();
	}

	protected IResource[] getRootResources() {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber().roots();
	}

}
