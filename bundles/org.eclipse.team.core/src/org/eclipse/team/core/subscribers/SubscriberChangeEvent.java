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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;

/**
 * A concrete implementation of <code>ISubscriberChangeEvent</code> that can
 * be used by clients.
 *
 * @see IResource
 * @see ITeamProvider
 */
public class SubscriberChangeEvent implements ISubscriberChangeEvent {

	private Subscriber subscriber; 
	private int flags;
	private IResource resource; 
	
	public SubscriberChangeEvent(Subscriber subscriber, int flags, IResource resource) {
		this.subscriber = subscriber;
		this.flags = flags;
		this.resource = resource;
	}

	public int getFlags() {
		return flags;
	}

	public IResource getResource() {
		return resource;
	}

	public Subscriber getSubscriber() {
		return subscriber;
	}
	
	/**
	 * Returns an array of deltas for the resources with SubscriberChangeEvent.SYNC_CHANGED
	 * as the change type.
	 * @param resources the resources whose sync info has changed
	 * @return an array of events
	 */
	public static SubscriberChangeEvent[] asSyncChangedDeltas(Subscriber subscriber, IResource[] resources) {
		SubscriberChangeEvent[] deltas = new SubscriberChangeEvent[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			deltas[i] = new SubscriberChangeEvent(subscriber, ISubscriberChangeEvent.SYNC_CHANGED, resource);
		}
		return deltas;
	}
}
