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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.ISynchronizationScopeManager;
import org.eclipse.team.core.mapping.provider.MergeContext;
import org.eclipse.team.core.mapping.provider.ResourceDiffTree;
import org.eclipse.team.core.synchronize.SyncInfo;
import org.eclipse.team.internal.core.subscribers.SubscriberDiffTreeEventHandler;

/**
 * A merge context that uses a subscriber to populate the diff tree
 * used by the context.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see Subscriber
 * @see MergeContext
 * 
 * @since 3.2
 */
public abstract class SubscriberMergeContext extends MergeContext {

	private Subscriber subscriber;
	private SubscriberDiffTreeEventHandler handler;
	private final ISynchronizationScopeManager manager;
	
	protected SubscriberMergeContext(Subscriber subscriber, ISynchronizationScopeManager manager) {
		super(manager, getType(subscriber), new ResourceDiffTree());
		this.subscriber = subscriber;
		this.manager = manager;
	}

	private static int getType(Subscriber subscriber) {
		return subscriber.getResourceComparator().isThreeWay()
			? THREE_WAY : TWO_WAY;
	}

	/**
	 * Initialize the diff tree of this context. This method must
	 * be called before the context is given to clients.
	 * @throws CoreException
	 */
	protected void initialize() {
		handler = new SubscriberDiffTreeEventHandler(subscriber, manager, (ResourceDiffTree)getDiffTree());
		handler.setJobFamily(this);
		handler.start();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ISynchronizationContext#refresh(org.eclipse.core.resources.mapping.ResourceTraversal[], int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void refresh(ResourceTraversal[] traversals, int flags,
			IProgressMonitor monitor) throws CoreException {
		subscriber.refresh(traversals, monitor);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.SynchronizationContext#dispose()
	 */
	public void dispose() {
		handler.shutdown();
		super.dispose();
	}
	
	/**
	 * Return the sync info for the given resource.
	 * @param resource the resource
	 * @return the sync info for the resource obtained from the subscriber
	 * @throws CoreException
	 */
	protected SyncInfo getSyncInfo(IResource resource) throws CoreException {
		return handler.getSubscriber().getSyncInfo(resource);
	}

	/**
	 * Return the subscriber associated with this context.
	 * @return the subscriber associated with this context
	 */
	public Subscriber getSubscriber() {
		return subscriber;
	}

}
