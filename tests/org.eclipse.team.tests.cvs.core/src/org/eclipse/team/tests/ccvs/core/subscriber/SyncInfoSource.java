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

import java.util.*;

import junit.framework.AssertionFailedError;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.ccvs.core.*;

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
	public SyncInfo getSyncInfo(Subscriber subscriber, IResource resource) throws TeamException {
		return subscriber.getSyncInfo(resource);
	}
	
	/**
	 * Return the diff for the given subscriber for the given resource.
	 */
	public IDiff getDiff(Subscriber subscriber, IResource resource) throws CoreException {
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
}
