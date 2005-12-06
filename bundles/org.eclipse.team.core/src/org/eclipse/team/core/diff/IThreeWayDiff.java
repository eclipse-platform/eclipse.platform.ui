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

import org.eclipse.team.internal.core.diff.ThreeWayDiff;


/**
 * A three-way delta that describe the synchronization state between
 * two contributors and an ancestor. For simplicity, we refer to
 * one of the contributors as the local and the other as the remote.
 * A three-way delta is represented as a combination of two two-way 
 * deltas, one between the ancestor and local and the other between the
 * ancestor and remote. For a three-way delta, clients can assume that
 * the before state of both the local and remote changes are the same.
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients that need to create deltas should instead use
 * {@link ThreeWayDiff}
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p> 
 * @since 3.2
 */
public interface IThreeWayDiff extends IDiffNode {
	
	/*====================================================================
	 * Constants defining synchronization direction: 
	 *====================================================================*/
	
	/**
	 * Sync constant (value 4) indicating a change to the local resource.
	 * 
	 * @see IThreeWayDiff#getDirection()
	 */
	public static final int OUTGOING = 4;
	
	/**
	 * Sync constant (value 8) indicating a change to the remote resource.
	 * 
	 * @see IThreeWayDiff#getDirection()
	 */
	public static final int INCOMING = 8;
	
	/**
	 * Sync constant (value 12) indicating a change to both the 
	 * remote and local resources. This flag is equivalent
	 * to <code>OUTGOING | INCOMING</code>.
	 * 
	 * @see IThreeWayDiff#getDirection()
	 */
	public static final int CONFLICTING = OUTGOING | INCOMING;
	
	/*====================================================================
	 * Constants defining synchronization conflict types:
	 *====================================================================*/
	
	/**
	 * Sync constant (value 16) indication that both the local and remote resources have changed 
	 * relative to the base but their contents are the same. 
	 */
	public static final int PSEUDO_CONFLICT = 16;
	
	/**
	 * Sync constant (value 32) indicating that both the local and remote resources have changed 
	 * relative to the base but their content changes do not conflict (e.g. source file changes on different 
	 * lines). These conflicts could be merged automatically.
	 */
	public static final int AUTOMERGE_CONFLICT = 32;
	
	/**
	 * Return the local change associated with this delta.
	 * If there is no local change, either a delta with kind 
	 * {@link IDiffNode#NO_CHANGE} is returned or <code>null</code>
	 * may be returned.
	 * @return the local change associated with this delta or <code>null</code>
	 */
	public ITwoWayDiff getLocalChange();
	
	/**
	 * Return the remote change associated with this delta.
	 * If there is no remote change, either a delta with kind 
	 * {@link IDiffNode#NO_CHANGE} is returned or <code>null</code>
	 * may be returned.
	 * @return the remote change associated with this delta or <code>null</code>
	 */
	public ITwoWayDiff getRemoteChange();
	
	/**
	 * Return the direction of this three-way delta.
	 * @return the direction of this three-way delta
	 * @see IThreeWayDiff#INCOMING
	 * @see IThreeWayDiff#OUTGOING
	 * @see IThreeWayDiff#CONFLICTING
	 */
	public int getDirection();
	
	/**
	 * For conflicts, return a hint as to the type of conflict.
	 * May be one of <code>PSEUDO_CONFLICT</code> or <code>AUTOMERGE_CONFLICT</code>.
	 * 
	 * @return a hint as to the type of conflict
	 * @see IThreeWayDiff#PSEUDO_CONFLICT
	 * @see IThreeWayDiff#AUTOMERGE_CONFLICT
	 */
	public int getConflictHint();

}
