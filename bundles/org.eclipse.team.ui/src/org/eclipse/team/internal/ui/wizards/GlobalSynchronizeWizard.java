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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.*;
import org.eclipse.team.internal.ui.*;
import org.eclipse.team.internal.ui.registry.SynchronizeWizardDescription;
import org.eclipse.team.internal.ui.synchronize.SynchronizeManager;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference;
import org.eclipse.ui.IWorkbench;

/**
 * The wizard for synchronizing a synchronize participant.
 * 
 * @since 3.0
 */
public class GlobalSynchronizeWizard extends Wizard {

	protected IWorkbench workbench;
	protected IWizard wizard;
	protected GlobalRefreshWizardSelectionPage mainPage;
	protected ISynchronizeParticipantReference participant;

	public GlobalSynchronizeWizard() {
		setWindowTitle(Policy.bind("GlobalSynchronizeWizard.11")); //$NON-NLS-1$
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ITeamUIImages.IMG_WIZBAN_SHARE));
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(false);
	}
	
	/*
	 * @see Wizard#addPages
	 */
	public void addPages() {
		SynchronizeWizardDescription[] wizards = getWizards();
		if (wizards.length == 1) {
			// If there is only one wizard, skip the first page.
			// Only skip the first page if the one wizard has at least one
			// page.
			try {
				wizard = wizards[0].createWizard();
				wizard.addPages();
				if (wizard.getPageCount() > 0) {
					wizard.setContainer(getContainer());
					IWizardPage[] pages = wizard.getPages();
					for (int i = 0; i < pages.length; i++) {
						addPage(pages[i]);
					}
					return;
				}
			} catch (CoreException e) {
				Utils.handle(e);
				return;
			}	
		}
		mainPage = new GlobalRefreshWizardSelectionPage();
		addPage(mainPage);
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
			return wizard.performFinish();
		}
		// If we are on the first page and the selected wizard has no pages then allow it to finish.
		if (getContainer().getCurrentPage() == mainPage) {
			IWizard noPageWizard = mainPage.getSelectedWizard();
			if (noPageWizard != null) {
				if (noPageWizard.canFinish()) {
					return noPageWizard.performFinish();
				}
			}
		}
		return true;
	}

	protected SynchronizeWizardDescription[] getWizards() {
		SynchronizeManager manager = (SynchronizeManager) TeamUI.getSynchronizeManager();
		return manager.getWizardDescriptors();
	}	
}
