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
package org.eclipse.help.internal;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.help.Node;

/*
 * Adapts a Node to expose a new interface, e.g. "toc" Nodes are exposed as
 * ITocs via the Toc adapter class.
 */
public abstract class NodeAdapter extends Node {

	private static final Class[] PARAMETER_TYPES = new Class[] { Node.class };
	private static final Object[] PARAMETER = new Object[1];
	
	// the node to adapt
	protected Node node;
	
	/*
	 * Creates an adapter for a new node.
	 */
	protected NodeAdapter() {
		node = new Node();
	}

	/*
	 * Creates an adapter for the given node.
	 */
	protected NodeAdapter(Node node) {
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#appendChild(org.eclipse.help.Node)
	 */
	public void appendChild(Node newChild) {
		node.appendChild(newChild);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getAttribute(java.lang.String)
	 */
	public String getAttribute(String name) {
		return node.getAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getAttributes()
	 */
	public Set getAttributes() {
		return node.getAttributes();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getChildren()
	 */
	public Node[] getChildren() {
		return node.getChildren();
	}
	
	/*
	 * Convenience method for getting all children with a given name, returned
	 * as an array of typed adapters. e.g. getChildren("topic", Topic.class)
	 * returns all children with name "topic" as a Topic[] array. The returned
	 * Object must be casted to the requested type.
	 * 
	 * It is assumed that the adapter has a one-arg constructor that takes in
	 * a node to be adapted.
	 */
	public Object getChildren(String name, Class clazz) {
		Node[] children = getChildren();
		if (children.length > 0) {
			List list = new ArrayList();
			for (int i=0;i<children.length;++i) {
				if (name.equals(children[i].getName())) {
					try {
						PARAMETER[0] = children[i];
						NodeAdapter typedNode = (NodeAdapter)clazz.getConstructor(PARAMETER_TYPES).newInstance(PARAMETER);
						list.add(typedNode);
					}
					catch (Throwable t) {
						// invalid; skip
					}
				}
			}
			// make the typed array
			Object array = Array.newInstance(clazz, list.size());
			for (int i=0;i<list.size();++i) {
				Array.set(array, i, list.get(i));
			}
			return array;
		}
		else {
			return Array.newInstance(clazz, 0);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getName()
	 */
	public String getName() {
		return node.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getParent()
	 */
	public Node getParent() {
		return node.getParent();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#getValue()
	 */
	public String getValue() {
		return node.getValue();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#insertBefore(org.eclipse.help.Node, org.eclipse.help.Node)
	 */
	public void insertBefore(Node newChild, Node refChild) {
		node.insertBefore(newChild, refChild);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String name) {
		node.removeAttribute(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#removeChild(org.eclipse.help.Node)
	 */
	public void removeChild(Node oldChild) {
		node.removeChild(oldChild);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String name, String value) {
		node.setAttribute(name, value);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#setName(java.lang.String)
	 */
	public void setName(String name) {
		node.setName(name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.Node#setValue(java.lang.String)
	 */
	public void setValue(String value) {
		node.setValue(value);
	}
}
