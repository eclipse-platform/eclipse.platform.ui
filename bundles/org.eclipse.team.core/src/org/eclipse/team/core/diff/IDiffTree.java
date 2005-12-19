/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.diff;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A diff tree provides access to a tree of {@link IDiffNode} instances.
 * For efficiency reasons, the tree only provides diffs for paths that represent a change.
 * Paths that do not contain a diff represent but are returned from the tree will
 * contain child paths in the set.
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * should use {@link org.eclipse.team.core.mapping.provider.DiffTree} instead.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see org.eclipse.team.core.mapping.provider.DiffTree
 * @since 3.2
 */
public interface IDiffTree {

	/**
	 * Add a listener to the tree. The listener will be informed of any changes
	 * in the tree. Registering a listener that is already registered will have
	 * no effects.
	 * 
	 * @param listener the listener to be added
	 */
	public void addDiffChangeListener(IDiffChangeListener listener);

	/**
	 * Remove the listener from the tree. Removing a listener that is not
	 * registered has no effect.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removeDiffChangeListener(IDiffChangeListener listener);

	/**
	 * Accepts the given visitor. The only kinds of deltas visited are
	 * <code>ADDED</code>, <code>REMOVED</code>, and <code>CHANGED</code>.
	 * The visitor's <code>visit</code> method is called with the given delta
	 * if applicable. If the visitor returns <code>true</code>, any of the
	 * delta's children in this tree are also visited.
	 * 
	 * @param delta the delta to be visited
	 * @param visitor the visitor
	 * @param depth the depth to visit
	 * @exception CoreException if the visitor failed with this exception.
	 * @see IDiffVisitor#visit(IDiffNode)
	 */
	public void accept(IPath path, IDiffVisitor visitor, int depth)
			throws CoreException;

	/**
	 * Returns the delta identified by the given path,
	 * or <code>null</code> if there is no delta at that path. The supplied path
	 * may be absolute or relative; in either case, it is interpreted as
	 * relative to the workspace. Trailing separators are ignored.
	 * <p>
	 * This method only returns a delta if there is a change at the given
	 * path. To know if there are deltas in descendent paths, clients
	 * should class {@link #getChildren(IPath) }.
	 * 
	 * @param path the path of the desired delta
	 * @return the delta, or <code>null</code> if no such
	 *         delta exists
	 */
	public IDiffNode getDiff(IPath path);

	/**
	 * Returns the child paths of the given path that either point to
	 * a sync delta or have a descendant path that points to a sync delta.
	 * Returns an empty array if there are no sync deltas that are descendents
	 * of the given path.
	 * 
	 * @return the child paths of the given path that either point to
	 * a sync delta or have a descendant path that points to a sync delta
	 */
	public IPath[] getChildren(IPath parent);

	/**
	 * Return whether the set is empty.
	 * @return whether the set is empty
	 */
	public boolean isEmpty();

}
