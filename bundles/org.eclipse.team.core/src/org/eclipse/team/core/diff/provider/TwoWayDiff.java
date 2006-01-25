/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.core.diff.provider;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.diff.ITwoWayDiff;

/**
 * Implementation of {@link ITwoWayDiff}. By default, this implementation
 * returns <code>null</code> for the {@link #getFromPath() } and
 * {@link #getToPath() }. Subclasses that support move diffs
 * should override these methods.
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
public class TwoWayDiff extends Diff implements ITwoWayDiff {

	/**
	 * Constant (bit mask) that defines the area of the status that is reserved
	 * for use by this abstract class for encoding the flags of the diff.
	 * However, subclasses may include their own bits in the flag
	 * as long as they do not overlap with the bits in the <code>FLAG_MASK</code>
	 * 
	 * @see Diff#getStatus()
	 */
	protected static final int FLAG_MASK = 0xFF00;

	/**
	 * Create a two-way diff
	 * @param path the path of the model object that has changed
	 * @param kind the kind of change
	 * @param flags additional flags that describe the change
	 * @param before the before state of the model object
	 * @param after the after state of the model object
	 */
	public TwoWayDiff(IPath path, int kind, int flags) {
		super(path, (kind & KIND_MASK) | (flags & ~KIND_MASK));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ITwoWayDelta#getFlags()
	 */
	public int getFlags() {
		return getStatus() & ~KIND_MASK;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getMovedToPath()
	 */
	public IPath getToPath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.delta.ITwoWayDelta#getMovedFromPath()
	 */
	public IPath getFromPath() {
		return null;
	}

}
