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
package org.eclipse.help.internal.toc;

import java.util.*;

/**
 * Navigation Element. Common for all objects definable in toc.xml
 */
abstract class TocNode implements ITocNode {
	protected List children;
	protected List parents;

	/**
	 * Adds another element as child of this element Modifies parents of a child
	 * as well
	 */
	public void addChild(ITocNode child) {
		if (children == null)
			children = new ArrayList();
		children.add(child);
		if (child instanceof TocNode)
			((TocNode) child).addParent(this);
	}
	/**
	 * Adds parent parents of this element called by addChild method
	 */
	protected void addParent(ITocNode parent) {
		if (parents == null)
			parents = new ArrayList();
		parents.add(parent);
	}

	/**
	 * Obtains children
	 * 
	 * @return ITocNode List
	 */
	public List getChildren() {
		if (children == null)
			return Collections.EMPTY_LIST;
		return children;
	}
	/**
	 * Obtains parents
	 * 
	 * @return ITocNode List
	 */
	protected List getParents() {
		if (parents == null)
			return Collections.EMPTY_LIST;
		return parents;
	}

	/**
	 * @return ITopic list
	 */
	public List getChildTopics() {
		if (children == null)
			return Collections.EMPTY_LIST;
		List childTopics = new ArrayList(children.size());
		for (Iterator childrenIt = children.iterator(); childrenIt.hasNext();) {
			TocNode c = (TocNode) childrenIt.next();
			if ((c instanceof Topic)) {
				childTopics.add(c);
			} else {
				// it is a Toc, Anchor or Link,
				// which may have children attached to it.
				childTopics.addAll(c.getChildTopics());
			}
		}
		return childTopics;
	}
}
