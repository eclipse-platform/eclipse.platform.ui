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
import java.util.Iterator;

import org.eclipse.swt.graphics.Image;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.misc.StringMatcher;

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
	private boolean highlight = false;
	private Object lastSelection = null;

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

	/**
	 * Add all of the children that match text to the
	 * highlight list.
	 * @param text
	 */
	public void highlightHits(String text) {
		Iterator pagesIterator = pages.iterator();
		StringMatcher matcher = new StringMatcher('*' + text + '*', true, false);
		
		
		while(pagesIterator.hasNext()){
			WorkbenchPreferenceNode node = (WorkbenchPreferenceNode) pagesIterator.next();
			if (text.length() ==0) 
				clearSearchResults(node); 
			else
				matchNode(matcher, node);
		}
		
		Iterator groupsIterator = childGroups.iterator();
		
		while(groupsIterator.hasNext()){
			WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) groupsIterator.next();
			group.highlight = text.length() == 0 ? false : matcher.match(group.getName()); 
			group.highlightHits(text);
		}
	}

	void clearSearchResults(WorkbenchPreferenceNode node) {
		node.setHighlighted(false);
		IPreferenceNode[] children = node.getSubNodes();
		for (int i = 0; i < children.length; i++)
			clearSearchResults((WorkbenchPreferenceNode) children[i]);
	}

	/**
	 * Match the node to the pattern and highlight it if there is
	 * a match.
	 * @param matcher
	 * @param node
	 */
	private void matchNode(StringMatcher matcher, WorkbenchPreferenceNode node) {
		node.setHighlighted(matcher.match(node.getLabelText()));
		IPreferenceNode[] children = node.getSubNodes();
		for (int i = 0; i < children.length; i++)
			matchNode(matcher, (WorkbenchPreferenceNode) children[i]);
	}

	/**
	 * Return whether or not the receiver is highlighted.
	 * @return Returns the highlight.
	 */
	public boolean isHighlighted() {
		return highlight;
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
	 * Find the parent of element in the receiver.
	 * If there isn't one return <code>null</null>.
	 * @param element
	 * @return Object or <code>null</null>.
	 */
	public Object findParent(Object element) {
		return findParent(this,element);
	}

	/**
	 * Find the parent of this element starting at this group.
	 * @param group
	 * @param element
	 */
	private Object findParent(WorkbenchPreferenceGroup group, Object element) {
		Iterator pagesIterator = group.pages.iterator();
		while(pagesIterator.hasNext()){
			WorkbenchPreferenceNode next = (WorkbenchPreferenceNode) pagesIterator.next();
			if(next.equals(element))
				return group;
			Object parent = findParent(next,element);
			if(parent != null)
				return parent;
		}
		
		Iterator subGroupsIterator = group.childGroups.iterator();
		while(subGroupsIterator.hasNext()){
			WorkbenchPreferenceGroup next = (WorkbenchPreferenceGroup) subGroupsIterator.next();
			if(next.equals(element))
				return group;
			Object parent = findParent(next,element);
			if(parent != null)
				return parent;
		}
		
		return null;
		
	}

	/**
	 * Find the parent of this element starting at this node.
	 * @param node
	 * @param element
	 * @return Object or <code>null</code>.
	 */
	private Object findParent(IPreferenceNode node, Object element) {
		IPreferenceNode[] subs = node.getSubNodes();
		for (int i = 0; i < subs.length; i++) {
			IPreferenceNode subNode = subs[i];
			if(subNode.equals(element))
				return node;
			Object parent = findParent(subNode,element);
			if(parent != null)
				return parent;			
		}
		return null;
	}

	/**
	 * Add any page ids that match the filteredIds
	 * to the list of highlights.
	 * @param filteredIds
	 */
	public void highlightIds(String[] filteredIds) {
		for (int i = 0; i < filteredIds.length; i++) {
			checkId(filteredIds[i]);
		}
		
	}

	/**
	 * Check the passed id to see if it matches
	 * any of the receivers pages.
	 * @param id
	 */
	private void checkId(String id) {
		Iterator pagesIterator = pages.iterator();
		while(pagesIterator.hasNext()){
			WorkbenchPreferenceNode next = (WorkbenchPreferenceNode) pagesIterator.next();
			checkHighlightNode(id, next);
		}
		
		Iterator childIterator = childGroups.iterator();
		while(childIterator.hasNext()){
			WorkbenchPreferenceGroup group = (WorkbenchPreferenceGroup) childIterator.next();
			group.checkId(id);
		}
	}

	/**
	 * Check if the node matches id and needs to be highlighted.
	 * @param id
	 * @param node
	 * @return <code>true</code> if a match is found
	 */
	private boolean checkHighlightNode(String id, IPreferenceNode node) {
		if(node.getId().equals(id)){
			((WorkbenchPreferenceNode) node).setHighlighted(true);
			return true;
		}
		IPreferenceNode[] subNodes = node.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			if(checkHighlightNode(id,subNodes[i]))
				return true;			
		}
		return false;
	}
}
