/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.help.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.help.INode;

public class Node extends FilterableUAElement implements INode {

	private Node parent;
	private List children;
	
	public void addChild(Node childToAdd) {
		if (children == null) {
			children = new ArrayList();
		}
		children.add(childToAdd);
		childToAdd.setParent(this);
	}
	
	public void addChildren(Node[] childrenToAdd) {
		for (int i=0;i<childrenToAdd.length;++i) {
			addChild(childrenToAdd[i]);
		}
	}

	public INode[] getChildren() {
		if (children == null) {
			return new INode[0];
		}
		return (INode[])children.toArray(new INode[children.size()]);
	}
	
	/*
	 * Gets the children of a specific type.
	 */
	public List getChildren(Class clazz) {
		if (children != null) {
			List list = new ArrayList();
			getChildren(clazz, list);
			return list;
		}
		return Collections.EMPTY_LIST;
	}

	public void getChildren(Class clazz, Collection collection) {
		if (children != null) {
			Iterator iter = children.iterator();
			while (iter.hasNext()) {
				Object o = iter.next();
				if (clazz.isAssignableFrom(o.getClass())) {
					collection.add(o);
				}
				else if (o instanceof Filter) {
					((Filter)o).getChildren(clazz, collection);
				}
			}
		}
	}

	public Node[] getChildrenInternal() {
		if (children == null) {
			return new Node[0];
		}
		return (Node[])children.toArray(new Node[children.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.help.internal.FilterableUAElement#getFilters()
	 */
	public Map getFilters() {
		Map filters = super.getFilters();
		Node parent = getParentInternal();
		if (parent != null) {
			Map parentFilters = parent.getFilters();
			if (parentFilters != null) {
				if (filters != null) {
					Map allFilters = new HashMap();
					allFilters.putAll(filters);
					allFilters.putAll(parentFilters);
					return allFilters;
				}
				return parentFilters;
			}
		}
		return filters;
	}
	
	public INode getParent() {
		return parent;
	}

	public Node getParentInternal() {
		return parent;
	}

	public void removeChild(Node node) {
		if (children != null) {
			children.remove(node);
		}
	}
	
	public void replaceChild(Node oldNode, Node newNode) {
		if (children != null) {
			int index = children.indexOf(oldNode);
			if (index != -1) {
				children.set(index, newNode);
				newNode.setParent(this);
			}
		}
	}
	
	public void replaceChild(Node oldNode, Node[] newNodes) {
		if (children != null) {
			int index = children.indexOf(oldNode);
			if (index != -1) {
				children.remove(index);
				for (int i=newNodes.length-1;i>=0;--i) {
					children.add(index, newNodes[i]);
					((Node)newNodes[i]).setParent(this);
				}
			}
		}
	}
	
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public void sortChildren(Comparator c) {
		if (children != null) {
			Collections.sort(children, c);
		}
	}
}
