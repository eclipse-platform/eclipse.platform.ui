/*
 * Copyright (c) 2002, Roscoe Rush. All Rights Reserved.
 *
 * The contents of this file are subject to the Common Public License
 * Version 0.5 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.eclipse.org/
 *
 */
package org.eclipse.ui.externaltools.internal.ant.antview.tree;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.graphics.Image;

import org.eclipse.ui.externaltools.internal.ant.antview.core.IAntViewConstants;
import org.eclipse.ui.externaltools.internal.ant.antview.core.ResourceMgr;

public class TreeNode implements IAdaptable, IAntViewConstants {
   
	private TreeNode parent = null;
	private ArrayList children = null;
	private Hashtable property = null;
	private boolean selected = false;
    private String text;
    
	/**
	 * Method TreeObject.
	 * @param text
	 */
	public TreeNode(String text) {
		property = new Hashtable();
		children = new ArrayList();
		this.text = text;
	}
	/**
	 * Method setParent.
	 * @param parent
	 */
	public void setParent(TreeNode parent) {
		this.parent = parent;
	}
	/**
	 * Method getParent.
	 * @return TreeObject
	 */
	public TreeNode getParent() {
		return parent;
	}
	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getText();
	}
	/**
	 * Method getAdapter.
	 * @param key
	 * @return Object
	 */
	public Object getAdapter(Class key) {
		return null;
	}
	/**
	 * Method addChild.
	 * @param child
	 */
	public void addChild(TreeNode child) {
		children.add(child);
		child.setParent(this);
	}
	/**
	 * Method removeChild.
	 * @param child
	 */
	public void removeChild(TreeNode child) {
		children.remove(child);
		child.setParent(null);
	}
	/**
	 * Method getChildren.
	 * @return TreeObject[]
	 */
	public TreeNode[] getChildren() {
		return (TreeNode[]) children.toArray(new TreeNode[children.size()]);
	}
	/**
	 * Method hasChildren.
	 * @return boolean
	 */
	public boolean hasChildren() {
		return children.size() > 0;
	}
	/**
	 * Sets the name.
	 * @param name The name to set
	 */
	public void setText(String text) {
	    this.text = text; 
	}
	/**
	 * Returns the name.
	 * @return String
	 */
	public String getText() {
		return text; 
	}
	/**
	 * Method decoreateText.
	 * @param text
	 * @return String
	 */
	public String decorateText(String text) {
		return text;
	}
	/**
	 * Returns the selected.
	 * @return boolean
	 */
	public boolean isSelected() {
		return selected;
	}
	/**
	 * Sets the selected.
	 * @param selected The selected to set
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	/**
	 * Method setSelected.
	 */
	public void setSelected() {
	    if (selected) { 
	    	selected = false;
	    } else {
	    	selected = true;
	    }
	}
	/**
	 * Method getProperty.
	 * @param key
	 * @return Object
	 */
	public Object getProperty(String key) {
		return property.get(key);
	}
	/**
	 * Sets the property.
	 * @param property The property to set
	 */
	public void setProperty(String key, Object data) {
		property.put(key,data);
	}
	/**
	 * Method removeProperty.
	 * @param key
	 */
	public void removeProperty(String key) { 
		property.remove(key);
	}
	/**
	 * Method getImage.
	 * @return Image
	 */
	public Image getImage() {		          		   
		return ResourceMgr.getImage(IMAGE_DEFAULT);
	}
	/**
	 * Method decorateImage.
	 * @param image
	 * @return Image
	 */
	public Image decorateImage(Image image) { 
		return image;
	}
	/**
	 * Method getRoot.
	 * @return TreeNode
	 */
	public TreeNode getRoot() { 
		TreeNode treeRoot = this;
		while (null != treeRoot.getParent()) {
			treeRoot = treeRoot.getParent();
		}
		return treeRoot;
	}
}