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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.internal.dtree.*;
import org.eclipse.core.internal.utils.Assert;
import org.eclipse.core.internal.utils.Policy;
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
public class ElementTreeIterator {

	/** pre-order traversal means visit the root first, then the children */
	public static final int PRE_ORDER = 0;

	/** post-order means visit the children, and then the root */
	public static final int POST_ORDER = 1;

	/** traversal order */
	private final int order;

	/* the tree being visited */
	private ElementTree tree;
/**
 * Creates a new element tree iterator that using the default traversal order
 * (<code>PRE_ORDER</code>).
 */
public ElementTreeIterator() {
	order = PRE_ORDER;
}
/**
 * Creates a new element tree iterator that uses a particular
 * traversal order (either <code>PRE_ORDER</code> or 
 * <code>POST_ORDER</code>).
 * @param order the traversal order
 */
public ElementTreeIterator(int order) {
	Assert.isTrue (order == PRE_ORDER || order == POST_ORDER, Policy.bind("watson.traversal")); //$NON-NLS-1$
	this.order = order;
}
/**
 * Iterates through the given element tree and visit each element (node)
 * passing in the element's ID and element object.
 */
public void iterate(DataTreeNode node, IElementContentVisitor visitor, IPath path) {
	if (order == PRE_ORDER) {
		visitor.visitElement(tree, path, node.getData());
	}

	AbstractDataTreeNode[] children = node.getChildren();
	for (int i = 0; i < children.length; i++) {
		iterate((DataTreeNode)children[i], visitor, path.append(children[i].getName()));
	}

	if (order == POST_ORDER) {
		visitor.visitElement(tree, path, node.getData());
	}
}
/**
 * Iterates through the given element tree and visit each element (node)
 * passing in the element's ID and element object.
 */
public void iterate(ElementTree tree, IElementContentVisitor visitor) {
	this.tree = tree;
	DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(Path.ROOT);
	iterate(node, visitor, Path.ROOT);
}
/**
 * Iterates through the given element tree and visits each element in the
 * subtree rooted at the given path.  The visitor is passed each element's 
 * path and data.
 */
public void iterate(ElementTree tree, IElementContentVisitor visitor, IPath path) {
	this.tree = tree;
	DataTreeNode node = (DataTreeNode)tree.getDataTree().copyCompleteSubtree(path);
	iterate(node, visitor, path);
}
}
