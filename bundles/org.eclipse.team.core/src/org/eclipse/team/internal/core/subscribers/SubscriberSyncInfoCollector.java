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
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.subscribers.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.internal.core.Assert;
import org.eclipse.team.internal.core.Policy;

/**
 * This collector maintains a {@link SyncInfoSet} for a particular team subscriber keeping
 * it up-to-date with both incoming changes and outgoing changes as they occur for 
 * resources in the workspace. The collector can be configured to consider all the subscriber's
 * roots or only a subset.
 * <p>
 * The advantage of this collector is that it processes both resource and team
 * subscriber deltas in a background thread.
 * </p>
 * @since 3.0
 */
public final class SubscriberSyncInfoCollector implements IResourceChangeListener, ISubscriberChangeListener {

	private SyncSetInputFromSubscriber subscriberInput;
	private SyncSetInputFromSyncSet filteredInput;
	private SubscriberEventHandler eventHandler;
	private Subscriber subscriber;
	private IResource[] roots;
	
	/**
	 * Create a collector that collects out-of-sync resources that are children of
	 * the given roots. If the roots are <code>null</code>, then all out-of-sync resources
	 * from the subscriber are collected. An empty array of roots will cause no resources
	 * to be collected. The <code>start()</code> method must be called after creation
	 * to rpime the collector's sync sets.
	 * @param subscriber the Subscriber
	 * @param roots the roots of the out-of-sync resources to be collected
	 */
	public SubscriberSyncInfoCollector(Subscriber subscriber, IResource[] roots) {
		this.roots = roots;
		this.subscriber = subscriber;
		Assert.isNotNull(subscriber);
		this.eventHandler = new SubscriberEventHandler(subscriber, roots);
		this.subscriberInput = eventHandler.getSyncSetInput();
		filteredInput = new SyncSetInputFromSyncSet(subscriberInput.getSyncSet(), getEventHandler());
		filteredInput.setFilter(new SyncInfoFilter() {
			public boolean select(SyncInfo info, IProgressMonitor monitor) {
				return true;
			}
		});
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
		subscriber.addListener(this);
	}
	
	public void setProgressGroup(IProgressMonitor monitor, int ticks) {
		getEventHandler().setProgressGroupHint(monitor, ticks);
	}
	
	/**
	 * Start the collector. 
	 */
	public void start() {
		eventHandler.start();
	}

	/**
	 * This causes the calling thread to wait any background collection of out-of-sync resources
	 * to stop before returning.
	 * @param monitor a progress monitor
	 */
	public void waitForCollector(IProgressMonitor monitor) {
		monitor.worked(1);
		// wait for the event handler to process changes.
		while(eventHandler.getEventHandlerJob().getState() != Job.NONE) {
			monitor.worked(1);
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
			Policy.checkCanceled(monitor);
		}
		monitor.worked(1);
	}
	
	/**
	 * Clears this collector's sync info sets and causes them to be recreated from the
	 * associated <code>Subscriber</code>. The reset will occur in the background. If the
	 * caller wishes to wait for the reset to complete, they should call 
	 * {@link waitForCollector(IProgressMonitor)}.
	 */
	public void reset() {	
		eventHandler.reset(getRoots());
	}

	/**
	 * Returns the <code>Subscriber</code> associated with this collector.
	 * 
	 * @return the <code>Subscriber</code> associated with this collector.
	 */
	public Subscriber getSubscriber() {
		return subscriber;
	}

	/**
	 * Disposes of the background job associated with this collector and deregisters
	 * all it's listeners. This method must be called when the collector is no longer
	 * referenced and could be garbage collected.
	 */
	public void dispose() {
		eventHandler.shutdown();
		subscriberInput.disconnect();
		if(filteredInput != null) {
			filteredInput.disconnect();
		}
		getSubscriber().removeListener(this);		
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	/**
	 * Process the resource delta and posts all necessary events to the background
	 * event handler.
	 * 
	 * @param delta the resource delta to analyse
	 */
	private void processDelta(IResourceDelta delta, IResource[] roots) {
		IResource resource = delta.getResource();
		int kind = delta.getKind();

		if (resource.getType() == IResource.PROJECT) {
			// Handle a deleted project
			if (((kind & IResourceDelta.REMOVED) != 0)) {
				eventHandler.remove(resource);
				return;
			}
			// Handle a closed project
			if ((delta.getFlags() & IResourceDelta.OPEN) != 0 && !((IProject) resource).isOpen()) {
				eventHandler.remove(resource);
				return;
			}
			// Only interested in projects mapped to the provider
			if (!isAncestorOfRoot(resource, roots)) {
				// If the project has any entries in the sync set, remove them
				if (getSubscriberSyncInfoSet().hasMembers(resource)) {
					eventHandler.remove(resource);
				}
				return;
			}
		}

		boolean visitChildren = false;
		if (isDescendantOfRoot(resource, roots)) {
			visitChildren = true;
			// If the resource has changed type, remove the old resource handle
			// and add the new one
			if ((delta.getFlags() & IResourceDelta.TYPE) != 0) {
				eventHandler.remove(resource);
				eventHandler.change(resource, IResource.DEPTH_INFINITE);
			}
	
			// Check the flags for changes the SyncSet cares about.
			// Notice we don't care about MARKERS currently.
			int changeFlags = delta.getFlags();
			if ((changeFlags & (IResourceDelta.OPEN | IResourceDelta.CONTENT)) != 0) {
				eventHandler.change(resource, IResource.DEPTH_ZERO);
			}
	
			// Check the kind and deal with those we care about
			if ((delta.getKind() & (IResourceDelta.REMOVED | IResourceDelta.ADDED)) != 0) {
				eventHandler.change(resource, IResource.DEPTH_ZERO);
			}
		}

		// Handle changed children
		if (visitChildren || isAncestorOfRoot(resource, roots)) {
			IResourceDelta[] affectedChildren = delta.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.REMOVED | IResourceDelta.ADDED);
			for (int i = 0; i < affectedChildren.length; i++) {
				processDelta(affectedChildren[i], roots);
			}
		}
	}

