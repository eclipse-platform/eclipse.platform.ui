/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
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

	private Collection pages = new ArrayList();

	private Collection pageIds;

	private ImageDescriptor imageDescriptor;
	
	private ImageDescriptor largeImageDescriptor;

	private Image image;
	
	private Image largeImage;

	private Object lastSelection = null;

	private boolean isDefault = false;

	/**
	 * Create a new instance of the receiver.
	 * @param uniqueID The unique id. Must be unique and non null.
	 * @param displayableName The human readable name
	 * @param ids
	 * @param icon The ImageDescriptor for the icon for the
	 * receiver. May be <code>null</code>.
	 * @param largeIcon The ImageDescriptor for the largeIcon for the
	 * receiver. May be <code>null</code>.
	 * @param defaultValue <code>true</code> if this is the default group
	 */
	public WorkbenchPreferenceGroup(String uniqueID, String displayableName, Collection ids,
			ImageDescriptor icon, ImageDescriptor largeIcon,boolean defaultValue) {
		id = uniqueID;
		name = displayableName;
		imageDescriptor = icon;
		largeImageDescriptor = largeIcon;
		pageIds = ids;
		isDefault = defaultValue;
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

		if (imageDescriptor == null)
			return null;

		if (image == null)
			image = imageDescriptor.createImage();
		return image;
	}
	
	/**
	 * Return the image for the receiver. Return a default
	 * image if there isn't one.
	 * @return Image
	 */
	public Image getLargeImage() {

		if (largeImageDescriptor == null)
			return null;

		if (largeImage == null)
			largeImage = largeImageDescriptor.createImage();
		return largeImage;
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
	public void disposeResources() {
		if(image != null)
			image.dispose();
		if(largeImage != null)
			largeImage.dispose();
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
	 * Get the last selected object in this group.
	 * @return Object
	 */
	public Object getLastSelection() {
		return lastSelection;
	}

	/**
	 * Set the last selected object in this group.
	 * @param lastSelection WorkbenchPreferenceGroup
	 * or WorkbenchPreferenceNode.
	 */
	public void setLastSelection(Object lastSelection) {
		this.lastSelection = lastSelection;
	}

	/**
	 * Return whether or not this is the default group.
	 * @return boolean
	 */
	public boolean isDefault() {
		return this.isDefault;
	}
}
