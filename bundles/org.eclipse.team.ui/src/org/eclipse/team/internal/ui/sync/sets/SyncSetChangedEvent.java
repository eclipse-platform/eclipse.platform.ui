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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.SyncInfo;

/**
 * This event keeps track of the changes in a sync set
 */
public class SyncSetChangedEvent {
	
	private SyncSet set;
	
	// List that accumulate changes
	// SyncInfo
	private Set changedResources = new HashSet();
	private Set removedResources = new HashSet();
	private Set addedResources = new HashSet();
	
	// IResources
	private Set removedRoots = new HashSet();
	private Set addedRoots = new HashSet();
	
	private boolean reset = false;
	
	public SyncSetChangedEvent(SyncSet set) {
		super();
		this.set = set;
	}

	/* package */ void added(SyncInfo info) {
		addedResources.add(info);
	}
	
	/* package */ void removed(IResource resource) {
		removedResources.add(resource);
	}
	
	/* package */ void changed(SyncInfo info) {
		changedResources.add(info);
	}
	
	public void removedRoot(IResource root) {
		if (addedRoots.contains(root)) {
			// The root was added and removed which is a no-op
			addedRoots.remove(root);
		} else {
			// check if the root is a child of an existing root
			// (in which case it need not be added).
			// Also, remove any exisiting roots that are children
			// of the new root
			for (Iterator iter = removedRoots.iterator(); iter.hasNext();) {
				IResource element = (IResource) iter.next();
				// check if the root is already in the list
				if (root.equals(element)) return;
				if (isParent(root, element)) {
					// the root invalidates the current element
					iter.remove();
				} else if (isParent(element, root)) {
					// the root is a child of an existing element
					return;
				}
			}
			removedRoots.add(root);
		}
	}
	
	private boolean isParent(IResource root, IResource element) {
		return root.getFullPath().isPrefixOf(element.getFullPath());
	}

	public void addedRoot(IResource parent) {
		if (removedRoots.contains(parent)) {
			// The root was re-added which is a no-op
			removedRoots.remove(parent);
		} else {
			// TODO: May be added underneath another added root
			addedRoots.add(parent);
		}
		
	}

	public SyncInfo[] getAddedResources() {
		return (SyncInfo[]) addedResources.toArray(new SyncInfo[addedResources.size()]);
	}

	public IResource[] getAddedRoots() {
		return (IResource[]) addedRoots.toArray(new IResource[addedRoots.size()]);
	}

	public SyncInfo[] getChangedResources() {
		return (SyncInfo[]) changedResources.toArray(new SyncInfo[changedResources.size()]);
	}

	public IResource[] getRemovedResources() {
		return (IResource[]) removedResources.toArray(new IResource[removedResources.size()]);
	}

	public IResource[] getRemovedRoots() {
		return (IResource[]) removedRoots.toArray(new IResource[removedRoots.size()]);
	}
		
	public SyncSet getSet() {
		return set;
	}

	public void reset() {
		reset = true;
	}
	
	public boolean isReset() {
		return reset;
	}
	
	public boolean isEmpty() {
		return changedResources.isEmpty() && removedResources.isEmpty() && addedResources.isEmpty() && removedRoots.isEmpty() && addedRoots.isEmpty();
	}
}
