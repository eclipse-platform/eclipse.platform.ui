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

import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;

/**
 * An <code>ISynchronizeViewNode</code> is used in the <code>ISynchronizeView</code>
 * to display the kind of change detected as the result of a two-way or three-way
 * synchronize.
 * <p>
 * Clients can contribute objectContribution actions to the Synchronize View that are 
 * scoped by <code>TeamSubscriber</code> ids.
 * <br>
 * Example objectContribution:
 * 
 * 	<extension
 *			point="org.eclipse.ui.popupMenus">
 *		<objectContribution
 *          objectClass="org.eclipse.team.ui.sync.ISynchronizeViewNode"
 *          id="yourObjectContributionID"
 *           adaptable="true">
 *         
 *       	<filter name="equals" value="org.eclipse.team.cvs.subscribers:workspace-subscriber" />
 * 			<action
 *				label="Your Action Label"
 *				menubarPath="SubscriberActions"
 *				class="org.eclipse.team.internal.ccvs.ui.subscriber.SubscriberCommitAction"
 *				overrideActionId="org.eclipse.team.ccvs.ui.commit"
 *				id="yourActionID">
 *			</action>
 *		</objectContribution>
 *	</extension>
 * 
 * The above example makes use of the standard UI mechanisms for contributing ObjectContribution
 * Actions in Eclipse.  See the UI documentation for additional elements that can be declared.
 * The interesting elements for scoping the contribution to a TeamSubscriber type are:
 * 
 * <filter>	element in the object contribution.  This will remove all the actions and their
 * 			overridesActionIds unless the filter is matched.  The name can be "equals" 
 * 			or "startsWith" and the value is the ID of the subscriber.
 * 
 * menuparPath is either SubscriberActions to place actions in their own group above other
 * objectContributions or Additions to place the actions at the bottom of the menu with other
 * objectContributions
 * 
 * overrideActionId is optional but if present, is the ID of an existing objectContribution that
 * this action should replace.  
 * 
 * </p> 
 * @see org.eclipse.team.ui.sync.ISynchronizeView
 * @see org.eclipse.team.core.subscribers.TeamSubscriber
 * @since 3.0
 */
public interface ISynchronizeViewNode {
	/**
	 * Answer the receiver's Subscriber
	 * @return the node's TeamSubscriber
	 */
	public abstract TeamSubscriber getTeamSubscriber();
	/**
	 * Returns the SyncInfo for this node. Note that the SynchronizeView only creates nodes 
	 * for resources that are out-of-sync. 
	 * @return SyncInfo the sync info for this node
	 */
	public abstract SyncInfo getSyncInfo();
	/**
	 * Return an array that contains all children (including the receiver) that have SyncInfos 
	 * that are out-of-sync. Returns an empty array if this node does not have children.
	 * @return SyncInfo[] all out-of-sync child resources.
	 */
	public abstract SyncInfo[] getChildSyncInfos();
}
