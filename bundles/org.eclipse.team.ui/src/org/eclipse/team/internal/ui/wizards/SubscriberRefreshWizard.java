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
package org.eclipse.team.internal.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.synchronize.subscriber.SubscriberParticipant;

public class SubscriberRefreshWizard extends Wizard {

	private SubscriberParticipant participant;
	private GlobalRefreshResourceSelectionPage selectionPage;

	public SubscriberRefreshWizard(SubscriberParticipant participant) {
		this.participant = participant;	
		setWindowTitle(Policy.bind("SubscriberRefreshWizard.0") + participant.getName()); //$NON-NLS-1$
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		selectionPage = new GlobalRefreshResourceSelectionPage(participant);
		addPage(selectionPage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		IResource[] resources = selectionPage.getSelectedResources();
		if(resources != null) {
			participant.refresh(resources);
		}
		return true;
	}
}
