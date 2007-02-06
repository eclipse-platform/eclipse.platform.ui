/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.internal.databinding.observable.tree;

/**
 * @since 1.1
 * 
 */
public abstract class TreeDiffNode {

	/**
	 * 
	 */
	public final static int NO_CHANGE = 0x00;

	/**
	 * 
	 */
	public final static int ADDED = 0x01;

	/**
	 * 
	 */
	public final static int REMOVED = 0x02;

	/**
	 * 
	 */
	public final static int REPLACED = 0x03;

	/**
	 * 
	 */
	public static final TreeDiffNode[] NO_CHILDREN = new TreeDiffNode[0];

	/**
	 * 
	 */
	public static final int INDEX_UNKNOWN = -1;

	/**
	 * @return the change type
	 */
	public abstract int getChangeType();

	/**
	 * @return the element that was removed, or the replaced element
	 */
	public abstract Object getOldElement();

	/**
	 * @return the element that was not changed, added, or the replacement
	 *         element
	 */
	public abstract Object getNewElement();

	/**
	 * @return the index at which the element was added, removed, or replaced
	 */
	public abstract int getIndex();

	/**
	 * Returns the child tree diff objects that describe changes to children. If
	 * the change type is REMOVED, there will be no children.
	 * 
	 * @return the nodes representing changes to children
	 */
	public abstract TreeDiffNode[] getChildren();

	protected void doAccept(TreeDiffVisitor visitor, TreePath parentPath) {
		TreePath currentPath = parentPath.createChildPath(getNewElement());
		boolean recurse = visitor.visit(this, currentPath);
		if (recurse) {
			TreeDiffNode[] children = getChildren();
			for (int i = 0; i < children.length; i++) {
				TreeDiffNode child = children[i];
				child.doAccept(visitor, currentPath);
			}
		}
	}

}
