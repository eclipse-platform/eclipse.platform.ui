/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.internal.localstore;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.internal.utils.*;
import java.util.*;
/**
 * Represents the workspace's tree merged with the file system's tree.
 */
public class UnifiedTree {
	/** tree's root */
	protected IResource root;

	/** cache root's location */
	protected IPath rootLocalLocation;

	/** tree's actual level */
	protected int level;

	/** our queue */
	protected Queue queue;
	
	/** Spare node objects available for reuse */
	protected ArrayList freeNodes = new ArrayList();

	/** sorter */
	protected Sorter sorter;

	/** special node to mark the beginning of a level in the tree */
	protected static final UnifiedTreeNode levelMarker = new UnifiedTreeNode(null, null, 0, null, null, false);

	/** special node to mark the separation of a node's children */
	protected static final UnifiedTreeNode childrenMarker = new UnifiedTreeNode(null, null, 0, null, null, false);
public UnifiedTree() {
}
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
	level = 0;
	while (isValidLevel(level, depth) && !queue.isEmpty()) {
		UnifiedTreeNode node = (UnifiedTreeNode)queue.remove();
		if (isChildrenMarker(node))
			continue;
		if (isLevelMarker(node)) {
			level++;
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
protected void addChildren(UnifiedTreeNode node) throws CoreException {
	IResource parent = node.getResource();

	/* is there a possibility to have children? */
	if (parent.getType() == IResource.FILE && node.isFile())
		return;

	/* get the list of resources in the file system */
	String parentLocalLocation = node.getLocalLocation();
	Object[] list = getLocalList(node, parentLocalLocation);
	int index = 0;

	/* get the list of resources in the workspace */
	if (node.existsInWorkspace() && (parent.getType() == IResource.FOLDER || parent.getType() == IResource.PROJECT)) {
		IResource target = null;
		boolean next = true;
		UnifiedTreeNode child = null;
		IResource[] members = ((IContainer) parent).members(IContainer.INCLUDE_TEAM_PRIVATE_MEMBERS);
		int i = 0;
		while (true) {
			if (next) {
				if (i >= members.length)
					break;
				target = members[i++];
			}
			String name = target.getName();
			String localName = (list != null && index < list.length) ? (String) list[index] : null;
			int comp = localName != null ? name.compareTo(localName) : -1;
			if (comp == 0) {
				// resource exists in workspace and file system
				String localLocation = createChildLocation(parentLocalLocation, localName);
				long stat = CoreFileSystemLibrary.getStat(localLocation);
				child = createNode(target, stat, localLocation, localName, true);
				index++;
				next = true;
			} else
				if (comp > 0) {
					// resource exists only in file system
					child = createChildNodeFromFileSystem(node, parentLocalLocation, localName);
					index++;
					next = false;
				} else {
					// resource exists only in the workspace
					child = createNode(target, 0, null, null, true);
					next = true;
				}
			addChildToTree(node, child);
		}
	}

	/* process any remaining resource from the file system */
	addChildrenFromFileSystem(node, parentLocalLocation, list, index);

	/* if we added children, add the childMarker separator */
	if (node.getFirstChild() != null)
		addChildrenMarker();
}
protected void addChildrenFromFileSystem(UnifiedTreeNode node, String parentLocalLocation, Object[] list, int index) throws CoreException {
	if (list == null)
		return;
	for (int i = index; i < list.length; i++) {
		String localName = (String) list[i];
		UnifiedTreeNode child = createChildNodeFromFileSystem(node, parentLocalLocation, localName);
		addChildToTree(node, child);
	}
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
}/**
 * Creates a string representing the OS path for the given parent and child name.
 */
protected String createChildLocation(String parentLocation, String childLocation) {
	StringBuffer buffer = new StringBuffer(parentLocation.length() + childLocation.length() + 1);
	buffer.append(parentLocation);
	buffer.append(java.io.File.separatorChar);
	buffer.append(childLocation);
	return buffer.toString();
}

protected void addNodeChildrenToQueue(UnifiedTreeNode node) throws CoreException {
	/* if the first child is not null we already added the children */
	if (node.getFirstChild() != null)
		return;
	addChildren(node);
	if (queue.isEmpty())
		return;
	//if we're about to change levels, then the children just added
	//are the last nodes for their level, so add a level marker to the queue
	UnifiedTreeNode nextNode = (UnifiedTreeNode)queue.peek();
	if (isLevelMarker(nextNode))
		addElementToQueue(levelMarker);
}
protected void addRootToQueue() throws CoreException {
	String rootLocationString = rootLocalLocation.toOSString();
	long stat = CoreFileSystemLibrary.getStat(rootLocationString);
	UnifiedTreeNode node = createNode(root, stat, rootLocationString, rootLocalLocation.lastSegment(), root.exists());
	if (!node.existsInFileSystem() && !node.existsInWorkspace())
		return;
	addElementToQueue(node);
}
protected UnifiedTreeNode createChildNodeFromFileSystem(UnifiedTreeNode parent, String parentLocalLocation, String childName) throws CoreException {
	IPath childPath = parent.getResource().getFullPath().append(childName);
	String location = createChildLocation(parentLocalLocation, childName);
	long stat = CoreFileSystemLibrary.getStat(location);
	int type = CoreFileSystemLibrary.isFile(stat) ? IResource.FILE : IResource.FOLDER;
	IResource target = getWorkspace().newResource(childPath, type);
	return createNode(target, stat, location, childName, false);
}
protected UnifiedTreeNode createNodeFromFileSystem(IPath path, String location, String localName) throws CoreException {
	long stat = CoreFileSystemLibrary.getStat(location);
	UnifiedTreeNode node = createNode(null, stat, location, localName, false);
	int type = node.isFile() ? IResource.FILE : IResource.FOLDER;
	IResource target = getWorkspace().newResource(path, type);
	node.setResource(target);
	return node;
}
/**
 * Factory method for creating a node for this tree.
 */
protected UnifiedTreeNode createNode(IResource resource, long stat, String localLocation, String localName, boolean existsWorkspace) {
	//first check for reusable objects
	UnifiedTreeNode node = null;
	int size = freeNodes.size();
	if (size > 0) {
		node = (UnifiedTreeNode)freeNodes.remove(size-1);
		node.reuse(this, resource, stat, localLocation, localName, existsWorkspace);
		return node;
	}
	//none available, so create a new one
	return new UnifiedTreeNode(this, resource, stat, localLocation, localName, existsWorkspace);
}
protected Enumeration getChildren(UnifiedTreeNode node) throws CoreException {
	/* if first child is null we need to add node's children to queue */
	if (node.getFirstChild() == null)
		addNodeChildrenToQueue(node);

	/* if the first child is still null, the node does not have any children */
	if (node.getFirstChild() == null)
		return EmptyEnumeration.getEnumeration();

	/* get the index of the first child */
	int index = queue.indexOf(node.getFirstChild());

	/* if we do not have children, just return an empty enumeration */
	if (index == -1)
		return EmptyEnumeration.getEnumeration();

	/* create an enumeration with node's children */
	List result = new ArrayList(10);
	while (true) {
		UnifiedTreeNode child = (UnifiedTreeNode) queue.elementAt(index);
		if (isChildrenMarker(child))
			break;
		result.add(child);
		index = queue.increment(index);
	}
	return Collections.enumeration(result);
}

protected  String getLocalLocation(IResource target) {
	int segments = target.getFullPath().matchingFirstSegments(root.getFullPath());
	return rootLocalLocation.append(target.getFullPath().removeFirstSegments(segments)).toOSString();
}

protected int getLevel() {
	return level;
}
protected Object[] getLocalList(UnifiedTreeNode node, String location) {
	if (node.isFile())
		return null;
	String[] list = new java.io.File(location).list();
	if (list == null)
		return list;
	int size = list.length;
	if (size > 1)
		quickSort(list, 0 , size-1);
	return list;
}
protected Workspace getWorkspace() {
	return (Workspace) root.getWorkspace();
}
protected void initializeQueue() throws CoreException {
	//init the queue
	if (queue == null)
		queue = new Queue(100, false);
	else
		queue.reset();
	//init the free nodes list
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
protected boolean isValidLevel(int level, int depth) {
	switch (depth) {
		case IResource.DEPTH_INFINITE :
			return true;
		case IResource.DEPTH_ONE :
			return level <= 1;
		case IResource.DEPTH_ZERO :
			return level == 0;
		default:
			return false;
	}
}
/**
 * Remove from the last element of the queue to the first child of the
 * given node.
 */
protected void removeNodeChildrenFromQueue(UnifiedTreeNode node) throws CoreException {
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
	this.rootLocalLocation = root.getLocation();
}
/**
 * Sorts the given array of strings in place.  This is
 * not using the sorting framework to avoid casting overhead.
 */
protected void quickSort(String[] strings, int left, int right) {
	int originalLeft = left;
	int originalRight = right;
	String mid = strings[ (left + right) / 2];
	do {
		while (mid.compareTo(strings[left]) > 0)
			left++;
		while (strings[right].compareTo(mid) > 0)
			right--;
		if (left <= right) {
			String tmp = strings[left];
			strings[left] = strings[right];
			strings[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (originalLeft < right)
		quickSort(strings, originalLeft, right);
	if (left < originalRight)
		quickSort(strings, left, originalRight);
	return;
}
}
