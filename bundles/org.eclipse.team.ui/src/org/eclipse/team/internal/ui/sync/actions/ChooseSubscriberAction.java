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

import org.eclipse.team.internal.ui.Utils;

class ChooseSubscriberAction extends SyncViewerToolbarDropDownAction {
	private final SyncViewerActions actions;

	public void run() {
		RefreshAction refresh = new RefreshAction(actions, true /* refresh all */);
		refresh.run();
	}

	public ChooseSubscriberAction(SyncViewerActions actions, SyncViewerActionGroup[] actionGroup) {
		super(actionGroup);
		this.actions = actions;
		Utils.initAction(this, "action.refreshSubscriber."); //$NON-NLS-1$
	}		
}