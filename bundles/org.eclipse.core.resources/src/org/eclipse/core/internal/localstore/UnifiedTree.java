package org.eclipse.core.internal.localstore;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
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

	/** sorter */
	protected Sorter sorter;

	/** special node to mark the beginning of a level in the tree */
	protected static final UnifiedTreeNode levelMarker = new UnifiedTreeNode(null, null, 0, null, false);

	/** special node to mark the separation of a node's children */
	protected static final UnifiedTreeNode childrenMarker = new UnifiedTreeNode(null, null, 0, null, false);
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
	while (isValidLevel(level, depth) && !queueIsEmpty()) {
		UnifiedTreeNode node = removeElementFromQueue();
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
	}
}
protected void addChildren(UnifiedTreeNode node) throws CoreException {
	IResource parent = node.getResource();

	/* is there a possibility to have children? */
	if (parent.getType() == IResource.FILE && node.isFile())
		return;

	/* get the list of resources in the file system */
	IPath parentLocalLocation = getLocalLocation(parent);
	Object[] list = getLocalList(node, parentLocalLocation);
	int index = 0;

	/* get the list of resources in the workspace */
	if (node.existsInWorkspace() && (parent.getType() == IResource.FOLDER || parent.getType() == IResource.PROJECT)) {
		IResource target = null;
		boolean next = true;
		UnifiedTreeNode child = null;
		IResource[] members = ((IContainer) parent).members();
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
				long stat = CoreFileSystemLibrary.getStat(getLocalLocation(target).toOSString());
				child = new UnifiedTreeNode(this, target, stat, localName, true);
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
					child = new UnifiedTreeNode(this, target, 0, null, true);
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
protected void addChildrenFromFileSystem(UnifiedTreeNode node, IPath parentLocalLocation, Object[] list, int index) throws CoreException {
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
}
protected void addNodeChildrenToQueue(UnifiedTreeNode node) throws CoreException {
	/* if the first child is not null we already added the children */
	if (node.getFirstChild() != null)
		return;
	addChildren(node);
	if (queueIsEmpty())
		return;
	UnifiedTreeNode nextNode = getElementFromQueue();
	if (isLevelMarker(nextNode))
		addElementToQueue(nextNode);
}
protected void addRootToQueue() throws CoreException {
	long stat = CoreFileSystemLibrary.getStat(rootLocalLocation.toOSString());
	UnifiedTreeNode node = new UnifiedTreeNode(this, root, stat, rootLocalLocation.lastSegment(), root.exists());
	if (!node.existsInFileSystem() && !node.existsInWorkspace())
		return;
	addElementToQueue(node);
}
protected UnifiedTreeNode createChildNodeFromFileSystem(UnifiedTreeNode node, IPath parentLocalLocation, String childName) throws CoreException {
	IPath childPath = node.getResource().getFullPath().append(childName);
	IPath childLocalLocation = parentLocalLocation.append(childName);
	return createNode(childPath, childLocalLocation, childName);
}
protected UnifiedTreeNode createNode(IPath path, IPath location, String localName) throws CoreException {
	long stat = CoreFileSystemLibrary.getStat(location.toOSString());
	UnifiedTreeNode node = new UnifiedTreeNode(this, null, stat, localName, false);
	int type = node.isFile() ? IResource.FILE : IResource.FOLDER;
	IResource target = getWorkspace().newResource(path, type);
	node.setResource(target);
	return node;
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
protected UnifiedTreeNode getElementFromQueue() {
	return (UnifiedTreeNode) queue.peek();
}
protected int getLevel() {
	return level;
}
protected Object[] getLocalList(UnifiedTreeNode node, IPath location) {
	if (node.isFile())
		return null;
	String[] list = location.toFile().list();
	if (list == null)
		return list;
	return getSorter().sort(list);
}
protected IPath getLocalLocation(IResource target) {
	int segments = target.getFullPath().matchingFirstSegments(root.getFullPath());
	return rootLocalLocation.append(target.getFullPath().removeFirstSegments(segments));
}
/**
 * helper method to reduce garbage generation
 */
protected Sorter getSorter() {
	if (sorter == null) {
		sorter = new Sorter() {
			public boolean compare(Object elementOne, Object elementTwo) {
				return ((String) elementTwo).compareTo((String) elementOne) > 0;
			}
		};
	}
	return sorter;
}
protected Workspace getWorkspace() {
	return (Workspace) root.getWorkspace();
}
protected void initializeQueue() throws CoreException {
	if (queue == null)
		queue = new Queue(50, false);
	else
		queue.reset();
	addRootToQueue();
	addElementToQueue(levelMarker);
}
protected boolean isChildrenMarker(UnifiedTreeNode node) {
	return node.equals(childrenMarker);
}
protected boolean isLevelMarker(UnifiedTreeNode node) {
	return node.equals(levelMarker);
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
protected boolean queueIsEmpty() {
	return queue.isEmpty();
}
protected UnifiedTreeNode removeElementFromQueue() {
	return (UnifiedTreeNode) queue.remove();
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
}
