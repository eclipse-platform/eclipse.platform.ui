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
package org.eclipse.core.internal.watson;

import org.eclipse.core.internal.dtree.NodeComparison;
import org.eclipse.core.runtime.IPath;

/**
 * An element delta describes how an element has been affected
 * in an element tree delta.
 */
public class ElementDelta {
	protected ElementTreeDelta treeDelta;
	protected IPath pathInDelta;
	protected IPath pathInTrees;
	protected NodeComparison comparison;

	/** Constructs an element delta.
	 */
	ElementDelta(ElementTreeDelta treeDelta, IPath pathInTrees, IPath pathInDelta, NodeComparison comparison) {
		this.treeDelta = treeDelta;
		this.pathInDelta = pathInDelta;
		this.pathInTrees = pathInTrees;
		this.comparison = comparison;
	}

	/**
	 * Destroys this delta and all trees deltas referenced herein.
	 */
	public void destroy() {
		treeDelta.destroy();
		treeDelta = null;
		pathInDelta = null;
		pathInTrees = null;
		comparison = null;
	}

	/**
	 * Returns any elements in the new tree that match the given filter query.
	 */
	protected ElementDelta[] getAddedChildren(IDeltaFilter filter) {
		IPath[] children = treeDelta.getElementTree().getChildren(pathInTrees);
		ElementDelta[] deltas = new ElementDelta[children.length];
		for (int i = children.length; --i >= 0;) {
			deltas[i] = new ElementDelta(treeDelta, children[i], pathInDelta.append(children[i].lastSegment()), comparison);
		}
		return deltas;
	}

	/**
	 * Returns ElementDeltas for any elements that match the given filter query.  
	 */
	public ElementDelta[] getAffectedChildren(IDeltaFilter filter) {
		switch (comparison.getComparison()) {
			case NodeComparison.K_CHANGED :
				return treeDelta.getAffectedElements(pathInDelta, filter);
			case NodeComparison.K_ADDED :
				/**
				 * Assume all children have the same user comparison as
				 * this node.  Is this a reasonable assumption?
				 */
				if (filter.includeElement(comparison.getUserComparison())) {
					return getAddedChildren(filter);
				}
				break;
			case NodeComparison.K_REMOVED :
				if (filter.includeElement(comparison.getUserComparison())) {
					return getRemovedChildren(filter);
				}
				break;
		}
		return new ElementDelta[0];
	}

	/**
	 * Returns an integer describing the changes in this element between the
	 * old and new trees.  This integer can be assigned any value by the
	 * ElementTree client.  A comparison value of 0 indicates that there was
	 * no change in the data.
	 * @see IElementComparator
	 */
	public int getComparison() {
		return comparison.getUserComparison();
	}

	/**
	 * Returns the path of the element described by this delta.
	 */
	public IPath getPath() {
		return pathInTrees;
	}

	/**
	 * Returns any elements in the old tree that match the given filter query.
	 */
	protected ElementDelta[] getRemovedChildren(IDeltaFilter filter) {
		IPath[] children = treeDelta.getParent().getChildren(pathInTrees);
		ElementDelta[] deltas = new ElementDelta[children.length];
		for (int i = children.length; --i >= 0;) {
			deltas[i] = new ElementDelta(treeDelta, children[i], pathInDelta.append(children[i].lastSegment()), comparison);
		}
		return deltas;
	}

	/**
	 * Returns the element tree delta containing this element delta.
	 */
	public ElementTreeDelta getTreeDelta() {
		return treeDelta;
	}

	/**
	 * Return true if there are deltas describing affected children of the receiver
	 * including added, removed or changed children.
	 */
	public boolean hasAffectedChildren(IDeltaFilter filter) {
		int compare = comparison.getComparison();

		/* search delta tree if it's a change */
		if (compare == NodeComparison.K_CHANGED) {
			return treeDelta.hasAffectedElements(pathInDelta, filter);
		}

		/**
		 * For added and deleted nodes, assume children have same
		 * comparison as parent.  Is this reasonable?
		 */
		if (filter.includeElement(comparison.getUserComparison())) {
			if (compare == NodeComparison.K_ADDED)
				// look in new tree 
				return treeDelta.getElementTree().getChildCount(pathInTrees) > 0;
			// look in parent tree
			return treeDelta.getParent().getChildCount(pathInTrees) > 0;
		}
		return false;
	}

	/**
	 * Returns a string representation of the receiver.  Used for debugging
	 * purposes only
	 */
	public String toString() {
		return "ElementDelta(" + pathInTrees + ")"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}