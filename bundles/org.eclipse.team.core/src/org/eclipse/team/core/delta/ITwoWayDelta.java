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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.core.delta.TwoWayDelta;

/**
 * A two-way delta represents the changes between two file resource trees.
 * It is modeled after the {@link IResourceDelta} but is simplified.
 * The delta can be used to describe the change between an ancestor and
 * remote, an ancestor and local or between the local and a remote 
 * for two-way comparisons.
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients that need to create deltas should instead use or subclass
 * {@link TwoWayDelta}
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see IDeltaTree
 * 
 * @since 3.2
 */
public interface ITwoWayDelta extends IDelta {

	/*====================================================================
	 * Constants which describe resource changes:
	 *====================================================================*/

	/**
	 * Change constant (bit mask) indicating that the content of the resource has changed.
	 * 
	 * @see ITwoWayDelta#getFlags() 
	 */
	public static final int CONTENT = 0x100;

	/**
	 * Change constant (bit mask) indicating that the resource was moved from another location.
	 * The location in the "before" state can be retrieved using <code>getMovedFromPath()</code>.
	 * 
	 * @see ITwoWayDelta#getFlags()
	 */
	public static final int MOVED_FROM = 0x1000;

	/**
	 * Change constant (bit mask) indicating that the resource was moved to another location.
	 * The location in the new state can be retrieved using <code>getMovedToPath()</code>.
	 * 
	 * @see ITwoWayDelta#getFlags()
	 */
	public static final int MOVED_TO = 0x2000;

	/**
	 * Change constant (bit mask) indicating that the type of the resource has changed.
	 * TODO: I think that type changes would need to be handled by the
	 * repository tooling before the delta tree is presented to the model
	 * 
	 * @see ITwoWayDelta#getFlags()
	 */
	public static final int TYPE = 0x8000;

	/**
	 * Change constant (bit mask) indicating that the resource has been
	 * replaced by another at the same location (i.e., the resource has 
	 * been deleted and then added). 
	 * 
	 * @see ITwoWayDelta#getFlags()
	 */
	public static final int REPLACED = 0x40000;
	
	/**
	 * Change constant (bit mask) indicating that the encoding of the resource has changed.
	 * TODO: I think that type changes would need to be handled by the
	 * repository tooling before the delta tree is presented to the model
	 * @see ITwoWayDelta#getFlags() 
	 * @since 3.0
	 */
	public static final int ENCODING = 0x100000;
	
	/**
	 * Returns flags which describe in more detail how a resource has been affected.
	 * <p>
	 * The following codes (bit masks) are used when kind is <code>CHANGED</code>, and
	 * also when the resource is involved in a move:
	 * <ul>
	 * <li><code>CONTENT</code> - The bytes contained by the resource have 
	 * 		been altered.</li>
	 * <li><code>ENCODING</code> - The encoding of the resource may have been altered.
	 * This flag is not set when the encoding changes due to the file being modified, 
	 * or being moved.</li>
	 * <li><code>TYPE</code> - The resource (a folder or file) has changed its type.</li>
	 * <li><code>REPLACED</code> - The resource (and all its properties)
	 *  was deleted (either by a delete or move), and was subsequently re-created
	 *  (either by a create, move, or copy).</li>
	 * </ul>
	 * The following code is only used if kind is <code>REMOVED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_TO</code> - The resource has moved.
	 * 	<code>getMovedToPath</code> will return the path of where it was moved to.</li>
	 * </ul>
	 * The following code is only used if kind is <code>ADDED</code>
	 * (or <code>CHANGED</code> in conjunction with <code>REPLACED</code>):
	 * <ul>
	 * <li><code>MOVED_FROM</code> - The resource has moved.
	 * 	<code>getMovedFromPath</code> will return the path of where it was moved from.</li>
	 * </ul>
	 * A simple move operation would result in the following delta information.
	 * If a resource is moved from A to B (with no other changes to A or B), 
	 * then A will have kind <code>REMOVED</code>, with flag <code>MOVED_TO</code>, 
	 * and <code>getMovedToPath</code> on A will return the path for B.  
	 * B will have kind <code>ADDED</code>, with flag <code>MOVED_FROM</code>, 
	 * and <code>getMovedFromPath</code> on B will return the path for A.
	 * B's other flags will describe any other changes to the resource, as compared
	 * to its previous location at A.
	 * </p>
	 * <p>
	 * Note that the move flags only describe the changes to a single resource; they
	 * don't necessarily imply anything about the parent or children of the resource.  
	 * If the children were moved as a consequence of a subtree move operation, 
	 * they will have corresponding move flags as well.
	 * </p>
	 * <p>
	 * Note that it is possible for a file resource to be replaced in the workspace
	 * by a folder resource (or the other way around).
	 * The resource delta, which is actually expressed in terms of
	 * paths instead or resources, shows this as a change to either the
	 * content or children.
	 * </p>
	 *
	 * @return the flags
	 * @see ITwoWayDelta#CONTENT
	 * @see ITwoWayDelta#ENCODING
	 * @see ITwoWayDelta#MOVED_TO
	 * @see ITwoWayDelta#MOVED_FROM
	 * @see ITwoWayDelta#TYPE
	 * @see ITwoWayDelta#REPLACED
	 * @see #getKind()
	 * @see #getMovedFromPath()
	 * @see #getMovedToPath()
	 * @see IResource#move(IPath, int, IProgressMonitor)
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
	
	/**
	 * Return a handle to the object representing the
	 * "before" state used to calculate this delta.
	 * A <code>null</code> is returned if the object did
	 * not exist in the before state.
	 * @return a handle to the object representing the
	 * "before" state used to calculate this delta
	 */
	public Object getBeforeState();
	
	/**
	 * Return a handle to the object representing the
	 * "after" state used to calculate this delta.
	 * A <code>null</code> is returned if the object did
	 * not exist in the before state.
	 * @return a handle to the object representing the
	 * "after" state used to calculate this delta
	 */
	public Object getAfterState();

}
