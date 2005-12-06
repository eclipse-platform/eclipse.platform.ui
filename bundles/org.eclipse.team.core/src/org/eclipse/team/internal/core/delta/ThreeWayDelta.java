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
package org.eclipse.team.internal.core.delta;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.diff.IThreeWayDiff;
import org.eclipse.team.core.diff.ITwoWayDiff;

/**
 * Implementation of {@link IThreeWayDiff}.
 * <p>
 * This class is not intended to be subclasses by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class ThreeWayDelta extends AbstractDelta implements IThreeWayDiff {

	private final ITwoWayDiff localChange;
	private final ITwoWayDiff remoteChange;
	private final int conflictHint;

	/**
	 * Create a three-way delta from the two changes
	 * @param path the path of the model object that has changed
	 * @param localChange the local change in the model object or <code>null</code> if there is no local change
	 * @param remoteChange the remote change in the model object or <code>null</code> if there is no local change
	 * @param conflictHint
	 */
	public ThreeWayDelta(IPath path, ITwoWayDiff localChange, ITwoWayDiff remoteChange, int conflictHint) {
		super(path, calculateKind(localChange, remoteChange));
		this.localChange = localChange;
		this.remoteChange = remoteChange;
		this.conflictHint = conflictHint;
	}

	private static int calculateKind(ITwoWayDiff localChange, ITwoWayDiff remoteChange) {
		int localKind = NO_CHANGE;
		if (localChange != null)
			localKind = localChange.getKind();
		int remoteKind = NO_CHANGE;
		if (remoteChange != null)
			remoteKind = remoteChange.getKind();
		if (localKind == NO_CHANGE || localKind == remoteKind)
			return remoteKind;
		if (remoteKind == NO_CHANGE)
			return localKind;
		return CHANGED;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IThreeWayDelta#getLocalChange()
	 */
	public ITwoWayDiff getLocalChange() {
		return localChange;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IThreeWayDelta#getRemoteChange()
	 */
	public ITwoWayDiff getRemoteChange() {
		return remoteChange;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IThreeWayDelta#getDirection()
	 */
	public int getDirection() {
		if (getKind() == NO_CHANGE) {
			return 0;
		}
		int direction = 0;
		if (localChange != null && localChange.getKind() != NO_CHANGE) {
			direction |= OUTGOING;
		}
		if (remoteChange != null && remoteChange.getKind() != NO_CHANGE) {
			direction |= INCOMING;
		}
		return direction;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IThreeWayDelta#getConflictHint()
	 */
	public int getConflictHint() {
		return conflictHint;
	}

}
