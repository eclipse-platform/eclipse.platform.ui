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
package org.eclipse.team.internal.ccvs.ui.subscriber;

import org.eclipse.jface.action.Action;
import org.eclipse.team.internal.ui.synchronize.sets.ISyncSetChangedListener;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.sets.SyncSetChangedEvent;
import org.eclipse.ui.actions.ActionDelegate;

public abstract class SynchronizeViewActionDelagate extends Action implements ISyncSetChangedListener {

	private SubscriberInput input;
	private ActionDelegate delegate;

	public SynchronizeViewActionDelagate(SubscriberInput input, ActionDelegate delegate) {
		super();
		this.delegate = delegate;
		this.input = input;
		input.getFilteredSyncSet().addSyncSetChangedListener(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ui.sync.sets.ISyncSetChangedListener#syncSetChanged(org.eclipse.team.internal.ui.sync.sets.SyncSetChangedEvent)
	 */
	public void syncSetChanged(SyncSetChangedEvent event) {
		setEnabled(isEnabled());		
	}
}
