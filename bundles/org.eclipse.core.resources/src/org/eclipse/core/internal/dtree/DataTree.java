/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.dtree;

import org.eclipse.core.runtime.IPath;

/**
 * A <code>DataTree</code> represents the complete information of a source tree.
 * The tree contains all information within its branches, and has no relation to any
 * other source trees (no parent and no children).
 */
public class DataTree extends AbstractDataTree {

	/**
	 * the root node of the tree
	 */
	private DataTreeNode rootNode;

	/**
	 * Creates a new empty tree
	 */
	public DataTree() {
		this.empty();
	}

	/**
	 * Returns a copy of the node subtree rooted at the given key.
	 *
	 * @param key
	 *	Key of subtree to copy
	 */
	@Override
	public AbstractDataTreeNode copyCompleteSubtree(IPath key) {
		DataTreeNode node = findNodeAt(key);
		if (node == null) {
			handleNotFound(key);
		}
		return copyHierarchy(node);
	}

	/**
	 * Returns a deep copy of a node and all its children.
	 *
	 * @param node
	 *	Node to be copied.
	 */
	DataTreeNode copyHierarchy(DataTreeNode node) {
		DataTreeNode newNode;
		int size = node.size();
		if (size == 0) {
			newNode = new DataTreeNode(node.getName(), node.getData());
		} else {
			AbstractDataTreeNode[] children = node.getChildren();
			DataTreeNode[] newChildren = new DataTreeNode[size];
			for (int i = size; --i >= 0;) {
				newChildren[i] = this.copyHierarchy((DataTreeNode) children[i]);
			}
			newNode = new DataTreeNode(node.getName(), node.getData(), newChildren);
		}

		return newNode;
	}

	/**
	 * Creates a new child in the tree.
	 * @see AbstractDataTree#createChild(IPath, String)
	 */
	@Override
	public void createChild(IPath parentKey, String localName) {
		createChild(parentKey, localName, null);
	}

	/**
	 * Creates a new child in the tree.
	 * @see AbstractDataTree#createChild(IPath, String, Object)
	 */
	@Override
	public void createChild(IPath parentKey, String localName, Object data) {
		DataTreeNode node = findNodeAt(parentKey);
		if (node == null)
			handleNotFound(parentKey);
		if (this.isImmutable())
			handleImmutableTree();
		/* If node already exists, replace */
		if (node.includesChild(localName)) {
			node.replaceChild(localName, new DataTreeNode(localName, data));
		} else {
			this.replaceNode(parentKey, node.copyWithNewChild(localName, new DataTreeNode(localName, data)));
		}
	}

	/**
	 * Creates and returns an instance of the receiver.  This is an
	 * implementation of the factory method creational pattern for allowing
	 * abstract methods to create instances
	 */
	@Override
	protected AbstractDataTree createInstance() {
		return new DataTree();
	}

	/**
	 * Creates or replaces a subtree in the tree.  The parent node must exist.
	 *
	 * @param key
	 *	Key of parent node whose subtree we want to create/replace.
	 * @param subtree
	 *	New node to insert into tree.
	 */
	@Override
	public void createSubtree(IPath key, AbstractDataTreeNode subtree) {

		// Copy it since destructive mods are allowed on the original
		// and shouldn't affect this tree.
		DataTreeNode newNode = copyHierarchy((DataTreeNode) subtree);

		if (this.isImmutable()) {
			handleImmutableTree();
		}

		if (key.isRoot()) {
			setRootNode(newNode);
		} else {
			String localName = key.lastSegment();
			newNode.setName(localName); // Another mod, but it's OK we've already copied

			IPath parentKey = key.removeLastSegments(1);

			DataTreeNode node = findNodeAt(parentKey);
			if (node == null) {
				handleNotFound(parentKey);
			}

			/* If node already exists, replace */
			if (node.includesChild(localName)) {
				node.replaceChild(localName, newNode);
			}

			this.replaceNode(parentKey, node.copyWithNewChild(localName, newNode));
		}
	}

