/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.team.core.ITeamStatus;
import org.eclipse.team.core.diff.IDiff;
import org.eclipse.team.core.diff.IDiffVisitor;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.core.mapping.ISynchronizationScope;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.internal.core.Policy;

/**
 * A subscriber event handler whose output is a diff tree
 */
public class SubscriberDiffTreeEventHandler extends SubscriberEventHandler {

	private ResourceDiffTree tree;
	private SubscriberDiffCollector collector;
	private Object family;

	/*
	 * An event used to represent a change in a diff
	 */
	private class SubscriberDiffChangedEvent extends SubscriberEvent {
		private final IDiff node;

		public SubscriberDiffChangedEvent(IResource resource, int type, int depth, IDiff node) {
			super(resource, type, depth);
			this.node = node;
		}
		public IDiff getChangedNode() {
			return node;
		}
	}
	
	/*
	 * Collects resource and subscriber changes
	 */
	private class SubscriberDiffCollector extends SubscriberResourceCollector {

		public SubscriberDiffCollector(Subscriber subscriber) {
			super(subscriber);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#hasMembers(org.eclipse.core.resources.IResource)
		 */
		protected boolean hasMembers(IResource resource) {
			return tree.members(resource).length > 0;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#remove(org.eclipse.core.resources.IResource)
		 */
		protected void remove(IResource resource) {
			SubscriberDiffTreeEventHandler.this.remove(resource);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.team.internal.core.subscribers.SubscriberResourceCollector#change(org.eclipse.core.resources.IResource, int)
		 */
		protected void change(IResource resource, int depth) {
			SubscriberDiffTreeEventHandler.this.change(resource, depth);
		}
	}
	
	/**
	 * Create the handler
	 * @param subscriber the subscriber for the handler
	 * @param scope the scope of the handler
	 * @param tree the tree to be populated by this handler
	 */
	public SubscriberDiffTreeEventHandler(Subscriber subscriber, ISynchronizationScope scope, ResourceDiffTree tree) {
		super(subscriber, scope);
		this.tree = tree;
		this.collector = new SubscriberDiffCollector(subscriber);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.SubscriberEventHandler#handleChange(org.eclipse.core.resources.IResource)
	 */
	protected void handleChange(IResource resource) throws CoreException {
		IDiff node = getSubscriber().getDiff(resource);
		if (node == null) {
			queueDispatchEvent(
				new SubscriberEvent(resource, SubscriberEvent.REMOVAL, IResource.DEPTH_ZERO));
		} else {
			queueDispatchEvent(
				new SubscriberDiffChangedEvent(resource, SubscriberEvent.CHANGE, IResource.DEPTH_ZERO, node));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.SubscriberEventHandler#collectAll(org.eclipse.core.resources.IResource, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void collectAll(IResource resource, int depth,
			final IProgressMonitor monitor) {
		ResourceTraversal[] traversals = new ResourceTraversal[] { new ResourceTraversal(new IResource[] { resource }, depth, IResource.NONE) };
		try {
			getSubscriber().accept(traversals, new IDiffVisitor() {
				public boolean visit(IDiff node) throws CoreException {
					// Queue up any found diffs for inclusion into the output tree
					queueDispatchEvent(
							new SubscriberDiffChangedEvent(tree.getResource(node), SubscriberEvent.CHANGE, IResource.DEPTH_ZERO, node));
					// Handle any pending dispatches
					handlePreemptiveEvents(monitor);
					handlePendingDispatch(monitor);
					return true;
				}
			});
		} catch (CoreException e) {
			handleException(e, resource, ITeamStatus.SYNC_INFO_SET_ERROR, e.getMessage());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.SubscriberEventHandler#dispatchEvents(org.eclipse.team.internal.core.subscribers.SubscriberEventHandler.SubscriberEvent[], org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected void dispatchEvents(SubscriberEvent[] events,
			IProgressMonitor monitor) {
		try {
        	tree.beginInput();
			for (int i = 0; i < events.length; i++) {
				SubscriberEvent event = events[i];
				switch (event.getType()) {
					case SubscriberEvent.CHANGE :
						if (event instanceof SubscriberDiffChangedEvent) {
							SubscriberDiffChangedEvent se = (SubscriberDiffChangedEvent) event;
							IDiff changedNode = se.getChangedNode();
							if (changedNode.getKind() == IDiff.NO_CHANGE) {
								tree.remove(changedNode.getPath());
							} else {
								tree.add(changedNode);
							}
						}
						break;
					case SubscriberEvent.REMOVAL :
						IDiff[] nodesToRemove = tree.getDiffs(new ResourceTraversal[] { event.asTraversal() });
						for (int j = 0; j < nodesToRemove.length; j++) {
							IDiff node = nodesToRemove[j];
							tree.remove(node.getPath());
						}
						break;
				}
			}
		} finally {
			tree.endInput(monitor);
		}
	}

	/**
	 * Return the resource diff tree that contains the out-of-sync diffs for the 
	 * subscriber.
	 * @return the resource diff tree
	 */
	public IResourceDiffTree getTree() {
		return tree;
	}
	
	public void waitUntilIdle(IProgressMonitor monitor) {
		monitor.worked(1);
		// wait for the event handler to process changes.
		while(getEventHandlerJob().getState() != Job.NONE) {
			monitor.worked(1);
			try {
				Thread.sleep(10);		
			} catch (InterruptedException e) {
			}
			Policy.checkCanceled(monitor);
		}
		monitor.worked(1);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.SubscriberEventHandler#getSubscriber()
	 */
	public Subscriber getSubscriber() {
		return super.getSubscriber();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.core.subscribers.SubscriberEventHandler#shutdown()
	 */
	public void shutdown() {
		collector.dispose();
		super.shutdown();
	}

	protected Object getJobFamiliy() {
		return family;
	}
	public void setJobFamily(Object family) {
		this.family = family;
	}

}
