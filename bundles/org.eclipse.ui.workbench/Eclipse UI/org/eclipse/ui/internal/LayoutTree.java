package org.eclipse.ui.internal;

/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *     IBM Corporation - initial API and implementation
 *     Cagatay Kavukcuoglu <cagatayk@acm.org>
 *       - Fix for bug 10025 - Resizing views should not use height ratios
**********************************************************************/

import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.*;
import java.util.ArrayList;
/**
 * Implementation of a tree where the node is allways a sash
 * and it allways has two chidren. If a children is removed
 * the sash, ie the node, is removed as well and its other children
 * placed on its parent.
 */
public class LayoutTree {
	/* The parent of this tree or null if it is the root */
	LayoutTreeNode parent;
	/* Any LayoutPart if this is a leaf or a LayoutSashPart if it is a node */
	LayoutPart part;

/**
 * Initialize this tree with its part.
 */
public LayoutTree(LayoutPart part) {
	this.part = part;
}
/**
 * Add the relation ship between the children in the list
 * and returns the left children.
 */
public LayoutPart computeRelation(ArrayList relations) {
	return part;
}
/**
 * Dispose all Sashs in this tree
 */
public void disposeSashes() {
}
/**
 * Find a LayoutPart in the tree and return its sub-tree. Returns
 * null if the child is not found.
 */
public LayoutTree find(LayoutPart child) {
	if(part != child) 
		return null;
	return this;
}
/**
 * Find the Left,Right,Top and Botton 
 * sashes around this tree and set them
 * in <code>sashes</code>
 */
public void findSashes(PartPane.Sashes sashes) {
	if(getParent() == null)
		return;
	getParent().findSashes(this,sashes);
}
/**
 * Find the part that is in the bottom rigth possition.
 */
public LayoutPart findBottomRight() {
	return part;
}
/**
 * Find a sash in the tree and return its sub-tree. Returns
 * null if the sash is not found.
 */
public LayoutTreeNode findSash(LayoutPartSash sash) {
	return null;
}
/**
 * Return the bounds of this tree which is the rectangle that
 * contains all Controls in this tree.
 */
public Rectangle getBounds() {
	return part.getBounds();
}

// getMinimumWidth() added by cagatayk@acm.org 
public int getMinimumWidth() {
	return part.getMinimumWidth();
}

// getMinimumHeight() added by cagatayk@acm.org 
public int getMinimumHeight() {
	return part.getMinimumHeight();
}
/**
 * Returns the parent of this tree or null if it is the root.
 */
public LayoutTreeNode getParent() {
	return parent;
}
/**
 * Inserts a new child on the tree. The child will be placed beside 
 * the <code>relative</code> child. Returns the new root of the tree.
 */
public LayoutTree insert(LayoutPart child,boolean left,LayoutPartSash sash,LayoutPart relative) {
	LayoutTree relativeChild = find(relative);
	LayoutTreeNode node = new LayoutTreeNode(sash);
	if(relativeChild == null) {
		//Did not find the relative part. Insert beside the root.
		node.setChild(left,child);	
		node.setChild(!left,this);
		return node;
	} else {
		LayoutTreeNode oldParent = relativeChild.getParent();
		node.setChild(left,child);	
		node.setChild(!left,relativeChild);
		if(oldParent == null) {
			//It was the root. Return a new root.
			return node;
		}
		oldParent.replaceChild(relativeChild,node);
		return this;
	}
}
/**
 * Returns true if this tree has visible parts otherwise returns false.
 */
public boolean isVisible() {
	return !(part instanceof PartPlaceholder);
}
/**
 * Recompute the ratios in this tree.
 */
public void recomputeRatio() {
}
/**
 * Find a child in the tree and remove it and its parent.
 * The other child of its parent is placed on the parent's parent.
 * Returns the new root of the tree.
 */
public LayoutTree remove(LayoutPart child) {
	LayoutTree tree = find(child);
	if(tree == null)
		return this;
	LayoutTreeNode oldParent = tree.getParent();
	if(oldParent == null) {
		//It was the root and the only child of this tree
		return null;
	}
	if(oldParent.getParent() == null)
		return oldParent.remove(tree);
		
	oldParent.remove(tree);
	return this;
}
/**
 * Resize the parts on this tree to fit in <code>bounds</code>.
 */
public void setBounds(Rectangle bounds) {
	part.setBounds(bounds);
}
/**
 * Set the parent of this tree.
 */
void setParent(LayoutTreeNode parent) {
	this.parent = parent;
}
/**
 * Set the part of this leaf
 */
void setPart(LayoutPart part) {
	this.part = part;
}
/**
 * Returns a string representation of this object.
 */
public String toString() {
	return "(" + part.toString() + ")";//$NON-NLS-2$//$NON-NLS-1$
}
/**
 * Create the sashes if the children are visible
 * and dispose it if they are not.
 */
public void updateSashes(Composite parent) {
}
}
