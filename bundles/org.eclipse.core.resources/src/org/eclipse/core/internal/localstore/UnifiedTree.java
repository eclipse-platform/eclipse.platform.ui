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
package org.eclipse.core.internal.localstore;

import java.util.*;
import org.eclipse.core.filesystem.*;
import org.eclipse.core.internal.resources.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Queue;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Represents the workspace's tree merged with the file system's tree.
 */
public class UnifiedTree {
	/** tree's root */
	protected IResource root;

	/** tree's actual level */
	protected int level;

	/**
	 * True if the level of the children of the current node are valid according
	 * to the requested refresh depth, false otherwise
	 */
	protected boolean childLevelValid = false;

	/** our queue */
	protected Queue queue;

	/** Spare node objects available for reuse */
	protected ArrayList freeNodes = new ArrayList();

	/** special node to mark the beginning of a level in the tree */
	protected static final UnifiedTreeNode levelMarker = new UnifiedTreeNode(null, null, null, null, false);

	/** special node to mark the separation of a node's children */
	protected static final UnifiedTreeNode childrenMarker = new UnifiedTreeNode(null, null, null, null, false);

	/** Singleton to indicate no local children */
	private static final IResource[] NO_RESOURCES = new IResource[0];
	private static final IFileInfo[] NO_CHILDREN = new IFileInfo[0];
	private static final Iterator EMPTY_ITERATOR = Collections.EMPTY_LIST.iterator();

	/**
	 * The root must only be a file or a folder.
	 */
	public UnifiedTree(IResource root) {
		setRoot(root);
	}

	public void accept(IUnifiedTreeVisitor visitor) throws CoreException {
		accept(visitor, IResource.DEPTH_INFINITE);
	}

	public void accept(IUnifiedTreeVisitor visitor, int depth) throws CoreException {
		Assert.isNotNull(root);
		initializeQueue();
		setLevel(0, depth);
		while (!queue.isEmpty()) {
			UnifiedTreeNode node = (UnifiedTreeNode) queue.remove();
			if (isChildrenMarker(node))
				continue;
			if (isLevelMarker(node)) {
				if (!setLevel(getLevel() + 1, depth))
					break;
				continue;
			}
			if (visitor.visit(node))
				addNodeChildrenToQueue(node);
			else
				removeNodeChildrenFromQueue(node);
			//allow reuse of the node
			freeNodes.add(node);
		}
	}

	protected void addChildren(UnifiedTreeNode node) {
		Resource parent = (Resource) node.getResource();

		// is there a possibility to have children? 
		int parentType = parent.getType();
		if (parentType == IResource.FILE && !node.isFolder())
			return;

		// get the list of resources in the file system 
		// don't ask for local children if we know it doesn't exist locally
		IFileInfo[] list = node.existsInFileSystem() ? getLocalList(node) : NO_CHILDREN;
		int localIndex = 0;

		// See if the children of this resource have been computed before 
		ResourceInfo resourceInfo = parent.getResourceInfo(false, false);
		int flags = parent.getFlags(resourceInfo);
		boolean unknown = ResourceInfo.isSet(flags, ICoreConstants.M_CHILDREN_UNKNOWN);

		// get the list of resources in the workspace 
		if (!unknown && (parentType == IResource.FOLDER || parentType == IResource.PROJECT) && parent.exists(flags, true)) {
			IResource target = null;
			UnifiedTreeNode child = null;
			IResource[] members;
			try {
				members = ((IContainer) parent).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
			} catch (CoreException e) {
				members = NO_RESOURCES;
			}
			int workspaceIndex = 0;
			//iterate simultaneously over file system and workspace members
			while (workspaceIndex < members.length) {
				target = members[workspaceIndex];
				String name = target.getName();
				String localName = (list != null && localIndex < list.length) ? list[localIndex].getName() : null;
				int comp = localName != null ? name.compareTo(localName) : -1;
				//special handling for linked resources
				if (parentType == IResource.PROJECT && target.isLinked()) {
					//child will be null if location is undefined
					child = createChildForLinkedResource(target);
					workspaceIndex++;
					//if there is a matching local file, skip it - it will be blocked by the linked resource
					if (comp == 0)
						localIndex++;
				} else if (comp == 0) {
					// resource exists in workspace and file system
					child = createNode(target, null, list[localIndex], true);
					localIndex++;
					workspaceIndex++;
				} else if (comp > 0) {
					// resource exists only in file system 
					child = createChildNodeFromFileSystem(node, list[localIndex]);
					localIndex++;
				} else {
					// resource exists only in the workspace
					child = createNode(target, null, null, true);
					workspaceIndex++;
				}
				if (child != null)
					addChildToTree(node, child);
			}
		}

		/* process any remaining resource from the file system */
		addChildrenFromFileSystem(node, list, localIndex);

		/* Mark the children as now known */
		if (unknown) {
			// Don't open the info - we might not be inside a workspace-modifying operation
			resourceInfo = parent.getResourceInfo(false, false);
			if (resourceInfo != null)
				resourceInfo.clear(ICoreConstants.M_CHILDREN_UNKNOWN);
		}

		/* if we added children, add the childMarker separator */
		if (node.getFirstChild() != null)
			addChildrenMarker();
	}

