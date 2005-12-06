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
package org.eclipse.team.core.diff;

import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.core.diff.TwoWayDiff;

/**
 * A two-way diff represents the changes between two states of the same object,
 * referred to as the "before" state and the "after" state.
 * It is modeled after the {@link IResourceDelta} but is simplified.
 * <p>
 * This interface is not intended to be implemented by clients. Clients that
 * need to create two-way diffs should instead use or subclass {@link TwoWayDiff}
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see IDiffTree
 * 
 * @since 3.2
 */
public interface ITwoWayDiff extends IDiffNode {

	/*====================================================================
	 * Constants which describe resource changes:
	 *====================================================================*/

	/**
	 * Change constant (bit mask) indicating that the content of the object has changed.
	 * 
	 * @see ITwoWayDiff#getFlags() 
	 */
	public static final int CONTENT = 0x100;

	/**
	 * Change constant (bit mask) indicating that the object was moved from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.
	 * 
	 * @see ITwoWayDiff#getFlags()
	 */
	public static final int MOVED_FROM = 0x1000;

	/**
	 * Change constant (bit mask) indicating that the object was moved to another location.
	 * The location in the new state can be retrieved using <code>getMovedToPath()</code>.
	 * 
	 * @see ITwoWayDiff#getFlags()
	 */
	public static final int MOVED_TO = 0x2000;

	/**
	 * Change constant (bit mask) indicating that the object has been
	 * replaced by another at the same location (i.e., the object has 
	 * been deleted and then added). 
	 * 
	 * @see ITwoWayDiff#getFlags()
	 */
	public static final int REPLACED = 0x40000;
	
	/**
	 * Returns flags which describe in more detail how a object has been affected.
	 * <p>
	 * The following codes (bit masks) are used when kind is <code>CHANGED</code>, and
	 * also when the object is involved in a move:
	 * <ul>
	 * <li><code>CONTENT</code> - The bytes contained by the resource have 
	 * 		been altered.</li>
	 * <li><code>REPLACED</code> - The object
	 *  was deleted (either by a delete or move), and was subsequently re-created
	 *  (either by a create, move, or copy).</li>
	 * </ul>
	 * The following code is only used if kind is <code>REMOVED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_TO</code> - The object has moved.
	 * 	<code>getMovedToPath</code> will return the path of where it was moved to.</li>
	 * </ul>
	 * The following code is only used if kind is <code>ADDED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_FROM</code> - The object has moved.
	 * 	<code>getMovedFromPath</code> will return the path of where it was moved from.</li>
	 * </ul>
	 * A simple move operation would result in the following diff information.
	 * If a object is moved from A to B (with no other changes to A or B), 
	 * then A will have kind <code>REMOVED</code>, with flag <code>MOVED_TO</code>, 
	 * and <code>getMovedToPath</code> on A will return the path for B.  
	 * B will have kind <code>ADDED</code>, with flag <code>MOVED_FROM</code>, 
	 * and <code>getMovedFromPath</code> on B will return the path for A.
	 * B's other flags will describe any other changes to the resource, as compared
	 * to its previous location at A.
	 * </p>
	 * <p>
	 * Note that the move flags only describe the changes to a single object; they
	 * don't necessarily imply anything about the parent or children of the object.  
	 * If the children were moved as a consequence of a subtree move operation, 
	 * they will have corresponding move flags as well.
	 * </p>
	 *
	 * @return the flags
	 * @see ITwoWayDiff#CONTENT
	 * @see ITwoWayDiff#MOVED_TO
	 * @see ITwoWayDiff#MOVED_FROM
	 * @see ITwoWayDiff#REPLACED
	 * @see #getKind()
	 * @see #getMovedFromPath()
	 * @see #getMovedToPath()
	 */
	public int getFlags();
	
	/**
	 * Returns the full path (in the "before" state) from which this resource 
	 * (in the "after" state) was moved.  This value is only valid 
	 * if the <code>MOVED_FROM</code> change flag is set; otherwise,
	 * <code>null</code> is returned.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 *
	 * @return a path, or <code>null</code>
	 * @see #getMovedToPath()
	 * @see #getPath()
	 * @see #getFlags()
	 */
	public IPath getMovedFromPath();

	/**
	 * Returns the full path (in the "after" state) to which this resource 
	 * (in the "before" state) was moved.  This value is only valid if the 
	 * <code>MOVED_TO</code> change flag is set; otherwise,
	 * <code>null</code> is returned.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * 
	 * @return a path, or <code>null</code>
	 * @see #getMovedFromPath()
	 * @see #getPath()
	 * @see #getFlags()
	 */
	public IPath getMovedToPath();

}
