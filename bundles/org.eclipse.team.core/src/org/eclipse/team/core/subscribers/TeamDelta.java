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
 * A team delta represents changes in the team state.
 *
 * @see IResource
 * @see ITeamProvider
 */
public class TeamDelta {
	
	/*====================================================================
	 * Constants defining the kinds of team changes to resources:
	 *====================================================================*/

	/**
	 * Delta kind constant indicating that the resource has not been changed in any way
	 * @see IResourceDelta#getKind
	 */
	public static final int NO_CHANGE = 0;

	/**
	 * Delta kind constant (bit mask) indicating that the synchronization state of a resource has changed.
	 */
	public static final int SYNC_CHANGED = 0x1;

	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been configured on the resource.
	 * @see IResourceDelta#getKind
	 */
	public static final int PROVIDER_CONFIGURED = 0x2;
	
	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been de-configured on the resource.
	 * @see IResourceDelta#getKind
	 */	
	public static final int PROVIDER_DECONFIGURED = 0x4;

	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been de-configured on the resource.
	 * @see IResourceDelta#getKind
	 */	
	public static final int SUBSCRIBER_CREATED = 0x8;
	
	/**
	 * Delta kind constant (bit mask) indicating that a team provider has been de-configured on the resource.
	 * @see IResourceDelta#getKind
	 */	
	public static final int SUBSCRIBER_DELETED = 0x10;

	private TeamSubscriber subscriber; 
	private int flags;
	private IResource resource; 
	
	public TeamDelta(TeamSubscriber subscriber, int flags, IResource resource) {
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

	public TeamSubscriber getSubscriber() {
		return subscriber;
	}
	
	/**
	 * Returns an array of deltas for the resources with TeamDelta.SYNC_CHANGED
	 * as the change type.
	 * @param resources the resources whose sync info has changed
	 * @return
	 */
	public static TeamDelta[] asSyncChangedDeltas(TeamSubscriber subscriber, IResource[] resources) {
		TeamDelta[] deltas = new TeamDelta[resources.length];
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			deltas[i] = new TeamDelta(subscriber, TeamDelta.SYNC_CHANGED, resource);
		}
		return deltas;
	}
}