	/**
	 * Creates a tree node for a resource that is linked in a different file system location.
	 */
	protected UnifiedTreeNode createChildForLinkedResource(IResource target) {
		IFileStore store = ((Resource) target).getStore();
		return createNode(target, store, store.fetchInfo(), true);
	}

	protected void addChildrenFromFileSystem(UnifiedTreeNode node, IFileInfo[] childInfos, int index) {
		if (childInfos == null)
			return;
		for (int i = index; i < childInfos.length; i++)
			addChildToTree(node, createChildNodeFromFileSystem(node, childInfos[i]));
	}

	protected void addChildrenMarker() {
		addElementToQueue(childrenMarker);
	}

	protected void addChildToTree(UnifiedTreeNode node, UnifiedTreeNode child) {
		if (node.getFirstChild() == null)
			node.setFirstChild(child);
		addElementToQueue(child);
	}

	protected void addElementToQueue(UnifiedTreeNode target) {
		queue.add(target);
	}

	protected void addNodeChildrenToQueue(UnifiedTreeNode node) {
		/* if the first child is not null we already added the children */
		/* If the children won't be at a valid level for the refresh depth, don't bother adding them */
		if (!childLevelValid || node.getFirstChild() != null)
			return;
		addChildren(node);
		if (queue.isEmpty())
			return;
		//if we're about to change levels, then the children just added
		//are the last nodes for their level, so add a level marker to the queue
		UnifiedTreeNode nextNode = (UnifiedTreeNode) queue.peek();
		if (isChildrenMarker(nextNode))
			queue.remove();
		nextNode = (UnifiedTreeNode) queue.peek();
		if (isLevelMarker(nextNode))
			addElementToQueue(levelMarker);
	}

	protected void addRootToQueue() {
		IFileStore rootStore = ((Resource)root).getStore();
		UnifiedTreeNode node = createNode(root, rootStore, rootStore.fetchInfo(), root.exists());
		if (!node.existsInFileSystem() && !node.existsInWorkspace())
			return;
		addElementToQueue(node);
	}

	/**
	 * Creates a child node for a location in the file system. Does nothing and returns null if the location does not correspond to a valid file/folder. 
	 */
	protected UnifiedTreeNode createChildNodeFromFileSystem(UnifiedTreeNode parent, IFileInfo info) {
		IPath childPath = parent.getResource().getFullPath().append(info.getName());
		int type = info.isDirectory() ? IResource.FOLDER : IResource.FILE;
		IResource target = getWorkspace().newResource(childPath, type);
		return createNode(target, null, info, false);
	}

	/**
	 * Factory method for creating a node for this tree.  If the file exists on
	 * disk, either the parent store or child store can be provided. Providing
	 * only the parent store avoids creation of the child store in cases where
	 * it is not needed. The store object is only needed for directories for
	 * simple file system traversals, so this avoids creating store objects
	 * for all files.
	 */
	protected UnifiedTreeNode createNode(IResource resource, IFileStore store, IFileInfo info, boolean existsWorkspace) {
		//first check for reusable objects
		UnifiedTreeNode node = null;
		int size = freeNodes.size();
		if (size > 0) {
			node = (UnifiedTreeNode) freeNodes.remove(size - 1);
			node.reuse(this, resource, store, info, existsWorkspace);
			return node;
		}
		//none available, so create a new one
		return new UnifiedTreeNode(this, resource, store, info, existsWorkspace);
	}

