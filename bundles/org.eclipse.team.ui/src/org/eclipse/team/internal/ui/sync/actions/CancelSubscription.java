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
package org.eclipse.team.internal.ui.sync.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.sync.views.SubscriberInput;
import org.eclipse.ui.actions.ActionContext;


class CancelSubscription extends Action {
	private final SyncViewerActions actions;
	public CancelSubscription(SyncViewerActions actions) {
		setText("Cancel");
		this.actions = actions;
		setToolTipText("Cancel the active synchronization target");
		// don't enable until necessary
		setEnabled(false);
	}
	public void run() {
		ActionContext context = actions.getContext();
		SubscriberInput input = (SubscriberInput)context.getInput();
		input.getSubscriber().cancel();
	}
	public void updateTitle(SubscriberInput input) {
		TeamSubscriber subscriber = input.getSubscriber();
		if(subscriber.isCancellable()) {
			setText("Cancel [" + subscriber.getName() +"]");
		} else {
			setText("Cancel");
		}
		setToolTipText("Cancel the active synchronization target");
	}
}