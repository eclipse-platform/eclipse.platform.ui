/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.core.subscriber;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.provider.Diff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.subscriber.CVSSubscriberOperation;
import org.eclipse.team.internal.ccvs.ui.subscriber.ConfirmMergedOperation;
import org.eclipse.team.internal.core.mapping.SyncInfoToDiffConverter;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;

/**
 * This class acts as the source for the sync info used by the subscriber tests.
 * The purpose is to allow the sync info to be obtained directly from the subscriber 
 * or through the sync set visible in the sync view.
 */
public class SyncInfoSource {

	protected static IProgressMonitor DEFAULT_MONITOR = new NullProgressMonitor();
	protected List mergeSubscribers = new ArrayList();
	protected List compareSubscribers = new ArrayList();
	
	public CVSMergeSubscriber createMergeSubscriber(IProject project, CVSTag root, CVSTag branch) {
		CVSMergeSubscriber subscriber = new CVSMergeSubscriber(new IResource[] { project }, root, branch);
		mergeSubscribers.add(subscriber);
		return subscriber;
	}
	
	public CVSCompareSubscriber createCompareSubscriber(IResource resource, CVSTag tag) {
		CVSCompareSubscriber subscriber = new CVSCompareSubscriber(new IResource[] { resource }, tag);
		compareSubscribers.add(subscriber);
		return subscriber;
	}
	
	public Subscriber createWorkspaceSubscriber() throws TeamException {
		return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
	}
	
	/**
	 * Return the sync info for the given subscriber for the given resource.
	 */
	protected SyncInfo getSyncInfo(Subscriber subscriber, IResource resource) throws TeamException {
		return subscriber.getSyncInfo(resource);
	}
	
	/**
	 * Return the diff for the given subscriber for the given resource.
	 */
	protected IDiff getDiff(Subscriber subscriber, IResource resource) throws CoreException {
		return subscriber.getDiff(resource);
	}
	
	/**
	 * Refresh the subscriber for the given resource
	 */
	public void refresh(Subscriber subscriber, IResource resource) throws TeamException {
		refresh(subscriber, new IResource[] { resource});
	}
	
	/**
	 * Refresh the subscriber for the given resources
	 */
    public void refresh(Subscriber subscriber, IResource[] resources) throws TeamException {
        subscriber.refresh(resources, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
    }
    
	protected void assertProjectRemoved(Subscriber subscriber, IProject project) throws TeamException {
		IResource[] roots = subscriber.roots();
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			if (resource.equals(project)) {
				throw new AssertionFailedError();
			}
		}
	}

	public void tearDown() {
		for (Iterator it = mergeSubscribers.iterator(); it.hasNext(); ) {
			CVSMergeSubscriber s = (CVSMergeSubscriber) it.next();
			s.cancel();
		}
	}

	/**
	 * Recalculate a sync info from scratch
	 */
	public void reset(Subscriber subscriber) throws TeamException {
		// Do nothing
	}
	
	/**
	 * Assert that the model for the subscriber matches what is being displayed.
	 * Default is to do nothing. Subclasses may override
	 * @param subscriber the subscriber
	 */
	public void assertViewMatchesModel(Subscriber subscriber) {
	    // Default is to do nothing. Subclasses may override
	}
	
	public void assertSyncEquals(String message, Subscriber subscriber, IResource resource, int syncKind) throws CoreException {
		int conflictTypeMask = 0x0F; // ignore manual and auto merge sync types for now.
		SyncInfo info = getSyncInfo(subscriber, resource);
		int kind;
		int kindOther = syncKind & conflictTypeMask;
		if (info == null) {
			kind = SyncInfo.IN_SYNC;
		} else {
			kind = info.getKind() & conflictTypeMask;
		}
		// Special handling for folders
		if (kind != kindOther && resource.getType() == IResource.FOLDER) {
			// The only two states for folders are outgoing addition and in-sync.
			// Other additions will appear as in-sync
			if (info.getKind() == SyncInfo.IN_SYNC 
					&& (syncKind & SyncInfo.ADDITION) != 0) {
				return;
			}
		} else {
			// Only test if kinds are equal
			assertDiffKindEquals(message, subscriber, resource, SyncInfoToDiffConverter.asDiffFlags(syncKind));
		}
		junit.framework.Assert.assertTrue(message + ": improper sync state for " + resource + " expected " + 
				   SyncInfo.kindToString(kindOther) + " but was " +
				   SyncInfo.kindToString(kind), kind == kindOther);
		
	}
	
