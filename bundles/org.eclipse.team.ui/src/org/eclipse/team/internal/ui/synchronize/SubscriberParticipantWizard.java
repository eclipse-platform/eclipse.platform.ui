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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.*;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeScope;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * This is the class registered with the org.eclipse.team.ui.synchronizeWizard
 */
public abstract class SubscriberParticipantWizard extends Wizard {

	private GlobalRefreshResourceSelectionPage selectionPage;

	public SubscriberParticipantWizard() {
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE));
		setNeedsProgressMonitor(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
	 */
	public String getWindowTitle() {
		return Policy.bind("GlobalRefreshSubscriberPage.0"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		selectionPage = new GlobalRefreshResourceSelectionPage(getRootResources());
		selectionPage.setTitle(Policy.bind("GlobalRefreshSubscriberPage.1", getName())); //$NON-NLS-1$
		selectionPage.setMessage(Policy.bind("GlobalRefreshSubscriberPage.2")); //$NON-NLS-1$
		addPage(selectionPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		IResource[] resources = selectionPage.getRootResources();
		if(resources != null && resources.length > 0) {
			SubscriberParticipant participant = createParticipant(selectionPage.getSynchronizeScope());
			TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
			// We don't know in which site to show progress because a participant could actually be shown in multiple sites.
			participant.run(null /* no site */);
		}
		return true;
	}

	protected abstract IResource[] getRootResources();
	
	protected abstract SubscriberParticipant createParticipant(ISynchronizeScope scope);

	protected abstract String getName();
}
