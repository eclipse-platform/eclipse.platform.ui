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
package org.eclipse.core.internal.dtree;

/**
 * <code>NodeInfo</code> objects are placeholders for information about a node.  
 * They provide a mechanism for comparing arbitrary data trees and for 
 * remembering a snapshot of what a node looked like.
 *
 */
public class NodeInfo {
	
	private int type;
	private Object data;
	private String namesOfChildren[], namesOfDeletedChildren[];
/**
 * Creates a new NodeInfo object
 *
 * @param name name of node
 * @param data node's data
 * @param children array of child names
 * @param deleted array of deleted child names
 */
public NodeInfo (int type, Object data, String[] children, String[] deleted) {
	this.type = type;
	this.data = data;
	this.namesOfChildren = children;
	this.namesOfDeletedChildren = deleted;
}
/**
 * Get node's data
 */
public Object getData() {
	return data;
}
/**
 * Returns an array of names of children of the node
 */
public String[] getNamesOfChildren() {
	return namesOfChildren;
}
/**
 * Returns an array of names of deleted children of the node
 */
public String[] getNamesOfDeletedChildren() {
	return namesOfDeletedChildren;
}
public int getType () {
	return type;
}
/**
 * Returns true if the type of node carries data, false otherwise.
 */
public boolean hasData() {
	return (type == AbstractDataTreeNode.T_COMPLETE_NODE ||
		 	type == AbstractDataTreeNode.T_DELTA_NODE);
}
/**
 * Returns true if the receiver represents a complete node.
 */
public boolean isComplete () {
	return this.getType() == AbstractDataTreeNode.T_COMPLETE_NODE;
}
/**
 * Returns true if the receiver represents a node that has been
 * deleted from the tree, false otherwise.
 */
public boolean isDeleted () {
	return this.getType() == AbstractDataTreeNode.T_DELETED_NODE;
}
/**
 * Returns true if the node carries delta information, false otherwise.
 */
public boolean isDelta () {
	int type = this.getType();
	
	return (type == AbstractDataTreeNode.T_DELTA_NODE ||
		   type == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE);
}
/**
 * Returns whether the node represents an empty delta.
 * The node represents an empty delta if has no data and no children.
 */
public boolean isEmptyDelta() {
	return (
		this.getType() == AbstractDataTreeNode.T_NO_DATA_DELTA_NODE &&
		this.getNamesOfChildren().length == 0 &&
		this.getNamesOfDeletedChildren().length == 0);
}
/**
 * Returns true if the node is present in the tree, whether it
 * be a complete node, delta node, deleted node or virtual node.
 */
public boolean isPresent() {
	return this.getType() != AbstractDataTreeNode.T_MISSING_NODE;
}
/**
 * Returns a node info object describing a missing or deleted node.
 */
static NodeInfo missing() {
	return new NodeInfo(AbstractDataTreeNode.T_MISSING_NODE,
		null, 			//no data
		new String[0],	//no children
		new String[0]);	//no deleted children
}
public void setData (Object o) {
	data = o;
}
public void setNamesOfChildren(String names[]) {
	namesOfChildren = names;
}
public void setNamesOfDeletedChildren(String names[]) {
	namesOfDeletedChildren = names;
}
public void setType (int type) {
	this.type = type;
}
}
