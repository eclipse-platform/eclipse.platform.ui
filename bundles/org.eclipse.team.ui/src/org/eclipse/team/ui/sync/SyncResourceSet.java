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
package org.eclipse.team.ui.sync;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.subscribers.SyncInfo;
import org.eclipse.team.internal.ui.sync.views.SyncResource;

/**
 * @author Administrator
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SyncResourceSet {

	Set set = new HashSet();
	
	public SyncResourceSet(SyncResource[] resources) {
		set.addAll(Arrays.asList(resources));
	}
	/**
	 * Returns true if there are any conflicting nodes in the set, and
	 * false otherwise.
	 */
	public boolean hasConflicts() {
		return hasNodes(new SyncInfoDirectionFilter(SyncInfo.CONFLICTING));
	}
	
	/**
	 * Returns true if this sync set has incoming changes.
	 * Note that conflicts are not considered to be incoming changes.
	 */
	public boolean hasIncomingChanges() {
		return hasNodes(new SyncInfoDirectionFilter(SyncInfo.INCOMING));
	}

	/**
	 * Returns true if this sync set has outgoing changes.
	 * Note that conflicts are not considered to be outgoing changes.
	 */
	public boolean hasOutgoingChanges() {
		return hasNodes(new SyncInfoDirectionFilter(SyncInfo.OUTGOING));
	}
	
	/**
	 * Returns true if this sync set has auto-mergeable conflicts.
	 */
	public boolean hasAutoMergeableConflicts() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			if ((node.getKind() & SyncInfo.AUTOMERGE_CONFLICT) != 0) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes all conflicting nodes from this set.
	 */
	public void removeConflictingNodes() {
		rejectNodes(new SyncInfoDirectionFilter(SyncInfo.CONFLICTING));
	}
	/**
	 * Removes all outgoing nodes from this set.
	 */
	public void removeOutgoingNodes() {
		rejectNodes(new SyncInfoDirectionFilter(SyncInfo.OUTGOING));
	}
	/**
	 * Removes all incoming nodes from this set.
	 */
	public void removeIncomingNodes() {
		rejectNodes(new SyncInfoDirectionFilter(SyncInfo.INCOMING));
	}
	
	/**
	 * Removes all nodes from this set that are not auto-mergeable conflicts
	 */
	public void removeNonMergeableNodes() {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			if ((node.getKind() & SyncInfo.MANUAL_CONFLICT) != 0) {
				it.remove();
			} else if (node.getChangeDirection() != SyncInfo.CONFLICTING) {
				it.remove();
			}
		}
	}

	/**
	 * Indicate whether the set has nodes matching the given filter
	 */
	public boolean hasNodes(SyncInfoFilter filter) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			SyncInfo info = node.getSyncInfo();
			if (info != null && filter.select(info)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Removes all nodes from this set that do not match the given filter
	 */
	public void selectNodes(SyncInfoFilter filter) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			SyncInfo info = node.getSyncInfo();
			if (info == null || !filter.select(info)) {
				it.remove();
			}
		}
	}
	
	/**
	 * Removes all nodes from this set that match the given filter
	 */
	public void rejectNodes(SyncInfoFilter filter) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			SyncInfo info = node.getSyncInfo();
			if (info != null && filter.select(info)) {
				it.remove();
			}
		}
	}
	
	/**
	 * Return all nodes in this set that match the given filter
	 */
	public SyncResource[] getNodes(SyncInfoFilter filter) {
		List result = new ArrayList();
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			SyncInfo info = node.getSyncInfo();
			if (info != null && filter.select(info)) {
				result.add(node);
			}
		}
		return (SyncResource[]) result.toArray(new SyncResource[result.size()]);
	}
	
	/**
	 * 
	 */
	public SyncResource[] getSyncResources() {
		return (SyncResource[]) set.toArray(new SyncResource[set.size()]);
	}
	
	/**
	 * Returns the resources from all the nodes in this set.
	 */
	public IResource[] getResources() {
		SyncResource[] changed = getSyncResources();
		IResource[] resources = new IResource[changed.length];
		for (int i = 0; i < changed.length; i++) {
			resources[i] = changed[i].getResource();
		}
		return resources;
	}
	
	/**
	 * @return
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}
	
	/**
	 * @param resources
	 */
	public void removeResources(IResource[] resources) {
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			removeResource(resource);
		}
	}
	
	/**
	 * @param resource
	 */
	private void removeResource(IResource resource) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			if (node.getResource().equals(resource)) {
				it.remove();
				// short-circuit the operation once a match is found
				return;
			}
		}
	}
	/**
	 * @return
	 */
	public int size() {
		return set.size();
	}
	/**
	 * @param file
	 * @return
	 */
	public SyncResource getNodeFor(IResource resource) {
		for (Iterator it = set.iterator(); it.hasNext();) {
			SyncResource node = (SyncResource)it.next();
			if (node.getResource().equals(resource)) {
				return node;
			}
		}
		return null;
	}
	
}
