/**********************************************************************
Copyright (c) 2000, 2002 IBM Corp.  All rights reserved.
This file is made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html
**********************************************************************/
package org.eclipse.ui.externaltools.internal.ant.view.elements;


public abstract class AntNode {
	
	private AntNode parent= null;
	private String name= null;
	
	/**
	 * Creates a new node with no parent
	 */
	private AntNode() {
	}
	
	/**
	 * Creates a new node with the given name
	 * 
	 * @param name the new node's name
	 */
	public AntNode(String name) {
		this(null, name);
	}
	
	/**
	 * Creates a new node with the given parent and the given name
	 * 
	 * @param parent the new node's parent node
	 * @param name the new node's name
	 */
	public AntNode(AntNode parent, String name) {
		this.parent= parent;
		this.name= name;
	}
	
	/**
	 * Returns this node's parent or <code>null</code> if none.
	 * 
	 * @return AntNode this node's parent node
	 */
	public AntNode getParent() {
		return parent;
	}
	
	/**
	 * Sets this node's parent node to the given node
	 * 
	 * @param parent the parent node
	 */
	public void setParent(AntNode parent) {
		this.parent= parent;
	}
	
	/**
	 * Returns this node's name or <code>null</code> if none. Subclasses which
	 * represent an ant build element that has a required name must override
	 * this method to never return <code>null</code>
	 * 
	 * @return String this node's name or <code>null</code> if the name
	 * attribute is optional for this node.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets this node's name to the given name
	 * 
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name= name;
	}
	
	public String toString() {
		if (getName() != null) {
			return getName();
		}
		return super.toString();
	}

}
