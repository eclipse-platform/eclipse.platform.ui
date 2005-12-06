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
import org.eclipse.team.core.delta.IThreeWayDelta;
import org.eclipse.team.core.delta.ITwoWayDelta;

/**
 * Class for {@link IThreeWayDelta}.
 */
public class ThreeWayDelta extends AbstractDelta implements IThreeWayDelta {

	private final ITwoWayDelta localChange;
	private final ITwoWayDelta remoteChange;
	private final int conflictHint;

	public ThreeWayDelta(IPath path, ITwoWayDelta localChange, ITwoWayDelta remoteChange, int conflictHint) {
		super(path, calculateKind(localChange, remoteChange));
		this.localChange = localChange;
		this.remoteChange = remoteChange;
		this.conflictHint = conflictHint;
	}

	private static int calculateKind(ITwoWayDelta localChange, ITwoWayDelta remoteChange) {
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
	public ITwoWayDelta getLocalChange() {
		return localChange;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.IThreeWayDelta#getRemoteChange()
	 */
	public ITwoWayDelta getRemoteChange() {
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
