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
package org.eclipse.team.ui.synchronize;

import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;

/**
 * ITeamSubscriberParticipantNode is used in the page created by the 
 * <code>TeamSubscriberParticipant<code> to display the kind of change 
 * detected in a two or three-way synchronize operation. They are the
 * nodes shown in the Synchronize View.
 * <p>
 * Actions contributed to the TeamSubscriberParticipant will operate
 * on these nodes. A common super class {@link SubscriberAction} exists
 * to help create actions that are contributed to the TeamSubscriberParticipant.
 * It contains helpers for accessing and filtering these nodes.
 * </p>
 * @see TeamSubscriberParticipantPage
 * @see SubscriberAction
 * @since 3.0
 */
public interface ITeamSubscriberParticipantNode {
	/**
	 * Answer the receiver's Subscriber
	 * 
	 * @return the node's TeamSubscriber
	 */
	public abstract TeamSubscriber getTeamSubscriber();
	/**
	 * Returns the SyncInfo for this node. Note that the SynchronizeView only
	 * creates nodes for resources that are out-of-sync.
	 * 
	 * @return SyncInfo the sync info for this node
	 */
	public abstract SyncInfo getSyncInfo();
	/**
	 * Return an array that contains all children (including the receiver) that
	 * have SyncInfos that are out-of-sync. Returns an empty array if this node
	 * does not have children.
	 * 
	 * @return SyncInfo[] all out-of-sync child resources.
	 */
	public abstract SyncInfo[] getChildSyncInfos();
}