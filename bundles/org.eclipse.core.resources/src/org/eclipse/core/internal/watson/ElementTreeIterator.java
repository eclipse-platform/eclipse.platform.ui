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
package org.eclipse.core.internal.watson;

import org.eclipse.core.internal.dtree.AbstractDataTreeNode;
import org.eclipse.core.internal.dtree.DataTreeNode;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
/**
 * A class for performing operations on each element in an element tree.
 * For example, this can be used to print the contents of a tree.
 * <p>
 * A traversal order can optionally be specified if the user wants to iterate over the
 * tree in a certain way.  The traversal order may be passed to the constructor.
 * Note that in-order traversal does not make sense for non-binary trees, so the
 * only traversal orders available are pre-order and post-order.  The default is
 * pre-order traversal.
 * <p>
 * When using the <code>iterate()</code> function, an element tree 
 * and a visitor object must be
 * provided.  The visitor is called once for each node of the tree.  For each node,
 * the visitor is passed the entire tree, and the complete element ID of the
 * node, and the element object at that node.
 * <p>
 * <b>Example:</b>
<code><pre>
// printing a crude representation of the posterchild
IElementContentVisitor visitor=
	new IElementContentVisitor() {
	  public void visitElement(ElementTree tree, ElementID elementID, Object elementContents) {
		 System.out.println(elementID + " -> " + elementContent);
	  }
	});
new ElementTreeIterator().iterate(
	new PosterChild().getWorkspace().getElementTree(), visitor);
</pre></code>
 */
public class ElementTreeIterator implements IElementContentVisitor.IPathRequestor {
	//for path requestor
	private String[] segments = new String[10];
	private int nextFreeSegment;

	/* the tree being visited */
	private ElementTree tree;

	/**
	 * Method grow.
	 */
	private void grow() {
		//grow the segments array
		int oldLen = segments.length;
		String[] newPaths = new String[oldLen*2];
		System.arraycopy(segments, 0, newPaths, 0, oldLen);
		segments = newPaths;
	}
	/**
	 * Push the first "toPush" segments of this path.
	 */
	private void push(IPath path, int toPush) {
		if (toPush <= 0)
			return;
		for (int i = 0; i < toPush; i++) {
			if (nextFreeSegment >= segments.length) {
				grow();
			}
			segments[nextFreeSegment++] = path.segment(i);
		}
	}
	public IPath requestPath() {
		if (nextFreeSegment == 0)
			return Path.ROOT;
		int length = nextFreeSegment;
		for (int i = 0; i < nextFreeSegment; i++) {
			length += segments[i].length();
		}
		StringBuffer pathBuf = new StringBuffer(length);
		for (int i = 0; i < nextFreeSegment; i++) {
			pathBuf.append('/');
			pathBuf.append(segments[i]);
		}
		return new Path(null, pathBuf.toString());
	}
	
/**
 * Creates a new element tree iterator that using the default traversal order
 * (<code>PRE_ORDER</code>).
 */
public ElementTreeIterator() {
}
/**
 * Iterates through the given element tree and visit each element (node)
 * passing in the element's ID and element object.
 */
private void doIteration(DataTreeNode node, IElementContentVisitor visitor) {
	//push the name of this node to the requestor stack
	if (nextFreeSegment >= segments.length) {
		grow();
	}
	segments[nextFreeSegment++] = node.getName();

	//do the visit
	visitor.visitElement(tree, this, node.getData());
	
	//recurse
	AbstractDataTreeNode[] children = node.getChildren();
	for (int i = children.length; --i >= 0;) {
		doIteration((DataTreeNode)children[i], visitor);
	}
	
	//pop the segment from the requestor stack
	nextFreeSegment--;
	if (nextFreeSegment < 0)
		nextFreeSegment = 0;
}
/**
 * Iterates through the given element tree and visit each element (node)
 * passing in the element's ID and element object.
 */
private void doIterationWithPath(DataTreeNode node, IElementPathContentVisitor visitor, IPath path) {
	visitor.visitElement(tree, path, node.getData());
	AbstractDataTreeNode[] children = node.getChildren();
	for (int i = children.length; --i >= 0;) {
		doIterationWithPath((DataTreeNode)children[i], visitor, path.append(children[i].getName()));
	}
}
/**
 * Iterates through the given element tree and visit each element (node)
 * passing in the element's ID and element object.
 */
public void iterate(ElementTree tree, IElementContentVisitor visitor) {
	iterate(tree, visitor, Path.ROOT);
}
/**
 * Iterates through the given element tree and visits each element in the
 * subtree rooted at the given path.  The visitor is passed each element's 
 * path and data.
 */
public void iterate(ElementTree tree, IElementContentVisitor visitor, IPath path) {
	this.tree = tree;
	try {
		if (path.isRoot()) {
			//special visit for root element to use special treeData
			visitor.visitElement(tree, this, tree.getTreeData());
			DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(path);
			AbstractDataTreeNode[] children = node.getChildren();
			for (int i = children.length; --i >= 0;) {
				doIteration((DataTreeNode)children[i], visitor);
			}
		} else {
			push(path, path.segmentCount()-1);
			DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(path);
			doIteration(node, visitor);
		}
	} finally {
		//make sure someone caching an iterator doesn't accidently cache a whole tree
		this.tree = null;
	}
}/**
 * Iterates through the given element tree and visits each element in the
 * subtree rooted at the given path.  The visitor is passed each element's
 * path and data.
 */
public void iterateWithPath(ElementTree tree, IElementPathContentVisitor visitor, IPath path) {
	this.tree = tree;
	if (path.isRoot()) {
		//special visit for root element to use special treeData
		visitor.visitElement(tree, path, tree.getTreeData());
		DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(path);
		AbstractDataTreeNode[] children = node.getChildren();
		for (int i = children.length; --i >= 0;) {
			doIterationWithPath((DataTreeNode)children[i], visitor, path.append(children[i].getName()));
		}
	} else {
		DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(path);
		doIterationWithPath(node, visitor, path);
	}
}
}