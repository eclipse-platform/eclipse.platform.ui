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
import org.eclipse.core.runtime.IPath;

/**
 * A diff node describes differences between two or more model objects.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 *  
 * @see ITwoWayDiff
 * @see IThreeWayDiff
 * @since 3.2
 */
public interface IDiffNode {

	/*
	 * ====================================================================
	 * Constants defining diff kinds:
	 * ====================================================================
	 */

	/**
	 * Diff kind constant (bit mask) indicating that the resource has not been changed in
	 * any way.
	 * 
	 * @see IDiffNode#getKind()
	 */
	public static final int NO_CHANGE = 0;

	/**
	 * Diff kind constant (bit mask) indicating that the resource has been
	 * added to its parent. That is, one that appears in the "after" state, not
	 * in the "before" one.
	 * 
	 * @see IDiffNode#getKind()
	 */
	public static final int ADDED = 0x1;

	/**
	 * Diff kind constant (bit mask) indicating that the resource has been
	 * removed from its parent. That is, one that appears in the "before" state,
	 * not in the "after" one.
	 * 
	 * @see IDiffNode#getKind()
	 */
	public static final int REMOVED = 0x2;

	/**
	 * Diff kind constant (bit mask) indicating that the resource has been
	 * changed. That is, one that appears in both the "before" and "after"
	 * states.
	 * 
	 * @see IDiffNode#getKind()
	 */
	public static final int CHANGED = 0x4;
	
	/**
	 * The bit mask which describes all possible diff kinds.
	 * 
	 * @see IDiffNode#getKind()
	 */
	public static final int ALL = CHANGED | ADDED | REMOVED;
	
	/**
	 * Returns the full, absolute path of the object to which the diff applies
	 * with respect to the model root.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * </p>
	 * 
	 * @return the full, absolute path of this diff
	 */
	public IPath getPath();

	/**
	 * Returns the kind of this diff. Normally, one of
	 * <code>ADDED</code>, <code>REMOVED</code>, <code>CHANGED</code>.
	 * 
	 * @return the kind of this diff
	 * @see IDiffNode#ADDED
	 * @see IDiffNode#REMOVED
	 * @see IDiffNode#CHANGED
	 */
	public int getKind();
}