	protected void assertDiffKindEquals(String message, Subscriber subscriber, IResource resource, int expectedFlags) throws CoreException {
		IDiff node = getDiff(subscriber, resource);
		int actualFlags;
		if (node == null) {
			actualFlags = IDiff.NO_CHANGE;
		} else {
			actualFlags = ((Diff)node).getStatus();
		}
		// Special handling for folders
		if (actualFlags != expectedFlags && resource.getType() == IResource.FOLDER) {
			// The only two states for folders are outgoing addition and in-sync.
			// Other additions will appear as in-sync
			int expectedKind = expectedFlags & Diff.KIND_MASK;
			int actualKind = actualFlags & Diff.KIND_MASK;
			if (actualKind == IDiff.NO_CHANGE 
					&& expectedKind == IDiff.ADD) {
				return;
			}
		}
		junit.framework.Assert.assertTrue(message + ": improper diff for " + resource + " expected " + 
				SyncInfoToDiffConverter.diffStatusToString(expectedFlags) 
				+ " but was " + SyncInfoToDiffConverter.diffStatusToString(actualFlags), actualFlags == expectedFlags);
	}
	
	public void mergeResources(CVSMergeSubscriber subscriber, IResource[] resources, boolean allowOverwrite) throws TeamException, InvocationTargetException, InterruptedException {
		SyncInfo[] infos = createSyncInfos(subscriber, resources);
		mergeResources(subscriber, infos, allowOverwrite);
	}
	
	private void mergeResources(Subscriber subscriber, SyncInfo[] infos, boolean allowOverwrite) throws TeamException, InvocationTargetException, InterruptedException {
		TestMergeUpdateOperation action = new TestMergeUpdateOperation(getElements(infos), allowOverwrite);
		action.run(DEFAULT_MONITOR);
	}
	
	protected SyncInfo[] createSyncInfos(Subscriber subscriber, IResource[] resources) throws TeamException {
		SyncInfo[] result = new SyncInfo[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			result[i] = getSyncInfo(subscriber, resource);
		}
		return result;
	}
	
	public void markAsMerged(CVSSyncTreeSubscriber subscriber, IResource[] resources) throws InvocationTargetException, InterruptedException, TeamException {
		SyncInfo[] infos = createSyncInfos(subscriber, resources);
		new ConfirmMergedOperation(null, getElements(infos)).run(DEFAULT_MONITOR);
	}
	

	protected IDiffElement[] getElements(SyncInfo[] infos) {
		SyncInfoModelElement[] elements = new SyncInfoModelElement[infos.length];
		for (int i = 0; i < elements.length; i++) {
			elements[i] = new SyncInfoModelElement(null, infos[i]);
		}
		return elements;
	}
	
	public void updateResources(Subscriber subscriber, IResource[] resources) throws CoreException {
		runSubscriberOperation(new TestUpdateOperation(getElements(createSyncInfos(subscriber, resources))));
	}
	
	public void commitResources(Subscriber subscriber, IResource[] resources) throws CoreException {
		runSubscriberOperation(new TestCommitOperation(getElements(createSyncInfos(subscriber, resources)), false /* override */));
	}
	
	public void overrideAndUpdateResources(Subscriber subscriber, boolean shouldPrompt, IResource[] resources) throws CoreException {
		TestOverrideAndUpdateOperation action = new TestOverrideAndUpdateOperation(getElements(createSyncInfos(subscriber, resources)));
		runSubscriberOperation(action);
		Assert.assertTrue(shouldPrompt == action.isPrompted());
	}
	
	public void overrideAndCommitResources(CVSSyncTreeSubscriber subscriber, IResource[] resources) throws CoreException {
		TestCommitOperation action = new TestCommitOperation(getElements(createSyncInfos(subscriber, resources)), true /* override */);
		runSubscriberOperation(action);
	}
	
	private void runSubscriberOperation(CVSSubscriberOperation op) throws CoreException {
		try {
			op.run();
		} catch (InvocationTargetException e) {
			throw CVSException.wrapException(e);
		} catch (InterruptedException e) {
			junit.framework.Assert.fail("Operation was interrupted");
		}
	}

}
