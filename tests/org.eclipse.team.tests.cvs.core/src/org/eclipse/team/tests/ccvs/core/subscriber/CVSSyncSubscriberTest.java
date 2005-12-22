/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
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

import junit.framework.AssertionFailedError;

import org.eclipse.compare.structuremergeviewer.IDiffElement;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiffNode;
import org.eclipse.team.core.mapping.provider.DiffNode;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.CVSSyncTreeSubscriber;
import org.eclipse.team.internal.ccvs.ui.subscriber.ConfirmMergedOperation;
import org.eclipse.team.internal.core.mapping.SyncInfoToDiffConverter;
import org.eclipse.team.internal.ui.synchronize.SyncInfoModelElement;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.tests.ccvs.ui.SynchronizeViewTestAdapter;

/**
 * Provides test methods common to CVS sync subscribers
 */
public abstract class CVSSyncSubscriberTest extends EclipseTest {

	private ISubscriberChangeListener listener;
	private List accumulatedTeamDeltas = new ArrayList();
	private static SyncInfoSource source = new SynchronizeViewTestAdapter();

	public CVSSyncSubscriberTest() {
		super();
	}
	
	public CVSSyncSubscriberTest(String name) {
		super(name);
	}

	public static void setSyncSource(SyncInfoSource newSource) {
		source = newSource;
	}
	
	protected Subscriber getWorkspaceSubscriber() throws TeamException {
		return getSyncInfoSource().createWorkspaceSubscriber();
	}
	
	public SyncInfoSource getSyncInfoSource() {
		assertNotNull(source);
		return source;
	}
	
	protected void refresh(Subscriber subscriber, IResource resource) throws TeamException {
		getSyncInfoSource().refresh(subscriber, resource);
	}
	
	/*
	 * Assert that the specified resources in the subscriber have the specified sync kind
	 * Ignore conflict types if they are not specified in the assert statement
	 */
	protected void assertSyncEquals(String message, Subscriber subscriber, IContainer root, String[] resourcePaths, boolean refresh, int[] syncKinds) throws CoreException, TeamException {
		assertTrue(resourcePaths.length == syncKinds.length);
		if (refresh) refresh(subscriber, root);
		IResource[] resources = getResources(root, resourcePaths);
		for (int i=0;i<resources.length;i++) {
			assertSyncEquals(message, subscriber, resources[i], syncKinds[i]);
		}
		
	}
	
	protected void assertSyncEquals(String message, Subscriber subscriber, IResource resource, int syncKind) throws CoreException {
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
		assertTrue(message + ": improper sync state for " + resource + " expected " + 
				   SyncInfo.kindToString(kindOther) + " but was " +
				   SyncInfo.kindToString(kind), kind == kindOther);
		
	}
	
	protected SyncInfo getSyncInfo(Subscriber subscriber, IResource resource) throws TeamException {
		return getSyncInfoSource().getSyncInfo(subscriber, resource);
	}
	
	protected void assertDiffKindEquals(String message, Subscriber subscriber, IContainer root, String[] resourcePaths, boolean refresh, int[] diffKinds) throws CoreException, TeamException {
		assertTrue(resourcePaths.length == diffKinds.length);
		if (refresh) refresh(subscriber, root);
		IResource[] resources = getResources(root, resourcePaths);
		for (int i=0;i<resources.length;i++) {
			assertDiffKindEquals(message, subscriber, resources[i], diffKinds[i]);
		}
	}
	
	protected void assertDiffKindEquals(String message, Subscriber subscriber, IResource resource, int expectedFlags) throws CoreException {
		IDiffNode node = getDiff(subscriber, resource);
		int actualFlags;
		if (node == null) {
			actualFlags = IDiffNode.NO_CHANGE;
		} else {
			actualFlags = ((DiffNode)node).getStatus();
		}
		// Special handling for folders
		if (actualFlags != expectedFlags && resource.getType() == IResource.FOLDER) {
			// The only two states for folders are outgoing addition and in-sync.
			// Other additions will appear as in-sync
			int expectedKind = expectedFlags & DiffNode.KIND_MASK;
			int actualKind = actualFlags & DiffNode.KIND_MASK;
			if (actualKind == IDiffNode.NO_CHANGE 
					&& expectedKind == IDiffNode.ADD) {
				return;
			}
		}
		assertTrue(message + ": improper diff for " + resource + " expected " + 
				expectedFlags + " but was " + actualFlags, actualFlags == expectedFlags);
	}
	
	protected IDiffNode getDiff(Subscriber subscriber, IResource resource) throws CoreException {
		return getSyncInfoSource().getDiff(subscriber, resource);
	}

	protected void assertSyncChangesMatch(ISubscriberChangeEvent[] changes, IResource[] resources) {
		// First, ensure that all the resources appear in the delta
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			boolean found = false;
			for (int j = 0; j < changes.length; j++) {
				ISubscriberChangeEvent delta = changes[j];
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
	
	protected ISubscriberChangeEvent[] deregisterSubscriberListener(Subscriber subscriber) throws TeamException {
		subscriber.removeListener(listener);
		return (ISubscriberChangeEvent[]) accumulatedTeamDeltas.toArray(new SubscriberChangeEvent[accumulatedTeamDeltas.size()]);
	}

	protected ISubscriberChangeListener registerSubscriberListener(Subscriber subscriber) throws TeamException {
		listener = new ISubscriberChangeListener() {
			public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
				accumulatedTeamDeltas.addAll(Arrays.asList(deltas));
			}
		};
		accumulatedTeamDeltas.clear();
		subscriber.addListener(listener);
		return listener;
	}
	
	protected SyncInfo[] createSyncInfos(Subscriber subscriber, IResource[] resources) throws TeamException {
		SyncInfo[] result = new SyncInfo[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			result[i] = getSyncInfo(subscriber, resource);
		}
		return result;
	}

	protected void assertProjectRemoved(Subscriber subscriber, IProject project) throws TeamException {
		getSyncInfoSource().assertProjectRemoved(subscriber, project);
	}
	
	protected void markAsMerged(CVSSyncTreeSubscriber subscriber, IProject project, String[] resourcePaths) throws CoreException, TeamException, InvocationTargetException, InterruptedException {
		IResource[] resources = getResources(project, resourcePaths);
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
}
