/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.topics;

import java.util.*;

/**
 * Navigation Element.
 * Common for all objects definable in topics.xml
 */
abstract class NavigationElement implements INavigationNode {
	private final static List emptyList = new ArrayList(0);
	private List children;
	private List parents;
	
	
	/**
	 * Adds another element as child of this element
	 * Modifies parents of a child as well
	 */
	public void addChild(INavigationNode child) {
		if (children == null)
			children = new ArrayList();
		children.add(child);
		if (child instanceof NavigationElement)
			((NavigationElement)child).addParent(this);
	}
	/**
	 * Adds parent parents of this element
	 * called by addChild method
	 */
	protected void addParent(INavigationNode parent) {
		if (parents == null)
			parents = new ArrayList();
		parents.add(parent);
	}
	
	/**
	 * Removes a child
	 */
	public void removeChild(INavigationNode child)
	{
		// first, remove the parent of the child
		((NavigationElement)child).getParents().remove(this);
		// remove the child now
		getChildren().remove(child);
	}
	
	
	/**
	 * Obtains children
	 * @return INavigationNode List
	 */
	public List getChildren() {
		if (children == null)
			return emptyList;
		return children;
	}
	/**
	 * Obtains parents
	 * @return INavigationNode List
	 */
	protected List getParents() {
		if (parents == null)
			return emptyList;
		return parents;
	}
	
	/**
	 * @return ITopic list
	 */
	public List getChildTopics() {
		if (children == null)
			return emptyList;
		List childTopics = new ArrayList(children.size());
		for (Iterator childrenIt = children.iterator(); childrenIt.hasNext();) {
			NavigationElement c = (NavigationElement) childrenIt.next();
			if ((c instanceof Topic)) {
				childTopics.add(c);
			} else {
				// it is a topics, anchor or include,
				// which may have children attached to it.
				childTopics.addAll(c.getChildTopics());
			}
		}
		return childTopics;
	}
}