	protected Iterator getChildren(UnifiedTreeNode node) {
		/* if first child is null we need to add node's children to queue */
		if (node.getFirstChild() == null)
			addNodeChildrenToQueue(node);

		/* if the first child is still null, the node does not have any children */
		if (node.getFirstChild() == null)
			return EMPTY_ITERATOR;

		/* get the index of the first child */
		int index = queue.indexOf(node.getFirstChild());

		/* if we do not have children, just return an empty enumeration */
		if (index == -1)
			return EMPTY_ITERATOR;

		/* create an enumeration with node's children */
		List result = new ArrayList(10);
		while (true) {
			UnifiedTreeNode child = (UnifiedTreeNode) queue.elementAt(index);
			if (isChildrenMarker(child))
				break;
			result.add(child);
			index = queue.increment(index);
		}
		return result.iterator();
	}

	protected int getLevel() {
		return level;
	}

	protected IFileInfo[] getLocalList(UnifiedTreeNode node) {
		IFileInfo[] list;
		try {
			list = node.getStore().childInfos(IFileStoreConstants.NONE, null);
		} catch (CoreException e) {
			//treat failure to access the directory as a non-existent directory
			return null;
		}
		if (list == null)
			return null;
		int size = list.length;
		if (size > 1)
			quickSort(list, 0, size - 1);
		return list;
	}

	protected Workspace getWorkspace() {
		return (Workspace) root.getWorkspace();
	}

	/**
	 * Increases the current tree level by one. Returns true if the new
	 * level is still valid for the given depth
	 */
	protected boolean setLevel(int newLevel, int depth) {
		level = newLevel;
		childLevelValid = isValidLevel(level + 1, depth);
		return isValidLevel(level, depth);
	}

	protected void initializeQueue() {
		//initialize the queue
		if (queue == null)
			queue = new Queue(100, false);
		else
			queue.reset();
		//initialize the free nodes list
		if (freeNodes == null)
			freeNodes = new ArrayList(100);
		else
			freeNodes.clear();
		addRootToQueue();
		addElementToQueue(levelMarker);
	}

	protected boolean isChildrenMarker(UnifiedTreeNode node) {
		return node == childrenMarker;
	}

	protected boolean isLevelMarker(UnifiedTreeNode node) {
		return node == levelMarker;
	}

	protected boolean isValidLevel(int currentLevel, int depth) {
		switch (depth) {
			case IResource.DEPTH_INFINITE :
				return true;
			case IResource.DEPTH_ONE :
				return currentLevel <= 1;
			case IResource.DEPTH_ZERO :
				return currentLevel == 0;
			default :
				return currentLevel + 1000 <= depth;
		}
	}

	/**
	 * Remove from the last element of the queue to the first child of the
	 * given node.
	 */
	protected void removeNodeChildrenFromQueue(UnifiedTreeNode node) {
		UnifiedTreeNode first = node.getFirstChild();
		if (first == null)
			return;
		while (true) {
			if (first.equals(queue.removeTail()))
				break;
		}
		node.setFirstChild(null);
	}

	public void setRoot(IResource root) {
		this.root = root;
	}

	/**
	 * Sorts the given array of strings in place.  This is
	 * not using the sorting framework to avoid casting overhead.
	 */
	protected void quickSort(IFileInfo[] infos, int left, int right) {
		int originalLeft = left;
		int originalRight = right;
		IFileInfo mid = infos[(left + right) / 2];
		do {
			while (mid.compareTo(infos[left]) > 0)
				left++;
			while (infos[right].compareTo(mid) > 0)
				right--;
			if (left <= right) {
				IFileInfo tmp = infos[left];
				infos[left] = infos[right];
				infos[right] = tmp;
				left++;
				right--;
			}
		} while (left <= right);
		if (originalLeft < right)
			quickSort(infos, originalLeft, right);
		if (left < originalRight)
			quickSort(infos, left, originalRight);
		return;
	}
}
