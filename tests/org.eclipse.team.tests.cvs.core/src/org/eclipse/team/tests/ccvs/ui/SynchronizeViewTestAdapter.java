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
package org.eclipse.team.tests.ccvs.ui;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeSynchronizeParticipant;
import org.eclipse.team.internal.ui.synchronize.sets.SubscriberInput;
import org.eclipse.team.internal.ui.synchronize.sets.SyncSet;
import org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.TeamSubscriberParticipant;

/**
 * SyncInfoSource that obtains SyncInfo from the SynchronizeView's SyncSet.
 */
public class SynchronizeViewTestAdapter extends SyncInfoSource {

	public SynchronizeViewTestAdapter() {
		TeamUI.getSynchronizeManager().showSynchronizeViewInActivePage(null);
	}
	
	public void waitForEventNotification(SubscriberInput input) {
		// process UI events first, give the main thread a chance
		// to handle any syncExecs or asyncExecs posted as a result
		// of the event processing thread.
		while (Display.getCurrent().readAndDispatch()) {};
		
		// wait for the event handler to process changes.
		Job job = input.getEventHandler().getEventHandlerJob();
		while(job.getState() != Job.NONE) {
			while (Display.getCurrent().readAndDispatch()) {};
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
		}						
	}
	
	public SyncInfo getSyncInfo(TeamSubscriber subscriber, IResource resource) throws TeamException {
		SubscriberInput input = getInput(subscriber);
		SyncSet set = input.getWorkingSetSyncSet();
		SyncInfo info = set.getSyncInfo(resource);
		if (info == null) {
			info = subscriber.getSyncInfo(resource, DEFAULT_MONITOR);
			if ((info != null && info.getKind() != SyncInfo.IN_SYNC)) {
				throw new AssertionFailedError();
			}
		}
		return info;
	}
	
	private SubscriberInput getInput(TeamSubscriber subscriber) {
		// show the sync view
		ISynchronizeParticipant[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			String id = "";
			if(subscriber instanceof CVSMergeSubscriber) {
				id = ((CVSMergeSubscriber)subscriber).getId().getQualifier();
			} else if(subscriber instanceof CVSWorkspaceSubscriber) {
				id = ((CVSWorkspaceSubscriber)subscriber).getId().getQualifier();
			}
			ISynchronizeParticipant participant = participants[i];
			if(participant.getId().equals(id)) {
				if(participant instanceof TeamSubscriberParticipant) {
					SubscriberInput input = ((TeamSubscriberParticipant)participant).getInput();
					waitForEventNotification(input);
					return input;
				}
			}
		}
		return null;
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#createMergeSubscriber(org.eclipse.core.resources.IProject, org.eclipse.team.internal.ccvs.core.CVSTag, org.eclipse.team.internal.ccvs.core.CVSTag)
	 */
	public CVSMergeSubscriber createMergeSubscriber(IProject project, CVSTag root, CVSTag branch) {
		CVSMergeSubscriber mergeSubscriber = super.createMergeSubscriber(project, root, branch);
		ISynchronizeManager synchronizeManager = TeamUI.getSynchronizeManager();
		ISynchronizeParticipant participant = new MergeSynchronizeParticipant(mergeSubscriber);
		synchronizeManager.addSynchronizeParticipants(
				new ISynchronizeParticipant[] {participant});		
		ISynchronizeView view = synchronizeManager.showSynchronizeViewInActivePage(null);
		view.display(participant);
		return mergeSubscriber;
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#tearDown()
	 */
	public void tearDown() {
		ISynchronizeParticipant[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant participant = participants[i];
			if(participant.getId().equals(CVSMergeSubscriber.QUALIFIED_NAME)) {
				TeamUI.getSynchronizeManager().removeSynchronizeParticipants(new ISynchronizeParticipant[] {participant});
			}
		}
		// Process all async events that may have been generated above
		while (Display.getCurrent().readAndDispatch()) {};
	}
}
