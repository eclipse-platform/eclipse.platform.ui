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
import org.eclipse.team.internal.core.subscribers.SubscriberDiffTreeEventHandler;
import org.eclipse.team.internal.ui.synchronize.patch.*;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;

public class ReversePatchAction extends Action {

	private ISynchronizePageConfiguration configuration;
	private ApplyPatchModelSynchronizeParticipant participant;
	private ApplyPatchSubscriberMergeContext context;
	private ApplyPatchSubscriber subscriber;

	public ReversePatchAction(ISynchronizePageConfiguration configuration) {
		super("", AS_CHECK_BOX); //$NON-NLS-1$
		this.configuration = configuration;
		participant = (ApplyPatchModelSynchronizeParticipant) configuration
				.getParticipant();
		context = (ApplyPatchSubscriberMergeContext) participant.getContext();
		subscriber = (ApplyPatchSubscriber) context.getSubscriber();
	}

	public boolean isChecked() {
		return subscriber.getPatcher().isReversed();
	}

	public void run() {
		boolean oldValue = subscriber.getPatcher().isReversed();
		subscriber.getPatcher().setReversed(!oldValue);

		SubscriberDiffTreeEventHandler handler = (SubscriberDiffTreeEventHandler) context
				.getAdapter(SubscriberDiffTreeEventHandler.class);
		handler.reset();
		participant.refresh(configuration.getSite().getWorkbenchSite(), context
				.getScope().getMappings());

		firePropertyChange(CHECKED, new Boolean(oldValue), new Boolean(
				!oldValue));
	}

}
