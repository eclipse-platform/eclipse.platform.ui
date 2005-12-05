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
import org.eclipse.core.runtime.IPath;

/**
 * A synchronization delta describes differences between different copies of the
 * same file system. Typically a synchronization is either two-way or three-way.
 * 
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients that need to create deltas should instead use or subclass
 * {@link AbstractDelta}
 * </p>
 * 
 * @see ITwoWayDelta
 * @see IThreeWayDelta
 * @since 3.2
 */
public interface IDelta {

	/*
	 * ====================================================================
	 * Constants defining delta kinds:
	 * ====================================================================
	 */

	/**
	 * Delta kind constant indicating that the resource has not been changed in
	 * any way.
	 * 
	 * @see IDelta#getKind()
	 */
	public static final int NO_CHANGE = 0;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been
	 * added to its parent. That is, one that appears in the "after" state, not
	 * in the "before" one.
	 * 
	 * @see IDelta#getKind()
	 */
	public static final int ADDED = 0x1;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been
	 * removed from its parent. That is, one that appears in the "before" state,
	 * not in the "after" one.
	 * 
	 * @see IDelta#getKind()
	 */
	public static final int REMOVED = 0x2;

	/**
	 * Delta kind constant (bit mask) indicating that the resource has been
	 * changed. That is, one that appears in both the "before" and "after"
	 * states.
	 * 
	 * @see IDelta#getKind()
	 */
	public static final int CHANGED = 0x4;
	
	/**
	 * The bit mask which describes all possible delta kinds.
	 * 
	 * @see IDelta#getKind()
	 */
	public static final int ALL = CHANGED | ADDED | REMOVED;
	
	/**
	 * Returns the full, absolute path of the resource with respect to the
	 * workspace root to which this sync node is associated .
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * </p>
	 * 
	 * @return the full, absolute path of this resource delta
	 * @see IResource#getFullPath()
	 * @see #getProjectRelativePath()
	 */
	public IPath getPath();

	/**
	 * Returns the kind of this resource delta. Normally, one of
	 * <code>ADDED</code>, <code>REMOVED</code>, <code>CHANGED</code>.
	 * 
	 * @return the kind of this resource delta
	 * @see IDelta#ADDED
	 * @see IDelta#REMOVED
	 * @see IDelta#CHANGED
	 */
	public int getKind();
}
