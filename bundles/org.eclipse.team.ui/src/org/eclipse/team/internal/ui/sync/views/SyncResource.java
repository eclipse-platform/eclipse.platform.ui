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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * This is the UI model object representing a SyncInfo for a resource.
 * The main purpose of this class is to allow menu object contributions
 * to be applied to these resources.
 */
public class SyncResource implements IAdaptable {

	private SyncSet syncSet;
	private IResource resource;

	/**
	 * @param info
	 */
	public SyncResource(SyncSet syncSet, IResource resource) {
		this.syncSet = syncSet;
		this.resource = resource;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IResource.class) {
			return getLocalResource();
		} else if (adapter == SyncInfo.class) {
			return getSyncInfo();
		}
		return null;
	}

	/**
	 * @return
	 */
	public IResource getLocalResource() {
		return resource;
	}

	/**
	 * @return
	 */
	public SyncInfo getSyncInfo() {
		return syncSet.getSyncInfo(resource);
	}
	
	/**
	 * Return an array of all descendants (including the receiver) that have
	 * a non-null sync-info.
	 * @return
	 */
	public SyncResource[] getOutOfSyncDescendants() {
		List result = new ArrayList();
		SyncInfo info = getSyncInfo();
		if (info != null) {
			result.add(this);
		}
		Object[] members = SyncSet.members(syncSet, getLocalResource());
		for (int i = 0; i < members.length; i++) {
			Object object = members[i];
			if (object instanceof SyncResource) {
				SyncResource child = (SyncResource) object;
				result.addAll(Arrays.asList(child.getOutOfSyncDescendants()));
			}
		}
		return (SyncResource[]) result.toArray(new SyncResource[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object object) {
		if (object instanceof SyncResource) {
			SyncResource syncResource = (SyncResource) object;
			return getLocalResource().equals(syncResource.getLocalResource());
		}
		return super.equals(object);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getLocalResource().hashCode();
	}

	/**
	 * @return
	 */
	public int getChangeType() {
		return getKind() & SyncInfo.CHANGE_MASK;
	}
	
	/**
	 * @return
	 */
	public int getChangeDirection() {
		return getKind() & SyncInfo.DIRECTION_MASK;
	}

	/**
	 * @return
	 */
	public int getKind() {
		SyncInfo info = getSyncInfo();
		if (info == null) return 0;
		return info.getKind();
	}

	/**
	 * @return
	 */
	public SyncResource getParent() {
		Object parent = SyncSet.getParent(syncSet, this);
		if (parent instanceof SyncResource) {
			return (SyncResource)parent;
		}
		return null;
	}

	/**
	 * @return
	 */
	public IResource getResource() {
		return resource;
	}
	
	public String toString() {
		return "Sync for " + getResource().getFullPath().toString();
	}
}
