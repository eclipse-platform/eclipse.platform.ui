/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
package org.eclipse.help.internal.toc;

import java.util.*;

/**
 * Navigation Element.
 * Common for all objects definable in toc.xml
 */
abstract class TocNode implements ITocNode {
	private final static List emptyList = new ArrayList(0);
	private List children;
	private List parents;
	
	
	/**
	 * Adds another element as child of this element
	 * Modifies parents of a child as well
	 */
	public void addChild(ITocNode child) {
		if (children == null)
			children = new ArrayList();
		children.add(child);
		if (child instanceof TocNode)
			((TocNode)child).addParent(this);
	}
	/**
	 * Adds parent parents of this element
	 * called by addChild method
	 */
	protected void addParent(ITocNode parent) {
		if (parents == null)
			parents = new ArrayList();
		parents.add(parent);
	}
	
	/**
	 * Removes a child
	 */
	public void removeChild(ITocNode child)
	{
		// first, remove the parent of the child
		((TocNode)child).getParents().remove(this);
		// remove the child now
		getChildren().remove(child);
	}
	
	
	/**
	 * Obtains children
	 * @return ITocNode List
	 */
	public List getChildren() {
		if (children == null)
			return emptyList;
		return children;
	}
	/**
	 * Obtains parents
	 * @return ITocNode List
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
			TocNode c = (TocNode) childrenIt.next();
			if ((c instanceof Topic)) {
				childTopics.add(c);
			} else {
				// it is a toc, anchor or include,
				// which may have children attached to it.
				childTopics.addAll(c.getChildTopics());
			}
		}
		return childTopics;
	}
}