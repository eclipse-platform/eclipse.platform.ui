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
 * This class may be subclassed by clients.
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
public class TwoWayDelta extends AbstractDelta implements ITwoWayDiff {

	private final int flags;	
	private final Object before;
	private final Object after;

	/**
	 * Create a two-way delta
	 * @param path the path of the model object that has changed
	 * @param kind the kind of change
	 * @param flags additional flags that describe the change
	 * @param before the before state of the model object
	 * @param after the after state of the model object
	 */
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
