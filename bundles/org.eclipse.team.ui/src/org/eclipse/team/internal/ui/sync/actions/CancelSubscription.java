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
import org.eclipse.team.internal.ui.Utils;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;

class CancelSubscription extends Action {
	private TeamSubscriber subscriber;
	private SynchronizeView view;
	
	public CancelSubscription(SynchronizeView view, TeamSubscriber s) {
		this.subscriber = s;
		this.view = view;
		Utils.initAction(this, "action.cancelSubscriber."); //$NON-NLS-1$
		// don't enable until necessary
		setEnabled(false);
	}
	
	public void run() {
		view.removeSubscriber(subscriber);
	}

	public void setSubscriber(TeamSubscriber subscriber) {
		this.subscriber = subscriber;
	}
}