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
package org.eclipse.team.ui.synchronize.actions;

import java.util.*;

import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.actions.TeamAction;
import org.eclipse.team.ui.synchronize.ITeamSubscriberParticipantNode;
import org.eclipse.ui.IViewActionDelegate;

/**
 * This is an abstract superclass for actions associated with a 
 * {@link TeamSubscriberParticipant}. It provides helper methods to
 * access and filter selections that contain {@link ITeamSubscriberParticipantNode} 
 * instances.
 * <p>
 * It is optional for TeamSubscriberParticipant actions to subclass.
 * </p>
 * @since 3.0
 */
public abstract class SubscriberAction extends TeamAction implements IViewActionDelegate {
	
	public static final QualifiedName SUBSCRIBER_JOB_TYPE = new QualifiedName(TeamUIPlugin.ID, "subcriber_job"); //$NON-NLS-1$
	
	/**
	 * This method returns all instances of SynchronizeViewNode that are in the current
	 * selection. For a table view, this is any resource that is directly selected.
	 * For a tree view, this is any descendants of the selected resource that are
	 * contained in the view.
	 * 
	 * @return the selected resources
	 */
	protected SyncInfo[] getSyncInfos() {
		Object[] selected = ((IStructuredSelection)selection).toArray();
		Set result = new HashSet();
		for (int i = 0; i < selected.length; i++) {
			Object object = selected[i];
			if (object instanceof ITeamSubscriberParticipantNode) {
				ITeamSubscriberParticipantNode syncResource = (ITeamSubscriberParticipantNode) object;
				SyncInfo[] infos = syncResource.getChildSyncInfos();
				result.addAll(Arrays.asList(infos));
			}
		}
		return (SyncInfo[]) result.toArray(new SyncInfo[result.size()]);
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
}
