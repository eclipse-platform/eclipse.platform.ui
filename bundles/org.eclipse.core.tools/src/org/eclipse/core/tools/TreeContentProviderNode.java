/**********************************************************************
 * Copyright (c) 2002, 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.tools;

import java.util.*;

/**
 * Represents a node (possibly containing children) in a tree content 
 * provider model. Every node has a name and optionally a value.
 */
public class TreeContentProviderNode implements Comparable {

	/**
	 * A list containing this node's children. 
	 */
	private List children;

	/**
	 * This node's name.
	 */
	private String name;

	/** 
	 * This node's value (may be null).
	 */
	private Object value;

	/**
	 * This node's parent node.
	 */
	private TreeContentProviderNode parent;

	/**
	 * Constructs a TreeContentProviderNode with the given name and value.
	 * 
	 * @param name this node's name
	 * @param value this node's value (may be null)
	 */
	public TreeContentProviderNode(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	/**
	 * Constructs a TreeContentProviderNode with the given name.
	 * 
	 * @param name this node's name. 
	 */
	public TreeContentProviderNode(String name) {
		this(name, null);
	}

	/**
	 * Sets this node's parent.
	 * 
	 * @param parent this node's new parent
	 */
	private void setParent(TreeContentProviderNode parent) {
		this.parent = parent;
	}

	/**
	 * Adds a new child. If the child is a TreeContentProviderNode, sets its parent
	 * to this object.
	 * 
	 * @param child a new child to be added.
	 */
	public void addChild(Object child) {
		// lazilly instantiates the children's list
		if (this.children == null) {
			this.children = new ArrayList();
		}
		this.children.add(child);
		if (child instanceof TreeContentProviderNode) {
			TreeContentProviderNode childNode = (TreeContentProviderNode) child;
			childNode.setParent(this);
		}
	}

	/**
	 * Returns an array containing all children this node has. If this node 
	 * has no children, returns an empty array.
	 * 
	 * @return an array containing this node's children.
	 */
	public Object[] getChildren() {
		return children == null ? new Object[0] : children.toArray();
	}

	/**
	 * Returns a boolean indicating if this node has any children.
	 * 
	 * @return true, if this node has children, false otherwise
	 */
	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	/**
	 * Returns a string representation of the object.
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return name + (value == null ? "" : (" = " + value.toString())); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Returns this node's parent node.
	 * 
	 * @return this node's parent node or null, if this node is a root
	 */
	public TreeContentProviderNode getParent() {
		return parent;
	}

	/**
	 * Returns this node's value (may be null).
	 * 
	 * @return this node's value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * Returns a boolean indicating if this node is root or not.
	 * 
	 * @return true if this node is root, false otherwise
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * Removes all child nodes (if any) from this node. This operation affects
	 * only this node. No changes are made to the child nodes. 
	 */
	public void removeAllChildren() {
		if (children == null)
			return;

		children.clear();
	}

	/**
	 * Sorts this node's children list in ascending order. The children are 
	 * ordered by name. Any changes in the children list will potentially 
	 * invalidate the ordering. All children must be instances of 
	 * <code>TreeContentProviderNode</code>. 
	 */
	public void sort() {
		if (children == null)
			return;
		Collections.sort(children);
	}

	/**
	 * Compares this node with another node. 
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object other) {
		TreeContentProviderNode otherNode = (TreeContentProviderNode) other;
		return this.name.compareTo(otherNode.name);
	}

	/**
	 * Accepts the given visitor. The visitor's <code>visit</code> method is called
	 * with this node. If the visitor returns <code>true</code>, this method visits
	 * this node's child nodes.
	 *
	 * @param visitor the visitor
	 * @see ITreeNodeVisitor#visit
	 */
	public void accept(ITreeNodeVisitor visitor) {
		if (!visitor.visit(this))
			return;
		if (children == null)
			return;
		for (Iterator childrenIter = children.iterator(); childrenIter.hasNext();) {
			Object child = childrenIter.next();
			// child nodes don't need to be TreeContentProviderNodes
			if (child instanceof TreeContentProviderNode)
				((TreeContentProviderNode) child).accept(visitor);
		}
	}

	/**
	 * Returns this node's tree root node. If this node is a root node, returns itself.
	 * 
	 * @return this node's tree root node
	 */
	public TreeContentProviderNode getRoot() {
		return this.getParent() == null ? this : this.getParent().getRoot();
	}

}