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
import org.eclipse.team.core.delta.ITwoWayDelta;

/**
 * An implementation of ITwoWayDelta
 */
public class TwoWayDelta extends AbstractDelta implements ITwoWayDelta {

	private final int flags;
	
	private final Object before;
	private final Object after;

	public TwoWayDelta(IPath path, int kind, int flags, Object before, Object after) {
		super(path, kind);
		this.flags = flags;
		this.before = before;
		this.after = after;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ITwoWayDelta#getFlags()
	 */
	public int getFlags() {
		return flags;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getMovedToPath()
	 */
	public IPath getMovedToPath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getMovedFromPath()
	 */
	public IPath getMovedFromPath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getBeforeState()
	 */
	public Object getBeforeState() {
		return before;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getAfterState()
	 */
	public Object getAfterState() {
		return after;
	}

}
