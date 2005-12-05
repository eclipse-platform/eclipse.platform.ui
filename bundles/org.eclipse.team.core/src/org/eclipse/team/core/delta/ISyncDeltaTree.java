/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.delta;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * A sync delta tree provides access to a tree of {@link ISyncDelta} instances.
 * For efficiency reasons, the tree only provides deltas for paths that represent a change.
 * Paths that do not contain a delta represent nodes for which the files or folders
 * being compared are considered in-sync.
 * 
 * @since 3.2
 */
public interface ISyncDeltaTree {

	/**
	 * Add a listener to the tree. The listener will be informed of any changes
	 * in the tree. Registering a listener that is already registered will have
	 * no effects.
	 * 
	 * @param listener the listener to be added
	 */
	public void addSyncDeltaChangeListener(ISyncDeltaChangeListener listener);

	/**
	 * Remove the listener from the tree. Removing a listener that is not
	 * registered has no effect.
	 * 
	 * @param listener the listener to be removed
	 */
	public void removeSyncDeltaChangeListener(ISyncDeltaChangeListener listener);

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
	 * @see ISyncDeltaVisitor#visit(ISyncDelta)
	 */
	public void accept(IPath path, ISyncDeltaVisitor visitor, int depth)
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
	public ISyncDelta getDelta(IPath path);

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