	/**
	 * Deletes a child of the tree.
	 * @see AbstractDataTree#deleteChild(IPath, String)
	 */
	@Override
	public void deleteChild(IPath parentKey, String localName) {
		if (this.isImmutable())
			handleImmutableTree();
		DataTreeNode node = findNodeAt(parentKey);
		if (node == null || (!node.includesChild(localName))) {
			handleNotFound(node == null ? parentKey : parentKey.append(localName));
		} else {
			this.replaceNode(parentKey, node.copyWithoutChild(localName));
		}
	}

	/**
	 * Initializes the receiver.
	 * @see AbstractDataTree#empty()
	 */
	@Override
	public void empty() {
		this.setRootNode(new DataTreeNode(null, null));
	}

	/**
	 * Returns the specified node if it is present, otherwise returns null.
	 *
	 * @param key
	 *	Key of node to return
	 */
	public DataTreeNode findNodeAt(IPath key) {
		AbstractDataTreeNode node = this.getRootNode();
		int keyLength = key.segmentCount();
		for (int i = 0; i < keyLength; i++) {
			try {
				node = node.childAt(key.segment(i));
			} catch (ObjectNotFoundException notFound) {
				return null;
			}
		}
		return (DataTreeNode) node;
	}

	/**
	 * Returns the data at the specified node.
	 *
	 * @param key
	 *	Node whose data to return.
	 */
	@Override
	public Object getData(IPath key) {
		DataTreeNode node = findNodeAt(key);
		if (node == null) {
			handleNotFound(key);
			return null;
		}
		return node.getData();
	}

	/**
	 * Returns the names of the children of a node.
	 * @see AbstractDataTree#getNamesOfChildren(IPath)
	 */
	@Override
	public String[] getNamesOfChildren(IPath parentKey) {
		DataTreeNode parentNode;
		parentNode = findNodeAt(parentKey);
		if (parentNode == null) {
			handleNotFound(parentKey);
			return null;
		}
		return parentNode.namesOfChildren();
	}

	/**
	 * Returns the root node of the tree
	 */
	@Override
	AbstractDataTreeNode getRootNode() {
		return rootNode;
	}

	/**
	 * Returns true if the receiver includes a node with
	 * the given key, false otherwise.
	 */
	@Override
	public boolean includes(IPath key) {
		return (findNodeAt(key) != null);
	}

	/**
	 * Returns an object containing:
	 * 	- a flag indicating whether the specified node was found
	 *  - the data for the node, if it was found
	 * @param key
	 *	key of node for which we want to retrieve data.
	 */
	@Override
	public DataTreeLookup lookup(IPath key) {
		DataTreeNode node = this.findNodeAt(key);
		if (node == null)
			return DataTreeLookup.newLookup(key, false, null);
		return DataTreeLookup.newLookup(key, true, node.getData());
	}

	/**
	 * Replaces the node at the specified key with the given node
	 */
	protected void replaceNode(IPath key, DataTreeNode node) {
		DataTreeNode found;
		if (key.isRoot()) {
			this.setRootNode(node);
		} else {
			found = this.findNodeAt(key.removeLastSegments(1));
			found.replaceChild(key.lastSegment(), node);
		}
	}

	/**
	 * Sets the data at the specified node.
	 * @see AbstractDataTree#setData(IPath, Object)
	 */
	@Override
	public void setData(IPath key, Object data) {
		DataTreeNode node = this.findNodeAt(key);
		if (this.isImmutable())
			handleImmutableTree();
		if (node == null) {
			handleNotFound(key);
		} else {
			node.setData(data);
		}
	}

	/**
	 * Sets the root node of the tree
	 * @see AbstractDataTree#setRootNode(AbstractDataTreeNode)
	 */
	void setRootNode(DataTreeNode aNode) {
		rootNode = aNode;
	}
}
