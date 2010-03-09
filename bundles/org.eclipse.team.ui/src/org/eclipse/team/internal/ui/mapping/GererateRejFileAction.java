/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.mapping;

import org.eclipse.jface.action.Action;
import org.eclipse.team.internal.ui.synchronize.patch.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class GererateRejFileAction extends Action {

	private ApplyPatchSubscriber subscriber;

	public GererateRejFileAction(ISynchronizePageConfiguration configuration) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		ApplyPatchModelSynchronizeParticipant participant = (ApplyPatchModelSynchronizeParticipant) configuration
				.getParticipant();
		ApplyPatchSubscriberMergeContext context = (ApplyPatchSubscriberMergeContext) participant
				.getContext();
		subscriber = (ApplyPatchSubscriber) context.getSubscriber();
	}

	public boolean isChecked() {
		return subscriber.getPatcher().isGenerateRejectFile();
	}

	public void run() {
		boolean oldValue = subscriber.getPatcher().isGenerateRejectFile();
		subscriber.getPatcher().setGenerateRejectFile(!oldValue);

		firePropertyChange(CHECKED, new Boolean(oldValue), new Boolean(
				!oldValue));
	}

}
