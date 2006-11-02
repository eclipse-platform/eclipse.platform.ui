/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A lightweight tree node used to hold user assistance document nodes. Used
 * to provide document content in content providers for dynamic or generated
 * content.
 * 
 * @since 3.3
 */
public class Node {
	
	private String name;
	private String value;
	private Map attributes;
	private Node parent;
	private List children;

	/**
	 * Appends the given node to this node's children.
	 * 
	 * @param newChild the new child node to append
	 */
	public void appendChild(Node newChild) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(newChild);
		newChild.parent = this;
	}

	/**
	 * Returns the value of the attribute with a given name.
	 * 
	 * @param name the attribute name
	 * @return value of the attribute with a given name or <code>null</code> if not set.
	 */
	public String getAttribute(String name) {
		if (attributes != null) {
			return (String)attributes.get(name);
		}
		return null;
	}

	/**
	 * Returns the names (<code>String</code>s) of all the attributes defined
	 * for this node.
	 * 
	 * @return the set of all the attribute names
	 */
	public Set getAttributes() {
		if (attributes == null) {
			attributes = new HashMap();
		}
		return attributes.keySet();
	}

	/**
	 * Returns this node's child nodes.
	 * 
	 * @return the child nodes, or an empty array if there are no children
	 */
	public Node[] getChildren() {
		if (children == null) {
			return new Node[0];
		}
		return (Node[])children.toArray(new Node[children.size()]);
	}

	/**
	 * Returns the name of the node, or <code>null</code> if it has no name.
	 * 
	 * @return the node's name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the node's parent.
	 * 
	 * @return the parent node, or <code>null</code> if this is the root node
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Returns the value of the node.
	 * 
	 * @return value of the node or <code>null</code> if not set.
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Inserts the given <code>newChild</code> before the existing
	 * <code>refChild</code> child node. Does nothing if <code>refChild</code>
	 * is not a child of this node.
	 * 
	 * @param newChild the new child node to insert
	 * @param refChild the existing child node before which to insert
	 */
	public void insertBefore(Node newChild, Node refChild) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(children.indexOf(refChild), newChild);
		newChild.parent = this;
	}

	/**
	 * Removes the attribute with the given name from this node. Does nothing
	 * if the node has no such attribute.
	 * 
	 * @param name the name (key) of the attribute to remove
	 */
	public void removeAttribute(String name) {
		if (attributes != null) {
			attributes.remove(name);
		}
	}
	
	/**
	 * Removes the specified node from this node's children. Does nothing
	 * if the node is not a child of this node.
	 * 
	 * @param oldChild the child node to remove
	 */
	public void removeChild(Node oldChild) {
		if (children != null) {
			children.remove(oldChild);
		}
	}
	
	/**
	 * Sets the value of the named attribute.
	 * 
	 * @param name the attribute name
	 * @param value the attribute value
	 */
	public void setAttribute(String name, String value) {
		if (attributes == null) {
			attributes = new HashMap();
		}
		attributes.put(name, value);
	}
	
	/**
	 * Sets the node's name.
	 * 
	 * @param name the name of the node
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Sets the value of the node.
	 * 
	 * @param value the value of this node
	 */
	public void setValue(String value) {
		this.value = value;
	}
}