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
package org.eclipse.team.internal.ui.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.ISharedImages;
import org.eclipse.team.ui.TeamImages;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * Wizard contributed to the global synchronize action to synchronize subscriber participants.
 *
 * @since 3.0
 */
public class SubscriberRefreshWizard extends Wizard {
	
	public final static int SCOPE_WORKING_SET = 1;
	public final static int SCOPE_SELECTED_RESOURCES = 2;
	public final static int SCOPE_ENCLOSING_PROJECT = 3;
	public final static int SCOPE_PARTICIPANT_ROOTS = 4;

	private SubscriberParticipant participant;
	private GlobalRefreshResourceSelectionPage selectionPage;
	private GlobalRefreshSchedulePage schedulePage;
	private int scopeHint;

	public SubscriberRefreshWizard(SubscriberParticipant participant) {
		this.participant = participant;
		setWindowTitle(Policy.bind("SubscriberRefreshWizard.0") + participant.getName()); //$NON-NLS-1$
		setDefaultPageImageDescriptor(TeamImages.getImageDescriptor(ISharedImages.IMG_WIZBAN_SHARE));
		setNeedsProgressMonitor(false);
	}
	
	public void setScopeHint(int scopeHint) {
		this.scopeHint = scopeHint;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		selectionPage = new GlobalRefreshResourceSelectionPage(participant, scopeHint);
		addPage(selectionPage);
		schedulePage = new GlobalRefreshSchedulePage(participant);
		addPage(schedulePage);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	public boolean performFinish() {
		IResource[] resources = selectionPage.getCheckedResources();
		schedulePage.performFinish();
		if(resources != null && resources.length > 0) {
			// We don't know in which site to show progress because a participant could actually be shown in multiple sites.
			participant.refresh(resources, Policy.bind("Participant.synchronizing"), Policy.bind("Participant.synchronizingDetail", participant.getName()), null); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return true;
	}
}
