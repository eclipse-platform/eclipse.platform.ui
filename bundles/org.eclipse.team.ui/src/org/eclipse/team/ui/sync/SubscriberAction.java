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
package org.eclipse.team.ui.sync;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.actions.TeamAction;

/**
 * This is the abstract superclass for actions associated with a subscriber. 
 * It is not necessary that subscriber actions be subclasses of this class.
 */
public abstract class SubscriberAction extends TeamAction {
	
	TeamSubscriber subscriber;
	
	/**
	 * This method returns all instances of SyncResource that are in the current
	 * selection. For a table view, this is any resource that is directly selected.
	 * For a tree view, this is any descendants of the slected resource that are
	 * contained in the view.
	 * 
	 * @return the selected resources
	 */
	protected SyncInfo[] getSyncInfos() {
		SyncInfo[] syncInfos= (SyncInfo[])getSelectedResources(SyncInfo.class);
		return syncInfos;
	}

	/**
	 * The default enablement behavior for subscriber actions is to enable
	 * the action if there is at least one SyncInfo in the selection
	 * for which the action is enabled (determined by invoking 
	 * <code>isEnabled(SyncInfo)</code>).
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	protected boolean isEnabled() throws TeamException {
		return (getFilteredSyncInfos().length > 0);
	}

	/**
	 * Return true if the action should be enabled for the given SyncInfo.
	 * Default behavior is to use a SyncInfoFilter to determine if the action
	 * is enabled.
	 * 
	 * @param info
	 * @return
	 */
	protected boolean select(SyncInfo info) {
		return info != null && getSyncInfoFilter().select(info);
	}

	/**
	 * @return
	 */
	protected SyncInfoFilter getSyncInfoFilter() {
		return new SyncInfoFilter();
	}

	/**
	 * Return the selected SyncInfo for which this action is enabled.
	 * @return
	 */
	protected SyncInfo[] getFilteredSyncInfos() {
		SyncInfo[] infos = getSyncInfos();
		List filtered = new ArrayList();
		for (int i = 0; i < infos.length; i++) {
			SyncInfo info = infos[i];
			if (select(info))
				filtered.add(info);
		}
		return (SyncInfo[]) filtered.toArray(new SyncInfo[filtered.size()]);
	}

	/**
	 * @return
	 */
	public TeamSubscriber getSubscriber() {
		return subscriber;
	}

	/**
	 * Sets 
	 * @param context
	 */
	public void setSubscriber(TeamSubscriber subscriber) {
		this.subscriber = subscriber;
	}
}
