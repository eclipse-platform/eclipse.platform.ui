/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ccvs.core.CVSCompareSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSMergeSubscriber;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.team.internal.ccvs.ui.subscriber.CompareParticipant;
import org.eclipse.team.internal.ccvs.ui.subscriber.MergeSynchronizeParticipant;
import org.eclipse.team.internal.ccvs.ui.subscriber.WorkspaceSynchronizeParticipant;
import org.eclipse.team.internal.core.subscribers.SubscriberSyncInfoCollector;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.team.internal.ui.synchronize.SubscriberParticipantPage;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.internal.ui.synchronize.SynchronizeView;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource;
import org.eclipse.team.ui.TeamUI;
import org.eclipse.team.ui.synchronize.ISynchronizeManager;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipant;
import org.eclipse.team.ui.synchronize.ISynchronizeParticipantReference;
import org.eclipse.team.ui.synchronize.ISynchronizeView;
import org.eclipse.team.ui.synchronize.SubscriberParticipant;
import org.eclipse.team.ui.synchronize.WorkspaceScope;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;

/**
 * SyncInfoSource that obtains SyncInfo from the SynchronizeView's SyncSet.
 */
public class SynchronizeViewTestAdapter extends SyncInfoSource {

	public SynchronizeViewTestAdapter() {
			IWorkbenchPage activePage = TeamUIPlugin.getActivePage();
			try {
				activePage.showView(ISynchronizeView.VIEW_ID);
			} catch (PartInitException e) {
				throw new AssertionFailedError("Cannot show sync view in active page");
			}
	}
	
	public SyncInfo getSyncInfo(Subscriber subscriber, IResource resource) throws TeamException {
		// Wait for the collector
		SyncInfoSet set = getCollector(subscriber).getSyncInfoSet();
		// Obtain the sync info from the viewer to ensure that the 
		// entire chain has the proper state
		SyncInfo info = internalGetSyncInfo(subscriber, resource);
		// Do a sanity check on the collected sync info
		if (info == null) {
			info = subscriber.getSyncInfo(resource);
			if ((info != null && info.getKind() != SyncInfo.IN_SYNC)) {
				throw new AssertionFailedError(
						"Sync state for " 
						+ resource.getFullPath() 
						+ " was "
						+ SyncInfo.kindToString(info.getKind())
						+ " but resource was not collected");
			}
		} else {
			SyncInfo realInfo = subscriber.getSyncInfo(resource);
			if (info.getKind() != realInfo.getKind()) {
				throw new AssertionFailedError(
						"Collected sync state for " 
						+ resource.getFullPath() 
						+ " was "
						+ SyncInfo.kindToString(info.getKind())
						+ " but the real state was "
						+ SyncInfo.kindToString(realInfo.getKind()));
			}
		}
		return info;
	}
	
	private SubscriberParticipant getParticipant(Subscriber subscriber) {
		// show the sync view
		ISynchronizeParticipantReference[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			ISynchronizeParticipant participant;
			try {
				participant = participants[i].getParticipant();
			} catch (TeamException e) {
				return null;
			}
			if(participant instanceof SubscriberParticipant) {
				if(((SubscriberParticipant)participant).getSubscriber() == subscriber) {
					return (SubscriberParticipant)participant;
				}
			}
		}
		return null;
	}
	
