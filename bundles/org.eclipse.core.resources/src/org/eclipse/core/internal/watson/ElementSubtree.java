package org.eclipse.core.internal.watson;

/*
 * Licensed Materials - Property of IBM,
 * WebSphere Studio Workbench
 * (c) Copyright IBM Corp 2000
 */
import org.eclipse.core.internal.dtree.*;

/**
 * An <code>ElementSubtree</code> is a simple datastructure representing the 
 * contents of an element tree.  It can be used for rapidly creating ElementTree 
 * objects, but cannot be treated as an ElementTree itself.
 * @see ElementTree(ElementSubtree)
 * @see ElementTree.getElementSubtree()
 */
class ElementSubtree {
	protected String elementName;
	protected Object elementData;
	protected ElementSubtree[] children;

	static final ElementSubtree[] emptyArray = new ElementSubtree[0];
/**
 * Creates an <code>ElementChildSubtree</code> with the given element name and element,
 * and child elements (organized by type).
 * Passing either null or an empty array for childTypes indicates no children.
 */
public ElementSubtree(String elementName, Object elementData, ElementSubtree[] children) {
	if (children == null || children.length == 0) {
		children = emptyArray;
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
		children = emptyArray;
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
	return "ElementSubtree(" + elementName + ", " + elementData + ")";
}
}
