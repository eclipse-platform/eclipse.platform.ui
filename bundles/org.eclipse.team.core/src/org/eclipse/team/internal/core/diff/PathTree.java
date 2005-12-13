/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.diff;

import java.util.*;

import org.eclipse.core.runtime.IPath;

/**
 * A tree of objects keyed by path
 */
public class PathTree {
	
	private Map objects = new HashMap();
	
	private Map parents = new HashMap();

	/**
	 * Return the object at the given path or <code>null</code>
	 * if there is no object at that path
	 * @param path the path
	 * @return the object at the given path or <code>null</code>
	 */
	public synchronized Object get(IPath path) {
		return objects.get(path);
	}
	
	/**
	 * Put the object at the given path. Return the
	 * previous object at that path or <code>null</code>
	 * if the path did not previously have an object.
	 * @param path the path of the object
	 * @param object the object
	 * @return the previous object at that path or <code>null</code>
	 */
	public synchronized Object put(IPath path, Object object) {
		Object previous = get(path);
		objects.put(path, object);
		if(previous == null) {
			addToParents(path, path);
		}
		return previous;
	}
	
	/**
	 * Remove the removed object at the given path and return
	 * the removed object or <code>null</code> if no
	 * object was removed.
	 * @param path the path  to remove
	 * @return the removed object at the given path and return
	 * the removed object or <code>null</code>
	 */
	public synchronized Object remove(IPath path) {
		Object previous = objects.remove(path);
		if(previous == null) {
			removeFromParents(path, path);
		}
		return previous;
		
	}
	
	/**
	 * Return whether the given path has children in the tree
	 * @param path
	 * @return
	 */
	public synchronized boolean hasChildren(IPath path) {
		if (path.isEmpty()) return !objects.isEmpty();
		Set allDescendants = (Set)parents.get(path);
		return (allDescendants != null && !allDescendants.isEmpty());
	}
	
	/**
	 * Return the paths for any children of the given path in this set.
	 * @param path the path
	 * @return the paths for any children of the given path in this set
	 */
	public synchronized IPath[] getChildren(IPath path) {
		// OPTIMIZE: could be optimized so that we don't traverse all the deep 
		// children to find the immediate ones.
		Set children = new HashSet();
		Set possibleChildren = (Set)parents.get(path);
		if(possibleChildren != null) {
			for (Iterator it = possibleChildren.iterator(); it.hasNext();) {
				Object next = it.next();
				IPath descendantPath = (IPath)next;
				IPath childPath = null;
				if(descendantPath.segmentCount() == (path.segmentCount() +  1)) {
					childPath = descendantPath;
				} else if (descendantPath.segmentCount() > path.segmentCount()) {
					childPath = path.append(descendantPath.segment(path.segmentCount()));
				}
				if (childPath != null) {
					children.add(childPath);
				}
			}
		}
		return (IPath[]) children.toArray(new IPath[children.size()]);
	}
	
	private boolean addToParents(IPath path, IPath parent) {
		// this flag is used to indicate if the parent was previously in the set
		boolean addedParent = false;
		if (path == parent) {
			// this is the leaf that was just added
			addedParent = true;
		} else {
			Set children = (Set)parents.get(parent);
			if (children == null) {
				children = new HashSet();
				parents.put(parent, children);
				// this is a new folder in the sync set
				addedParent = true;
			}
			children.add(path);
		}
		// if the parent already existed and the resource is new, record it
		if ((parent.segmentCount() == 0 || !addToParents(path, parent.removeLastSegments(1))) && addedParent) {
			// TODO: we may not need to record the removed subtree
			// internalAddedSubtreeRoot(parent);
		}
		return addedParent;
	}
	
	private boolean removeFromParents(IPath path, IPath parent) {
		// this flag is used to indicate if the parent was removed from the set
		boolean removedParent = false;
		Set children = (Set)parents.get(parent);
		if (children == null) {
			// this is the leaf
			removedParent = true;
		} else {
			children.remove(path);
			if (children.isEmpty()) {
				parents.remove(parent);
				removedParent = true;
			}
		}
		//	if the parent wasn't removed and the resource was, record it
		if ((parent .isEmpty() || !removeFromParents(path, parent.removeLastSegments(1))) && removedParent) {
			// TODO: may not need to record this
			//internalRemovedSubtreeRoot(parent);
		}
		return removedParent;
	}

	/**
	 * Clear all entries from the path tree.
	 */
	public void clear() {
		objects.clear();
		parents.clear();
	}

	/**
	 * Return whether the path tree is empty.
	 * @return whether the path tree is empty
	 */
	public boolean isEmpty() {
		return objects.isEmpty();
	}

	/**
	 * Return the paths in this tree that contain diffs.
	 * @return the paths in this tree that contain diffs.
	 */
	public IPath[] getPaths() {
		List result = new ArrayList();
		for (Iterator iter = objects.keySet().iterator(); iter.hasNext();) {
			IPath path = (IPath) iter.next();
			result.add(path);
		}
		return (IPath[]) result.toArray(new IPath[result.size()]);
	}

}
