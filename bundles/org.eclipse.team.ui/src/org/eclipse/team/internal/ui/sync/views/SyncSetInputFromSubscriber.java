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
package org.eclipse.team.internal.ui.sync.views;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.ITeamResourceChangeListener;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.core.subscribers.TeamSubscriber;
import org.eclipse.team.core.subscribers.TeamDelta;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * This class translates resource deltas and subscriber events into the effects 
 * on a sync set
 */
public class SyncSetInputFromSubscriber extends SyncSetInput  implements IResourceChangeListener, ITeamResourceChangeListener {

	private TeamSubscriber subscriber;

	private void connect(TeamSubscriber s) {
		if (this.subscriber != null) return;
		this.subscriber = s;
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		s.addListener(this);
	}
	
	public void disconnect() {
		if (subscriber == null) return;
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		subscriber.removeListener(this);
		subscriber = null;
	}
	
	/**
	 * @return
	 */
	public TeamSubscriber getSubscriber() {
		return subscriber;
	}

	/**
	 * This method changes the subscriber the input is obtained from.
	 * This method may be long running as the sync state of several resources
	 * may need to be computed.
	 * 
	 * @param subscriber
	 */
	public void setSubscriber(TeamSubscriber subscriber, IProgressMonitor monitor) throws TeamException {
		if (subscriber != null) disconnect();
		connect(subscriber);
		reset(monitor);
	}
	
	/*
	 * Initialize the sync set to contain all out-of-sync resources.
	 */
	protected void fetchInput(IProgressMonitor monitor) throws TeamException {
		IResource[] roots = getRoots();
		monitor.beginTask(null, 100 * roots.length);
		try {
			SyncInfo[] outOfSync = getSubscriber().getAllOutOfSync(roots, IResource.DEPTH_INFINITE, Policy.infiniteSubMonitorFor(monitor, 100 * roots.length));
			if (outOfSync == null) {
				for (int i = 0; i < roots.length; i++) {
					IResource resource = roots[i];
					IProgressMonitor sub = Policy.infiniteSubMonitorFor(monitor, 100);
					sub.beginTask(null, 512);
					collectDeeply(resource, sub);
					sub.done();
				}
			} else {
				for (int i = 0; i < outOfSync.length; i++) {
					SyncInfo info = outOfSync[i];
					collect(info);
				}
			}
		} finally {
			monitor.done();
		}
	}
	
	protected IResource[] getRoots() {
		return getSubscriber().roots();
	}

	private void collectMembers(IContainer container, IProgressMonitor monitor) throws TeamException {
		IResource[] members = getSubscriber().members(container);
		for (int i = 0; i < members.length; i++) {
			IResource resource = members[i];
			collectDeeply(resource, monitor);
		}
	}

	protected void collectDeeply(IResource resource, IProgressMonitor monitor) throws TeamException {
		monitor.worked(1);
		collect(resource, monitor);
		if (resource.getType() != IResource.FILE) {
			collectMembers((IContainer) resource, monitor);
		}
	}
	protected void collect(IResource resource, IProgressMonitor monitor) throws TeamException {
		SyncInfo info = getSubscriber().getSyncInfo(resource, monitor);
		// resource is no longer under the subscriber control
		if (info == null) {
			remove(resource);
		} else { 
			collect(info);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		SyncSet syncSet = getSyncSet();
		try {
			syncSet.beginInput();
			processDelta(event.getDelta());
		} catch (TeamException e) {
			TeamUIPlugin.log(e);
		} finally {
			getSyncSet().endInput();
		}
	}
	
	/**
	 * Process the resource delta
	 * 
	 * @param delta
	 */
	private void processDelta(IResourceDelta delta) throws TeamException {
		IResource resource = delta.getResource();
		int kind = delta.getKind();
		
		if (resource.getType() == IResource.PROJECT) {
			// Handle a deleted project
			if (getSyncSet().hasMembers((IContainer)resource)) {
				if (((kind & IResourceDelta.REMOVED) != 0)
					 || !subscriber.isSupervised((IProject)resource)) {
						getSyncSet().removeAllChildren((IProject)resource);
					return;
				}
			}
			// Only interested in projects mapped to the provider
			if (!subscriber.isSupervised((IProject)resource)) {
				return;
			}
		}
		
		// If the resource has changed type, remove the old resource handle
		// and add the new one
		if ((delta.getFlags() & IResourceDelta.TYPE) != 0) {
			getSyncSet().removeAllChildren(resource);
			try {
				collectDeeply(resource, new NullProgressMonitor());
			} catch (TeamException e) {
				log(e);
			}
		}
		
		// Check the flags for changes the SyncSet cares about.
		// Notice we don't care about MARKERS currently.
		int changeFlags = delta.getFlags();
		if ((changeFlags
			& (IResourceDelta.OPEN | IResourceDelta.CONTENT))
			!= 0) {
				handleChange(resource);
		}
		
		// Check the kind and deal with those we care about
		if ((delta.getKind() & (IResourceDelta.REMOVED | IResourceDelta.ADDED)) != 0) {
			handleChange(resource);
		}
		
		// Handle changed children .
		IResourceDelta[] affectedChildren =
				delta.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.REMOVED | IResourceDelta.ADDED);
		for (int i = 0; i < affectedChildren.length; i++) {
			processDelta(affectedChildren[i]);
		}
		
	}
	
	// TODO: how to efficiently local resource changes so that we don't have
	// to recompute the sync state everytime?
	private void handleChange(IResource resource) {		
		// recalculate sync state, we don't have progress though? Not ideal
		// since the calculation could be long running...
		try {
			collect(resource, (IProgressMonitor)null);
		} catch (TeamException e) {
			log(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.sync.ITeamResourceChangeListener#teamResourceChanged(org.eclipse.team.core.sync.TeamDelta[])
	 */
	public void teamResourceChanged(TeamDelta[] deltas) {
		SyncSet syncSet = getSyncSet();
		try {
			syncSet.beginInput();
			for (int i = 0; i < deltas.length; i++) {
				switch(deltas[i].getFlags()) {
					case TeamDelta.SYNC_CHANGED:
						handleChange(deltas[i].getResource());
						break;
					case TeamDelta.PROVIDER_DECONFIGURED:
						getSyncSet().removeAllChildren(deltas[i].getResource());
						break;
					case TeamDelta.PROVIDER_CONFIGURED:
						// TODO: get the workbench progress monitor
						collectDeeply(deltas[i].getResource(), new NullProgressMonitor());
						break; 						
				}
			}
		} catch(TeamException e) {
			log(e);
		} finally {
			getSyncSet().endInput();
		}
	}
}
