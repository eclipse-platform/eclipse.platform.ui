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
package org.eclipse.team.tests.ccvs.ui.sync;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ui.IPreferenceIds;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.sync.sets.SubscriberInput;
import org.eclipse.team.internal.ui.sync.sets.SyncSet;
import org.eclipse.team.internal.ui.sync.views.SynchronizeView;
import org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource;

/**
 * SyncInfoSource that obtains SyncInfo from the SynchronizeView's SyncSet.
 */
public class SyncInfoFromSyncSet extends SyncInfoSource {

	public SyncInfoFromSyncSet() {		
	}
	
	public SyncInfo getSyncInfo(TeamSubscriber subscriber, IResource resource) throws TeamException {
		TeamUIPlugin.getPlugin().getPreferenceStore().setValue(IPreferenceIds.TESTING_SYNCVIEW, true);
		SubscriberInput input = getInput(subscriber);
		SyncSet set = input.getFilteredSyncSet();
		SyncInfo info = set.getSyncInfo(resource);
		if (info == null) {
			info = subscriber.getSyncInfo(resource, DEFAULT_MONITOR);
			if ((info != null && info.getKind() != SyncInfo.IN_SYNC)) {
				throw new AssertionFailedError();
			}
		}
		return info;
	}
	
	private SubscriberInput getInput(TeamSubscriber subscriber) throws AssertionFailedError {
		// show the sync view
		SynchronizeView syncView = (SynchronizeView)SynchronizeView.showInActivePage(null);
		SubscriberInput input = syncView.getInput();
		if (subscriber != input.getSubscriber()) {
			// ensure that the CVS subscriber is active
			syncView.activateSubscriber(subscriber);
			while (Display.getCurrent().readAndDispatch()) {};			
			input = syncView.getInput();
		} else {
			try {
				// sleep to let the events propagate
				Thread.sleep(500);
				} catch (InterruptedException e) {
			}
		}
		if (subscriber != input.getSubscriber()) {
			throw new AssertionFailedError();
		}
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#assertProjectRemoved(org.eclipse.team.core.subscribers.TeamSubscriber, org.eclipse.core.resources.IProject)
	 */
	protected void assertProjectRemoved(TeamSubscriber subscriber, IProject project) throws TeamException {
		super.assertProjectRemoved(subscriber, project);
		SubscriberInput input = getInput(subscriber);
		SyncSet set = input.getFilteredSyncSet();
		if (set.getOutOfSyncDescendants(project).length != 0) {
			throw new AssertionFailedError("The sync set still contains resources from the deleted project " + project.getName());	
		}
	}
}
