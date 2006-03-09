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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.team.core.mapping.provider.SynchronizationContext;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.subscribers.SubscriberMergeContext;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.ComparePreferencePage;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ModelSynchronizeParticipant;

public class ModelCompareParticipant extends ModelSynchronizeParticipant
		implements ISynchronizeParticipant {

	public ModelCompareParticipant(SynchronizationContext context) {
		super(context);
		try {
			setInitializationData(TeamUI.getSynchronizeManager().getParticipantDescriptor("org.eclipse.team.cvs.ui.modelCompareParticipant")); //$NON-NLS-1$
		} catch (CoreException e) {
			TeamUIPlugin.log(e);
		}
		setSecondaryId(Long.toString(System.currentTimeMillis()));
	}

    /* (non-Javadoc)
     * @see org.eclipse.team.ui.synchronize.AbstractSynchronizeParticipant#getPreferencePages()
     */
    public PreferencePage[] getPreferencePages() {
        return addCVSPreferencePages(super.getPreferencePages());
    }

    public static PreferencePage[] addCVSPreferencePages(PreferencePage[] inheritedPages) {
        PreferencePage[] pages = new PreferencePage[inheritedPages.length + 1];
        for (int i = 0; i < inheritedPages.length; i++) {
            pages[i] = inheritedPages[i];
        }
        pages[pages.length - 1] = new ComparePreferencePage();
        pages[pages.length - 1].setTitle(CVSUIMessages.CVSParticipant_2); 
        return pages;
    }

	public Subscriber getSubscriber() {
		return ((SubscriberMergeContext)getContext()).getSubscriber();
	}
}
