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
package org.eclipse.team.internal.core.subscribers.caches;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.subscribers.Subscriber;
import org.eclipse.team.core.synchronize.*;

/**
 * A specialization of Subscriber that provides some additional logic for creating
 * <code>SyncInfo</code> from <code>IResourceVariant</code> instances. 
 * The <code>members()</code> also assumes that remote 
 * instances are stored in the <code>ISynchronizer</code>.
 */
public abstract class SyncTreeSubscriber extends Subscriber {

	public abstract IResourceVariant getRemoteResource(IResource resource) throws TeamException;

	public abstract IResourceVariant getBaseResource(IResource resource) throws TeamException;
	
	/**
	 * Return whether the given local resource has a corresponding remote resource
	 * @param resource the local resource
	 * @return <code>true</code> if the locla resource has a corresponding remote
	 */
	protected abstract boolean hasRemote(IResource resource) throws TeamException;

	public SyncInfo getSyncInfo(IResource resource) throws TeamException {
		if (!isSupervised(resource)) return null;
		IResourceVariant remoteResource = getRemoteResource(resource);
		IResourceVariant baseResource;
		if (getResourceComparator().isThreeWay()) {
			baseResource= getBaseResource(resource);
		} else {
			baseResource = null;
		}
		return getSyncInfo(resource, baseResource, remoteResource);
	}

	/**
	 * @return
	 */
	public abstract IResourceVariantComparator getResourceComparator();

	/**
	 * Method that creates an instance of SyncInfo for the provider local, base and remote.
	 * Can be overiden by subclasses.
	 * @param local
	 * @param base
	 * @param remote
	 * @param monitor
	 * @return
	 */
	protected SyncInfo getSyncInfo(IResource local, IResourceVariant base, IResourceVariant remote) throws TeamException {
		SyncInfo info = new SyncInfo(local, base, remote, this.getResourceComparator());
		info.init();
		return info;
	}

	public IResource[] members(IResource resource) throws TeamException {
		if(resource.getType() == IResource.FILE) {
			return new IResource[0];
		}	
		try {
			// Filter and return only phantoms associated with the remote synchronizer.
			IResource[] members;
			try {
				members = ((IContainer)resource).members(true /* include phantoms */);
			} catch (CoreException e) {
				if (!isSupervised(resource) || e.getStatus().getCode() == IResourceStatus.RESOURCE_NOT_FOUND) {
					// The resource is no longer supervised or doesn't exist in any form
					// so ignore the exception and return that there are no members
					return new IResource[0];
				}
				throw e;
			}
			List filteredMembers = new ArrayList(members.length);
			for (int i = 0; i < members.length; i++) {
				IResource member = members[i];
				
				// TODO: consider that there may be several sync states on this resource. There
				// should instead be a method to check for the existance of a set of sync types on
				// a resource.
				if(member.isPhantom() && !hasRemote(member)) {
					continue;
				}
				
				// TODO: Is this a valid use of isSupervised
				if (isSupervised(resource)) {
					filteredMembers.add(member);
				}
			}
			return (IResource[]) filteredMembers.toArray(new IResource[filteredMembers.size()]);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

}
