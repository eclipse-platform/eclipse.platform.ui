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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.ui.*;
import org.eclipse.team.ui.synchronize.*;
import org.eclipse.ui.IWorkbench;

/**
 * The wizard for synchronizing a synchronize participant.
 * 
 * @since 3.0
 */
public class GlobalSynchronizeWizard extends Wizard {

	protected IWorkbench workbench;
	protected IWizard wizard;
	protected GlobalRefreshParticipantSelectionPage mainPage;
	protected ISynchronizeParticipantReference participant;
	private String pluginId = TeamUIPlugin.PLUGIN_ID;

	public GlobalSynchronizeWizard() {
		setWindowTitle(Policy.bind("GlobalSynchronizeWizard.11")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE));
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(false);
	}
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		ISynchronizeParticipantReference[] participants = getParticipants();
		if (participants.length == 1) {
			// If there is only one wizard, skip the first page.
			// Only skip the first page if the one wizard has at least one
			// page.
			try {
				participant = participants[0];
				ISynchronizeParticipant p = participant.getParticipant();
				IWizard wizard = p.createSynchronizeWizard();
				wizard.addPages();
				if (wizard.getPageCount() > 0) {
					wizard.setContainer(getContainer());
					IWizardPage[] pages = wizard.getPages();
					for (int i = 0; i < pages.length; i++) {
						addPage(pages[i]);
					}
					return;
				}
			} catch (TeamException e) {
				Utils.handle(e);
				return;
			}	
		}
		mainPage = new GlobalRefreshParticipantSelectionPage();
		addPage(mainPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#dispose()
	 */
	public void dispose() {
		super.dispose();
		ISynchronizeParticipantReference participant = getSelectedParticipant();
	}	
	
	public IWizardPage getNextPage(IWizardPage page) {
		return super.getNextPage(page);
	}
	
	public boolean canFinish() {
		// If we are on the first page, never allow finish unless the selected
		// wizard has no pages.
		if (getContainer().getCurrentPage() == mainPage) {
			if (mainPage.getSelectedWizard() != null && mainPage.getNextPage() == null) {
				return true;
			}
			return false;
		}
		if (wizard != null) {
			return wizard.canFinish();
		}
		return super.canFinish();
	}

	/*
	 * @see Wizard#performFinish
	 */
	public boolean performFinish() {
		// There is only one wizard with at least one page
		if (wizard != null) {
			TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT, getSelectedParticipant().getId());
			return wizard.performFinish();
		}
		// If we are on the first page and the selected wizard has no pages then allow it to finish.
		if (getContainer().getCurrentPage() == mainPage) {
			IWizard noPageWizard = mainPage.getSelectedWizard();
			if (noPageWizard != null) {
				if (noPageWizard.canFinish()) {
					TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT, getSelectedParticipant().getId());
					return noPageWizard.performFinish();
				}
			}
		}
		// If the wizard has pages and there are several
		// wizards registered then the registered wizard
		// will call it's own performFinish().
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.SYNCHRONIZING_DEFAULT_PARTICIPANT, getSelectedParticipant().getId());
		return true;
	}

	protected ISynchronizeParticipantReference[] getParticipants() {
		List participants = new ArrayList();
		ISynchronizeManager manager = (ISynchronizeManager) TeamUI.getSynchronizeManager();
		ISynchronizeParticipantReference[] desciptors = manager.getSynchronizeParticipants();
		for (int i = 0; i < desciptors.length; i++) {
			ISynchronizeParticipantReference descriptor = desciptors[i];
			if (descriptor.getDescriptor().isGlobalSynchronize()) {
				participants.add(descriptor);
			}
		}
		return (ISynchronizeParticipantReference[]) participants.toArray(new ISynchronizeParticipantReference[participants.size()]);
	}
	
	protected ISynchronizeParticipantReference getSelectedParticipant() {
		if(participant == null && mainPage != null) {
			return mainPage.getSelectedParticipant();
		} else {
			return participant;
		}
	}	
}