	private SubscriberSyncInfoCollector getCollector(Subscriber subscriber) {
		SubscriberParticipant participant = getParticipant(subscriber);
		if (participant == null) return null;
		SubscriberSyncInfoCollector syncInfoCollector = participant.getSubscriberSyncInfoCollector();
		EclipseTest.waitForSubscriberInputHandling(syncInfoCollector);
		return syncInfoCollector;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#assertProjectRemoved(org.eclipse.team.core.subscribers.TeamSubscriber, org.eclipse.core.resources.IProject)
	 */
	protected void assertProjectRemoved(Subscriber subscriber, IProject project) throws TeamException {		
		super.assertProjectRemoved(subscriber, project);
		SyncInfoTree set = getCollector(subscriber).getSyncInfoSet();
		if (set.hasMembers(project)) {
			throw new AssertionFailedError("The sync set still contains resources from the deleted project " + project.getName());	
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#createMergeSubscriber(org.eclipse.core.resources.IProject, org.eclipse.team.internal.ccvs.core.CVSTag, org.eclipse.team.internal.ccvs.core.CVSTag)
	 */
	public CVSMergeSubscriber createMergeSubscriber(IProject project, CVSTag root, CVSTag branch) {
		CVSMergeSubscriber mergeSubscriber = super.createMergeSubscriber(project, root, branch);
		ISynchronizeManager synchronizeManager = TeamUI.getSynchronizeManager();
		SubscriberParticipant participant = new MergeSynchronizeParticipant(mergeSubscriber);
		synchronizeManager.addSynchronizeParticipants(
				new ISynchronizeParticipant[] {participant});		
		IWorkbenchPage activePage = TeamUIPlugin.getActivePage();
		try {
			ISynchronizeView view = (ISynchronizeView)activePage.showView(ISynchronizeView.VIEW_ID);
			view.display(participant);
		} catch (PartInitException e) {
			throw new AssertionFailedError("Cannot show sync view in active page");
		}
		return mergeSubscriber;
	}
	
	public Subscriber createWorkspaceSubscriber() throws TeamException {
		ISynchronizeManager synchronizeManager = TeamUI.getSynchronizeManager();
		ISynchronizeParticipantReference[] participants = synchronizeManager.get(WorkspaceSynchronizeParticipant.ID);
		if (participants.length > 0) {
			return ((SubscriberParticipant)participants[0].getParticipant()).getSubscriber();
		}
		SubscriberParticipant participant = new WorkspaceSynchronizeParticipant(new WorkspaceScope());
		synchronizeManager.addSynchronizeParticipants(
				new ISynchronizeParticipant[] {participant});		
		IWorkbenchPage activePage = TeamUIPlugin.getActivePage();
		try {
			ISynchronizeView view = (ISynchronizeView)activePage.showView(ISynchronizeView.VIEW_ID);
			view.display(participant);
		} catch (PartInitException e) {
			throw new AssertionFailedError("Cannot show sync view in active page");
		}
		return participant.getSubscriber();
	}
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#createCompareSubscriber(org.eclipse.core.resources.IProject, org.eclipse.team.internal.ccvs.core.CVSTag)
	 */
	public CVSCompareSubscriber createCompareSubscriber(IResource resource, CVSTag tag) {
		CVSCompareSubscriber s = super.createCompareSubscriber(resource, tag);
		ISynchronizeManager synchronizeManager = TeamUI.getSynchronizeManager();
		SubscriberParticipant participant = new CompareParticipant(s);
		synchronizeManager.addSynchronizeParticipants(
				new ISynchronizeParticipant[] {participant});	
		IWorkbenchPage activePage = TeamUIPlugin.getActivePage();
		try {
			ISynchronizeView view = (ISynchronizeView)activePage.showView(ISynchronizeView.VIEW_ID);
			view.display(participant);
		} catch (PartInitException e) {
			throw new AssertionFailedError("Cannot show sync view in active page");
		}
		return s;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#tearDown()
	 */
	public void tearDown() {
		ISynchronizeParticipantReference[] participants = TeamUI.getSynchronizeManager().getSynchronizeParticipants();
		for (int i = 0; i < participants.length; i++) {
			try {
				ISynchronizeParticipantReference ref = participants[i];
				if(ref.getParticipant().getId().equals(CVSMergeSubscriber.ID)) {
					TeamUI.getSynchronizeManager().removeSynchronizeParticipants(new ISynchronizeParticipant[] {ref.getParticipant()});
				}
			} catch (TeamException e) {
				return;
			}
		}
		// Process all async events that may have been generated above
		while (Display.getCurrent().readAndDispatch()) {};
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#refresh(org.eclipse.team.core.subscribers.TeamSubscriber, org.eclipse.core.resources.IResource)
	 */
	public void refresh(Subscriber subscriber, IResource resource) throws TeamException {
		super.refresh(subscriber, resource);
		// Getting the collector waits for the subscriber input handlers
		getCollector(subscriber);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.tests.ccvs.core.subscriber.SyncInfoSource#reset()
	 */
	public void reset(Subscriber subscriber) throws TeamException {
		super.reset(subscriber);
		getCollector(subscriber).reset();
	}
	
	private SyncInfo internalGetSyncInfo(Subscriber subscriber, IResource resource) {
		try {
			SubscriberParticipant participant = getParticipant(subscriber);
			IWorkbenchPage activePage = TeamUIPlugin.getActivePage();
			ISynchronizeView view = (ISynchronizeView)activePage.showView(ISynchronizeView.VIEW_ID);
			IPage page = ((SynchronizeView)view).getPage(participant);
			if (page instanceof SubscriberParticipantPage) {
				SubscriberParticipantPage subscriberPage = (SubscriberParticipantPage)page;
				ISelection selection = subscriberPage.getViewerAdvisor().getModelManager().getSelection(new Object[] { resource });
				if (!selection.isEmpty() && selection instanceof StructuredSelection) {
					StructuredSelection ss = (StructuredSelection)selection;
					Object o = ss.getFirstElement();
					if (o instanceof SyncInfoModelElement) {
						return ((SyncInfoModelElement)o).getSyncInfo();
					}
				}
			}
		} catch (PartInitException e) {
			throw new AssertionFailedError("Cannot show sync view in active page");
		}
		return null;
	}
}
