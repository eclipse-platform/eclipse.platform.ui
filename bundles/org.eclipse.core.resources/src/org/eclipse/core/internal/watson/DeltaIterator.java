package org.eclipse.core.internal.watson;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.utils.Assert;
/**
 * An implementation of the visitor pattern, for visiting the elements
 * of an ElementTreeDelta.
 */
public class DeltaIterator {
	/** pre-order traversal means visit the root first, then the children */
	public static final int PRE_ORDER = 0;

	/** post-order means visit the children, and then the root */
	public static final int POST_ORDER = 1;

	/** traversal order */
	private final int order;

	private ElementTreeDelta elementTreeDelta;
	private DeltaDataTree deltaTree;
	private ElementTree oldTree;
	private ElementTree newTree;
/**
 * Creates a new DeltaIterator that traverses the tree in pre order.
 */
public DeltaIterator() {
	order = PRE_ORDER;
}
/**
 * Creates a new DeltaIterator that traverses the tree in the specified manner.
 */
public DeltaIterator(int order) {
	this.order = order;
}
/**
 * Initializes the iterator
 */
private void initialize(ElementTreeDelta tree) {
	elementTreeDelta = tree;
	deltaTree = tree.getDeltaTree();
	oldTree = tree.getParent();
	newTree = tree.getElementTree();
}
/**
 * Iterates through the given element tree delta and visits each element,
 * passing in the element's path and element object.
 */
public void iterate(ElementTreeDelta tree, IDeltaVisitor visitor) {
	iterate(tree, visitor, Path.ROOT);
}
/**
 * Iterates through the given element tree delta and visits each element
 * in the subtree rooted at the given path, passing in the element's path 
 * and element object.
 */
public void iterate(ElementTreeDelta tree, IDeltaVisitor visitor, IPath path) {
	initialize(tree);

	/* don't visit the root element */
	if (path.isRoot()) {
		IPath[] children = deltaTree.getChildren(path);
		for (int i = 0; i < children.length; i++) {
			iterate(visitor, children[i], NodeComparison.K_CHANGED);
		}
	} else {
		iterate(visitor, path);
	}
}
/**
 * Starts the iteration at the provided entry point.  If the entry
 * point is not in the real delta, the iteration terminates.
 */
private void iterate(IDeltaVisitor visitor, IPath path) {
	/* find which tree this element is in */
	DataTreeNode node = (DataTreeNode)deltaTree.findNodeAt(path);

	if (node == null) {
		/* look in old tree */
		if (oldTree.includes(path)) {
			iterate(visitor, path, NodeComparison.K_ADDED);
		} else {
			/* look in new tree */
			if (newTree.includes(path)) {
				iterate(visitor, path, NodeComparison.K_REMOVED);
			}
		}
	} else {
		/* look in the delta tree */
		iterate(visitor, path, NodeComparison.K_CHANGED);
	}
}
/**
 * Iterates from the given node, based on the kind of change in the parent.
 */
private void iterate(IDeltaVisitor visitor, IPath path, int parentChange) {
	int comparison = 0, realChange = 0;
	Object oldData = null;
	Object newData = null;
	IPath[] children = null;
	
	switch (parentChange) {
		case NodeComparison.K_ADDED:
			/* look in the new tree only */
			comparison = realChange = parentChange;
			newData = newTree.getElementData(path);
			children = newTree.getChildren(path);
			break;
		case NodeComparison.K_REMOVED:
			/* look in the old tree only */
			comparison = realChange = parentChange;
			oldData = oldTree.getElementData(path);
			children = oldTree.getChildren(path);
			break;
		case NodeComparison.K_CHANGED:
			/* look in delta tree */
			NodeComparison info = (NodeComparison)deltaTree.getData(path);
			comparison = info.getUserComparison();

			realChange = info.getComparison();
			children = deltaTree.getChildren(path);
			oldData = info.getOldData();
			newData = info.getNewData();
	}

	Assert.isNotNull(children);

	boolean visitChildren = true;
	if (order == PRE_ORDER) {
		visitChildren = visitor.visitElement(
			elementTreeDelta, path, oldData, newData, comparison);
	}

	if (visitChildren) {
		for (int i = 0; i < children.length; i++) {
			iterate(visitor, children[i], realChange);
		}
	}

	if (order == POST_ORDER) {
		visitor.visitElement(elementTreeDelta, path, oldData, newData, comparison);
	}
}
}
