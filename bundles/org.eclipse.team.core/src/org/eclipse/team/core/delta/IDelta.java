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
import org.eclipse.core.runtime.IPath;

/**
 * A delta describes differences between multiple states of the same tree
 * based model. 
 * 
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
	 * Returns the full, absolute path of the delta with respect to the
	 * model root.
	 * <p>
	 * Note: the returned path never has a trailing separator.
	 * </p>
	 * 
	 * @return the full, absolute path of this delta
	 */
	public IPath getPath();

	/**
	 * Returns the kind of this delta. Normally, one of
	 * <code>ADDED</code>, <code>REMOVED</code>, <code>CHANGED</code>.
	 * 
	 * @return the kind of this delta
	 * @see IDelta#ADDED
	 * @see IDelta#REMOVED
	 * @see IDelta#CHANGED
	 */
	public int getKind();
}
