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
package org.eclipse.team.tests.ccvs.core.subscriber;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.core.subscribers.TeamProvider;
import org.eclipse.team.core.sync.RemoteSyncElement;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ui.sync.views.SyncResource;
import org.eclipse.team.internal.ui.sync.views.SyncSet;
import org.eclipse.team.tests.ccvs.core.EclipseTest;

/**
 * Provides test methods common to CVS sync subscribers
 */
public abstract class CVSSyncSubscriberTest extends EclipseTest {

	private ITeamResourceChangeListener listener;
	private List accumulatedTeamDeltas = new ArrayList();
	
	public CVSSyncSubscriberTest() {
		super();
	}
	
	public CVSSyncSubscriberTest(String name) {
		super(name);
	}

	protected TeamSubscriber getWorkspaceSubscriber() throws TeamException {
		TeamSubscriber subscriber = TeamProvider.getSubscriber(CVSProviderPlugin.CVS_WORKSPACE_SUBSCRIBER_ID);
		if (subscriber == null) fail("The CVS sync subsciber is not registered");
		return subscriber;
	}
	
	/*
	 * Refresh the subscriber for the given resource
	 */
	protected void refresh(TeamSubscriber subscriber, IResource resource) throws TeamException {
		subscriber.refresh(new IResource[] { resource}, IResource.DEPTH_INFINITE, DEFAULT_MONITOR);
	}
	
	/*
	 * Assert that the specified resources in the subscriber have the specified sync kind
	 * Ignore conflict types if they are not specified in the assert statement
	 */
	protected void assertSyncEquals(String message, TeamSubscriber subscriber, IContainer root, String[] resourcePaths, boolean refresh, int[] syncKinds) throws CoreException, TeamException {
		assertTrue(resourcePaths.length == syncKinds.length);
		if (refresh) refresh(subscriber, root);
		IResource[] resources = getResources(root, resourcePaths);
		for (int i=0;i<resources.length;i++) {
			assertSyncEquals(message, subscriber, resources[i], syncKinds[i]);
		}
		
	}
	
	protected void assertSyncEquals(String message, TeamSubscriber subscriber, IResource resource, int syncKind) throws TeamException {
		int conflictTypeMask = 0x0F; // ignore manual and auto merge sync types for now.
		SyncInfo info = subscriber.getSyncInfo(resource, DEFAULT_MONITOR);
		int kind;
		int kindOther = syncKind & conflictTypeMask;
		if (info == null) {
			kind = SyncInfo.IN_SYNC;
		} else {
			kind = info.getKind() & conflictTypeMask;
		}
		assertTrue(message + ": improper sync state for " + resource + " expected " + 
				   RemoteSyncElement.kindToString(kindOther) + " but was " +
				   RemoteSyncElement.kindToString(kind), kind == kindOther);
	}
	
	/**
	 * @param changes
	 * @param resources
	 */
	protected void assertSyncChangesMatch(TeamDelta[] changes, IResource[] resources) {
		// First, ensure that all the resources appear in the delta
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			boolean found = false;
			for (int j = 0; j < changes.length; j++) {
				TeamDelta delta = changes[j];
				if (delta.getResource().equals(resource)) {
					found = true;
					break;
				}
			}
			assertTrue("No change reported for " + resource, found);
		}
		// TODO: We'll worry about extra deltas later
//		// Next, ensure there are no extra deltas
//		List changedResources = new ArrayList(resources.length);
//		changedResources.addAll(Arrays.asList(resources));
//		for (int i = 0; i < changes.length; i++) {
//			TeamDelta change = changes[i];
//			IResource resource = change.getResource();
//			assertTrue("Unanticipated change reported for " + resource, changedResources.contains(resource));
//		}
	}
	
	/* 
	 * Assert that the named resources have no local resource or sync info
	 */
	protected void assertDeleted(String message, IContainer root, String[] resourcePaths) throws CoreException, TeamException {
		IResource[] resources = getResources(root, resourcePaths);
		for (int i=0;i<resources.length;i++) {
			try {
				if (! resources[i].exists())
					break;
			} catch (AssertionFailedError e) {
				break;
			}
			assertTrue(message + ": resource " + resources[i] + " still exists in some form", false);
		}
	}
	
	public static class ResourceCondition {
		public boolean matches(IResource resource) throws CoreException, TeamException {
			return true;
		}
	}
	
	protected IResource[] collect(IResource[] resources, final ResourceCondition condition, int depth) throws CoreException, TeamException {
		final Set affected = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			if (resource.exists() || resource.isPhantom()) {
				resource.accept(new IResourceVisitor() {
					public boolean visit(IResource r) throws CoreException {
						try {
							if (condition.matches(r)) {
								affected.add(r);
							}
						} catch (TeamException e) {
							throw new CoreException(e.getStatus());
						}
						return true;
					}
				}, depth, true /* include phantoms */);
			} else {
				if (condition.matches(resource)) {
					affected.add(resource);
				}
			}
		}
		return (IResource[]) affected.toArray(new IResource[affected.size()]);
	}
	
	/**
	 * @param resources
	 * @param condition
	 * @return
	 */
	protected IResource[] collectAncestors(IResource[] resources, ResourceCondition condition) throws CoreException, TeamException {
		Set affected = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			while (resource.getType() != IResource.ROOT) {
				if (condition.matches(resource)) {
					affected.add(resource);
				} else {
					break;
				}
				resource = resource.getParent();
			}
		}
		return (IResource[]) affected.toArray(new IResource[affected.size()]);
	}
	
	protected TeamDelta[] deregisterSubscriberListener(TeamSubscriber subscriber) throws TeamException {
		subscriber.removeListener(listener);
		return (TeamDelta[]) accumulatedTeamDeltas.toArray(new TeamDelta[accumulatedTeamDeltas.size()]);
	}

	protected ITeamResourceChangeListener registerSubscriberListener(TeamSubscriber subscriber) throws TeamException {
		listener = new ITeamResourceChangeListener() {
			public void teamResourceChanged(TeamDelta[] deltas) {
				accumulatedTeamDeltas.addAll(Arrays.asList(deltas));
			}
		};
		accumulatedTeamDeltas.clear();
		subscriber.addListener(listener);
		return listener;
	}
	
	/**
	 * @param resources
	 */
	protected SyncResource[] createSyncResources(TeamSubscriber subscriber, IResource[] resources) throws TeamException {
		// TODO: SyncResources needs a SyncSet which contains the SyncInfo
		// but SyncSet is not API
		SyncSet syncSet = new SyncSet();
		SyncResource[] result = new SyncResource[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			syncSet.add(subscriber.getSyncInfo(resource, DEFAULT_MONITOR));
			result[i] = new SyncResource(syncSet, resource);
		}
		return result;
	}
}
