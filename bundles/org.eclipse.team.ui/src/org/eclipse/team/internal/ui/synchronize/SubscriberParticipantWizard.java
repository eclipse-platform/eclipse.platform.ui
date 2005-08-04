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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.*;
import org.eclipse.osgi.util.NLS;
import org.eclipse.team.internal.ui.ITeamUIImages;
import org.eclipse.team.internal.ui.TeamUIMessages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.*;

/**
 * This is the class registered with the org.eclipse.team.ui.synchronizeWizard
 */
public abstract class SubscriberParticipantWizard extends Wizard {

	private GlobalRefreshResourceSelectionPage selectionPage;
	private IWizard importWizard;
	
	public SubscriberParticipantWizard() {
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_WIZBAN_SHARE));
		setNeedsProgressMonitor(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getWindowTitle()
	 */
	public String getWindowTitle() {
		return TeamUIMessages.GlobalRefreshSubscriberPage_0; 
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		if (getRootResources().length == 0) {
			importWizard = getImportWizard();
			importWizard.setContainer(getContainer());
			importWizard.addPages();
			IWizardPage startingPage = importWizard.getStartingPage();
			if (startingPage != null) {
				startingPage.setTitle(NLS.bind(TeamUIMessages.SubscriberParticipantWizard_0, new String[] { getName() })); 
				startingPage.setDescription(NLS.bind(TeamUIMessages.SubscriberParticipantWizard_1, new String[] { importWizard.getWindowTitle() })); 
			}
		} else {
			selectionPage = new GlobalRefreshResourceSelectionPage(getRootResources());
			selectionPage.setTitle(NLS.bind(TeamUIMessages.GlobalRefreshSubscriberPage_1, new String[] { getName() })); 
			selectionPage.setMessage(TeamUIMessages.GlobalRefreshSubscriberPage_2); 
			addPage(selectionPage);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		if (importWizard != null) {
			return importWizard.performFinish();
		} else {
			IResource[] resources = selectionPage.getRootResources();
			if (resources != null && resources.length > 0) {
				SubscriberParticipant participant = createParticipant(selectionPage.getSynchronizeScope());
				TeamUI.getSynchronizeManager().addSynchronizeParticipants(new ISynchronizeParticipant[]{participant});
				// We don't know in which site to show progress because a participant could actually be shown in multiple sites.
				participant.run(null /* no site */);
			}
			return true;
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		if(importWizard != null ) {
			return importWizard.getNextPage(page);
		}
		return super.getNextPage(page);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performCancel()
	 */
	public boolean performCancel() {
		if(importWizard != null) {
			return importWizard.performCancel();
		}
		return super.performCancel();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	public boolean canFinish() {
		if(importWizard != null) {
			return importWizard.canFinish();
		}
		return super.canFinish();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getStartingPage()
	 */
	public IWizardPage getStartingPage() {
		if(importWizard != null) {
			return importWizard.getStartingPage();
		}
		return super.getStartingPage();
	}

	protected abstract IResource[] getRootResources();
	
	protected abstract SubscriberParticipant createParticipant(ISynchronizeScope scope);

	protected abstract String getName();
	
	protected abstract IWizard getImportWizard();
}
