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
package org.eclipse.team.internal.ui.sync.sets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.TeamUIPlugin;

/**
 * This class keeps track of a set of resources and their associated synchronization
 * information. It is optimized so that retrieving out-of-sync children is fast. 
 */
public class SyncSet {
	// fields used to hold resources of interest
	// {IPath -> SyncInfo}
	protected Map resources = Collections.synchronizedMap(new HashMap());
	
	// {IPath -> Set of deep out of sync child IResources}
	// weird thing is that the child set will include the
	// parent if the parent is out of sync
	protected Map parents = Collections.synchronizedMap(new HashMap());

	// fields used for change notification
	protected SyncSetChangedEvent changes;
	protected Set listeners = Collections.synchronizedSet(new HashSet());
	
	protected SyncInfoStatistics statistics = new SyncInfoStatistics();
	
	public SyncSet() {
		resetChanges();
	}
	
	protected void resetChanges() {
		changes = new SyncSetChangedEvent(this);
	}

	protected void fireChanges() {
		// Use a synchronized block to ensure that the event we send is static
		SyncSetChangedEvent event;
		synchronized(this) {
			event = changes;
			resetChanges();
		}
		// Fire the events
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			ISyncSetChangedListener listener = (ISyncSetChangedListener) iter.next();
			listener.syncSetChanged(event);
		}
	}

	/**
	 * Add a change listener
	 * @param provider
	 */
	public void addSyncSetChangedListener(ISyncSetChangedListener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a change listener
	 * @param provider
	 */
	public void removeSyncSetChangedListener(ISyncSetChangedListener listener) {
		listeners.remove(listener);
	}

	public synchronized void add(SyncInfo info) {
		internalAddSyncInfo(info);
		changes.added(info);
		IResource local = info.getLocal();
		addToParents(local, local);
	}

	private void internalAddSyncInfo(SyncInfo info) {
		IResource local = info.getLocal();
		IPath path = local.getFullPath();
		if(resources.put(path, info) == null) {
			statistics.add(info);
		}
	}

	protected synchronized void remove(IResource local) {
		IPath path = local.getFullPath();
		SyncInfo info = (SyncInfo)resources.remove(path);
		changes.removed(local);
		statistics.remove(info);
		removeFromParents(local, local);
	}

	protected synchronized void changed(SyncInfo info) {
		internalAddSyncInfo(info);
		changes.changed(info);
	}

	/**
	 * Reset the sync set so it is empty
	 */
	public synchronized void reset() {
		resources.clear();
		parents.clear();
		changes.reset();
		statistics.clear();
	}
	
	protected boolean addToParents(IResource resource, IResource parent) {
		if (parent.getType() == IResource.ROOT) {
			return false;
		}
		// this flag is used to indicate if the parent was previosuly in the set
		boolean addedParent = false;
		if (parent.getType() == IResource.FILE) {
			// the file is new
			addedParent = true;
		} else {
			Set children = (Set)parents.get(parent.getFullPath());
			if (children == null) {
				children = new HashSet();
				parents.put(parent.getFullPath(), children);
				// this is a new folder in the sync set
				addedParent = true;
			}
			children.add(resource);
		}
		// if the parent already existed and the resource is new, record it
		if (!addToParents(resource, parent.getParent()) && addedParent) {
			changes.addedRoot(parent);
		}
		return addedParent;
	}

	protected boolean removeFromParents(IResource resource, IResource parent) {
		if (parent.getType() == IResource.ROOT) {
			return false;
		}
		// this flag is used to indicate if the parent was removed from the set
		boolean removedParent = false;
		if (parent.getType() == IResource.FILE) {
			// the file will be removed
			removedParent = true;
		} else {
			Set children = (Set)parents.get(parent.getFullPath());
			if (children != null) {
				children.remove(resource);
				if (children.isEmpty()) {
					parents.remove(parent.getFullPath());
					removedParent = true;
				}
			}
		}
		//	if the parent wasn't removed and the resource was, record it
		if (!removeFromParents(resource, parent.getParent()) && removedParent) {
			changes.removedRoot(parent);
		}
		return removedParent;
	}

	/**
	 * Return the children of the given container who are either out-of-sync or contain
	 * out-of-sync resources.
	 * 
	 * @param container
	 * @return
	 */
	public IResource[] members(IResource resource) {
		if (resource.getType() == IResource.FILE) return new IResource[0];
		IContainer parent = (IContainer)resource;
		if (parent.getType() == IResource.ROOT) return getRoots(parent);
		// TODO: must be optimized so that we don't traverse all the deep children to find
		// the immediate ones.
		Set children = new HashSet();
		IPath path = parent.getFullPath();
		Set possibleChildren = (Set)parents.get(path);
		if(possibleChildren != null) {
			for (Iterator it = possibleChildren.iterator(); it.hasNext();) {
				Object next = it.next();
				IResource element = (IResource)next;
				IPath childPath = element.getFullPath();
				IResource modelObject = null;
				if(childPath.segmentCount() == (path.segmentCount() +  1)) {
					modelObject = element;

				} else if (childPath.segmentCount() > path.segmentCount()) {
					IContainer childFolder = parent.getFolder(new Path(childPath.segment(path.segmentCount())));
					modelObject = childFolder;
				}
				if (modelObject != null) {
					children.add(modelObject);
				}
			}
		}
		return (IResource[]) children.toArray(new IResource[children.size()]);
	}
	
	/**
	 * Return the out-of-sync descendants of the given resource. If the given resource
	 * is out of sync, it will be included in the result.
	 * 
	 * @param container
	 * @return
	 */
	public SyncInfo[] getOutOfSyncDescendants(IResource resource) {
		if (resource.getType() == IResource.FILE) {
			SyncInfo info = getSyncInfo(resource);
			if (info == null) {
				return new SyncInfo[0];
			} else {
				return new SyncInfo[] { info };
			}
		};
		IContainer container = (IContainer)resource;
		IPath path = container.getFullPath();
		Set children = (Set)parents.get(path);
		if (children == null) return new SyncInfo[0];
		List infos = new ArrayList();
		for (Iterator iter = children.iterator(); iter.hasNext();) {
			IResource child = (IResource) iter.next();
			SyncInfo info = getSyncInfo(child);
			if(info != null) {
				infos.add(info);
			} else {
				TeamUIPlugin.log(IStatus.INFO, "missing sync info: " + child.getFullPath(), null); //$NON-NLS-1$
			}
		}
		return (SyncInfo[]) infos.toArray(new SyncInfo[infos.size()]);
	}

	private IResource[] getRoots(IContainer root) {
		Set possibleChildren = parents.keySet();
		Set children = new HashSet();
		for (Iterator it = possibleChildren.iterator(); it.hasNext();) {
			Object next = it.next();
			IResource element = ((IWorkspaceRoot)root).findMember((IPath)next);
			if (element != null) {
				children.add(element.getProject());
			}
		}
		return (IResource[]) children.toArray(new IResource[children.size()]);
	}

	protected boolean hasMembers(IContainer container) {
		return parents.containsKey(container.getFullPath());
	}

	/**
	 * Return an array of all the resources that are known to be out-of-sync
	 * @return
	 */
	public SyncInfo[] allMembers() {
		return (SyncInfo[]) resources.values().toArray(new SyncInfo[resources.size()]);
	}

	protected synchronized void removeAllChildren(IResource resource) {
		// The parent map contains a set of all out-of-sync children
		Set allChildren = (Set)parents.get(resource.getFullPath());
		if (allChildren == null) return;
		IResource [] removed = (IResource[]) allChildren.toArray(new IResource[allChildren.size()]);
		for (int i = 0; i < removed.length; i++) {
			remove(removed[i]);
		}
	}

	public SyncInfo getSyncInfo(IResource resource) {
		return (SyncInfo)resources.get(resource.getFullPath());
	}

	/**
	 * This method is invoked by a SyncSetInput provider when the 
	 * provider is starting to provide new input to the SyncSet
	 */
	/* package */ void beginInput() {
		resetChanges();
	}
	
	/**
	 * This method is invoked by a SyncSetInput provider when the 
	 * provider is done providing new input to the SyncSet
	 */
	/* package */ void endInput() {
		fireChanges();
	}

	public int size() {
		return resources.size();		
	}
	
	public SyncInfoStatistics getStatistics() {
		return statistics;
	}
}
