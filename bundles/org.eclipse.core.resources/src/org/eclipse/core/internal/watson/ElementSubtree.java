/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.core.internal.watson;

import org.eclipse.core.internal.dtree.AbstractDataTreeNode;
import org.eclipse.core.internal.dtree.DataTreeNode;

/**
 * An <code>ElementSubtree</code> is a simple datastructure representing the 
 * contents of an element tree.  It can be used for rapidly creating ElementTree 
 * objects, but cannot be treated as an ElementTree itself.
 * @see ElementTree#ElementTree(ElementSubtree)
 * @see ElementTree#getElementSubtree()
 */
class ElementSubtree {
	protected String elementName;
	protected Object elementData;
	protected ElementSubtree[] children;

	static final ElementSubtree[] EMPTY_ARRAY = new ElementSubtree[0];

	/**
	 * Creates an <code>ElementChildSubtree</code> with the given element name and element,
	 * and child elements (organized by type).
	 * Passing either null or an empty array for childTypes indicates no children.
	 */
	public ElementSubtree(String elementName, Object elementData, ElementSubtree[] children) {
		if (children == null || children.length == 0) {
			children = EMPTY_ARRAY;
		}
		this.elementName = elementName;
		this.elementData = elementData;
		this.children = children;
	}

	/**
	 * Creates an <code>ElementSubtree</code> from a (complete) element node.
	 */
	ElementSubtree(DataTreeNode childNode) {
		AbstractDataTreeNode[] childNodes = childNode.getChildren();
		if (childNodes.length == 0) {
			children = EMPTY_ARRAY;
		} else {
			ElementSubtree[] types = new ElementSubtree[childNodes.length];
			for (int i = childNodes.length; --i >= 0;) {
				types[i] = new ElementSubtree((DataTreeNode) childNodes[i]);
			}
			children = types;
		}
		elementName = childNode.getName();
		elementData = childNode.getData();
	}

	/**
	 * Returns the child subtrees.
	 * Returns null if there are no children.
	 */
	public ElementSubtree[] getChildren() {
		return children;
	}

	/**
	 * Returns the element data.
	 */
	public Object getElementData() {
		return elementData;
	}

	/**
	 * Returns the element name.
	 */
	public String getElementName() {
		return elementName;
	}

	/**
	 * For debugging purposes
	 */
	public String toString() {
		return "ElementSubtree(" + elementName + ", " + elementData + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}