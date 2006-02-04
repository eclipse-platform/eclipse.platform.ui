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
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.team.internal.ui.synchronize.ParticipantSynchronizeWizard;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;

public abstract class ModelParticipantWizard extends ParticipantSynchronizeWizard {

	private ModelElementSelectionPage selectionPage;

	protected WizardPage createScopeSelectionPage() {
		selectionPage = new ModelElementSelectionPage(getRootResources());
		return selectionPage;
	}
	
	protected void createParticipant() {
		ISynchronizeParticipant participant = createParticipant(selectionPage.getSelectedMappings());
		TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
		// We don't know in which site to show progress because a participant could actually be shown in multiple sites.
		participant.run(null /* no site */);
	}

	protected abstract ISynchronizeParticipant createParticipant(ResourceMapping[] selectedMappings);

}
