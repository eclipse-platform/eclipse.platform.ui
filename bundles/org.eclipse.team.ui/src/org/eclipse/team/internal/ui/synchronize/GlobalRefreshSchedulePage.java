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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.team.internal.ui.Policy;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;

/**
 * Wizard page that allows configuration a synchronize schedule for a subscriber participant.
 * 
 * @since 3.0
 */
public class GlobalRefreshSchedulePage extends WizardPage {

	private SubscriberParticipant participant;
	private ConfigureSynchronizeScheduleComposite scheduleComposite;

	public GlobalRefreshSchedulePage(SubscriberParticipant participant) {
		super(Policy.bind("GlobalRefreshSchedulePage.0")); //$NON-NLS-1$
		setTitle(Policy.bind("GlobalRefreshSchedulePage.1")); //$NON-NLS-1$
		setDescription(Policy.bind("GlobalRefreshSchedulePage.2", participant.getName())); //$NON-NLS-1$ //$NON-NLS-2$
		this.participant = participant;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		IPageValidator validator = new IPageValidator() {
			public void setComplete(String errorMessage) {
				setPageComplete(errorMessage == null);
				setErrorMessage(errorMessage);
			}
		};
		scheduleComposite = new ConfigureSynchronizeScheduleComposite(parent, participant.getRefreshSchedule(), validator);
		setControl(scheduleComposite);
	}
	
	public void performFinish() {
		scheduleComposite.saveValues();
	}
}
