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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
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
	protected ISynchronizeParticipant participant;
	private String pluginId = TeamUIPlugin.PLUGIN_ID;

	public GlobalSynchronizeWizard() {
		setWindowTitle(Policy.bind("GlobalSynchronizeWizard.11")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE));
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(false);
	}

	public GlobalSynchronizeWizard(ISynchronizeParticipant participant) {
		this();
		this.participant = participant;
	}
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		ISynchronizeParticipant[] participants = getParticipants();
		if (participants.length == 1) {
			// If there is only one wizard, skip the first page.
			// Only skip the first page if the one wizard has at least one
			// page.
			participant = participants[0];
		}
		if (participant != null) {
			wizard = participants[0].createSynchronizeWizard();
			addWizardPages(participant.createSynchronizeWizard());
		} else {
			mainPage = new GlobalRefreshParticipantSelectionPage();
			addPage(mainPage);
		}
	}

	private void addWizardPages(IWizard wizard) {
		wizard.addPages();
		if (wizard.getPageCount() > 0) {
			wizard.setContainer(getContainer());
			IWizardPage[] pages = wizard.getPages();
			for (int i = 0; i < pages.length; i++) {
				addPage(pages[i]);
			}
		}
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

	protected ISynchronizeParticipant[] getParticipants() {
		List participants = new ArrayList();
		ISynchronizeManager manager = (ISynchronizeManager) TeamUI.getSynchronizeManager();
		ISynchronizeParticipant[] desciptors = manager.getSynchronizeParticipants();
		for (int i = 0; i < desciptors.length; i++) {
			ISynchronizeParticipant descriptor = desciptors[i];
			if (descriptor.doesSupportSynchronize()) {
				participants.add(descriptor);
			}
		}
		return (ISynchronizeParticipant[]) participants.toArray(new ISynchronizeParticipant[participants.size()]);
	}
	
	protected ISynchronizeParticipant getSelectedParticipant() {
		if(participant == null && mainPage != null) {
			return mainPage.getSelectedParticipant();
		} else {
			return participant;
		}
	}
}