	private boolean isAncestorOfRoot(IResource parent, IResource[] roots) {
		// Always traverse into projects in case a root was removed
		if (parent.getType() == IResource.ROOT) return true;
		for (int i = 0; i < roots.length; i++) {
			IResource resource = roots[i];
			if (parent.getFullPath().isPrefixOf(resource.getFullPath())) {
				return true;
			}
		}
		return false;
	}

	private boolean isDescendantOfRoot(IResource resource, IResource[] roots) {
		for (int i = 0; i < roots.length; i++) {
			IResource root = roots[i];
			if (root.getFullPath().isPrefixOf(resource.getFullPath())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Return the roots that are being considered by this collector.
	 * By default, the collector is interested in the roots of its
	 * subscriber. However, the set can be reduced using {@link setRoots(IResource)).
	 * @return
	 */
	public IResource[] getRoots() {
		if (roots == null) {
			return getSubscriber().roots();
		} else {
			return roots;
		}
	}
	
	/*
	 * Returns whether the collector is configured to collect for
	 * all roots of the subscriber or not
	 * @return <code>true</code> if the collector is considering all 
	 * roots of the subscriber and <code>false</code> otherwise
	 */
	public boolean isAllRootsIncluded() {
		return roots == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		processDelta(event.getDelta(), getRoots());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.team.core.sync.ITeamResourceChangeListener#teamResourceChanged(org.eclipse.team.core.sync.TeamDelta[])
	 */
	public void subscriberResourceChanged(ISubscriberChangeEvent[] deltas) {
		IResource[] roots = getRoots();
		for (int i = 0; i < deltas.length; i++) {
			switch (deltas[i].getFlags()) {
				case ISubscriberChangeEvent.SYNC_CHANGED :
					if (isAllRootsIncluded() || isDescendantOfRoot(deltas[i].getResource(), roots)) {
						eventHandler.change(deltas[i].getResource(), IResource.DEPTH_ZERO);
					}
					break;
				case ISubscriberChangeEvent.ROOT_REMOVED :
					eventHandler.remove(deltas[i].getResource());
					break;
				case ISubscriberChangeEvent.ROOT_ADDED :
					if (isAllRootsIncluded() || isDescendantOfRoot(deltas[i].getResource(), roots)) {
						eventHandler.change(deltas[i].getResource(), IResource.DEPTH_INFINITE);
					}
					break;
			}
		}
	}
	
	/**
	 * Return the event handler that performs the background processing for this collector.
	 * The event handler also serves the purpose of serializing the modifications and adjustments
	 * to the collector's sync sets in order to ensure that the state of the sets is kept
	 * consistent.
	 * @return Returns the eventHandler.
	 */
	protected SubscriberEventHandler getEventHandler() {
		return eventHandler;
	}
	
	/**
	 * Return the <code>SyncInfoSet</code> that contains all the all the out-of-sync resources for the
	 * subscriber that are descendants of the roots of this collector. The set will contain only those resources that are children of the roots
	 * of the collector unless the roots of the colletor has been set to <code>null</code>
	 * in which case all out-of-sync resources from the subscriber are collected.
	 * @return the subscriber sync info set
	 */
	public SyncInfoTree getSubscriberSyncInfoSet() {
		return subscriberInput.getSyncSet();
	}
	
	public SyncInfoTree getSyncInfoSet() {
		return filteredInput.getSyncSet();
	}
	
	/**
	 * Set the filter for this collector. Only elements that match the filter will
	 * be in the out sync info set.
	 * @see getSyncInfoSet()
	 * @param filter the sync info filter
	 */
	public void setFilter(SyncInfoFilter filter) {
		filteredInput.setFilter(filter);
		filteredInput.reset();
	}
	
	/**
	 * Return the filter that is filtering the output of this collector.
	 * @return a sync info filter
	 */
	public SyncInfoFilter getFilter() {
		if(filteredInput != null) {
			return filteredInput.getFilter();
		}
		return null;
	}

	/**
	 * @param roots2
	 */
	public void setRoots(IResource[] roots) {
		this.roots = roots;
		reset();
	}
}
