/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * WorkbenchPreferenceGroup is the representation of a category
 * in the workbench.
 */
public class WorkbenchPreferenceGroup {
	
	private String id;
	private String name;
	private String parentGroupId;
	private Collection childGroups = new ArrayList();
	private Collection pages = new ArrayList();
	private Collection pageIds;
	private ImageDescriptor imageDescriptor;
	private Image image;

	/**
	 * Create a new instance of the receiver.
	 * @param uniqueID The unique id. Must be unique and non null.
	 * @param displayableName The human readable name
	 * @param parentId The id of the parent category.
	 * @param ids
	 * @param icon The ImageDescriptor for the icon for the
	 * receiver. May be <code>null</code>.
	 */
	public WorkbenchPreferenceGroup(String uniqueID, String displayableName, String parentId, Collection ids, ImageDescriptor icon) {
		id = uniqueID;
		name = displayableName;
		parentGroupId = parentId;
		imageDescriptor = icon;
		pageIds = ids;
	}

	/**
	 * Return the id of the parent
	 * @return String
	 */
	public String getParent() {
		return parentGroupId;
	}

	/**
	 * Add the category to the children.
	 * @param category
	 */
	public void addChild(WorkbenchPreferenceGroup category) {
		childGroups.add(category);
		
	}

	/**
	 * Return the id for the receiver.
	 * @return String
	 */
	public String getId() {
		return id;
	}

	/**
	 * Add the node to the list of pages in this category.
	 * @param node
	 */
	public void addNode(WorkbenchPreferenceNode node) {
		pages.add(node);
		
	}
	
	/**
	 * Return the image for the receiver. Return a default
	 * image if there isn't one.
	 * @return Image
	 */
	public Image getImage() {
		
		if(imageDescriptor == null)
			return null;
		
		if(image == null)
			image = imageDescriptor.createImage();
		return image;
	}

	/**
	 * Return the name of the receiver.
	 * @return String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Dispose the resources for the receiver.
	 *
	 */
	public void disposeResources(){
		image.dispose();
		image = null;
	}

	/**
	 * Return the preference nodes in the receiver.
	 * @return IPreferenceNode[]
	 */
	public IPreferenceNode[] getPreferenceNodes() {
		IPreferenceNode[] nodes = new IPreferenceNode[pages.size()];
		pages.toArray(nodes);
		return nodes;
	}

	/**
	 * Return the pageIds for the receiver.
	 * @return Collection
	 */
	public Collection getPageIds() {
		return pageIds;
	}

	/**
	 * Return the children of the receiver.
	 * @return Collection
	 */
	public Collection getChildren() {
		return childGroups;
	}
	
	/**
	 * Return the all of the child groups and
	 * nodes.
	 * @return Collection
	 */
	public Object[] getGroupsAndNodes() {
		Collection allChildren = new ArrayList();
		allChildren.addAll(childGroups);
		allChildren.addAll(pages);
		return allChildren.toArray();
	}

}
