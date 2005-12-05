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
 * Clients that need to create deltas should instead use or subclass
 * {@link AbstractDelta}
 * </p>
 * 
 * @since 3.2
 */
public interface IThreeWayDelta extends IDelta {
	
	/*====================================================================
	 * Constants defining synchronization direction: 
	 *====================================================================*/
	
	/**
	 * Sync constant (value 4) indicating a change to the local resource.
	 * 
	 * @see IThreeWayDelta#getDirection()
	 */
	public static final int OUTGOING = 4;
	
	/**
	 * Sync constant (value 8) indicating a change to the remote resource.
	 * 
	 * @see IThreeWayDelta#getDirection()
	 */
	public static final int INCOMING = 8;
	
	/**
	 * Sync constant (value 12) indicating a change to both the 
	 * remote and local resources. This flag is equivalent
	 * to <code>OUTGOING | INCOMING</code>.
	 * 
	 * @see IThreeWayDelta#getDirection()
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
	 * {@link IDelta#NO_CHANGE} is returned or <code>null</code>
	 * may be returned.
	 * @return the local change associated with this delta or <code>null</code>
	 */
	public ITwoWayDelta getLocalChange();
	
	/**
	 * Return the remote change associated with this delta.
	 * If there is no remote change, either a delta with kind 
	 * {@link IDelta#NO_CHANGE} is returned or <code>null</code>
	 * may be returned.
	 * @return the remote change associated with this delta or <code>null</code>
	 */
	public ITwoWayDelta getRemoteChange();
	
	/**
	 * Return the direction of this three-way delta.
	 * @return the direction of this three-way delta
	 * @see IThreeWayDelta#INCOMING
	 * @see IThreeWayDelta#OUTGOING
	 * @see IThreeWayDelta#CONFLICTING
	 */
	public int getDirection();
	
	/**
	 * For conflicts, return a hint as to the type of conflict.
	 * May be one of <code>PSEUDO_CONFLICT</code> or <code>AUTOMERGE_CONFLICT</code>.
	 * 
	 * @return a hint as to the type of conflict
	 * @see IThreeWayDelta#PSEUDO_CONFLICT
	 * @see IThreeWayDelta#AUTOMERGE_CONFLICT
	 */
	public int getConflictHint();

}
