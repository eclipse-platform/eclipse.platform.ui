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
import org.eclipse.team.core.delta.IDelta;

/**
 * Abstract implementation of {@link IDelta}
 */
public abstract class AbstractDelta implements IDelta {

	private final IPath path;
	private final int kind;

	/**
	 * Create a sync delta
	 * @param path the path of the delta
	 * @param kind the kind of the delta
	 */
	public AbstractDelta(IPath path, int kind) {
		this.path = path;
		this.kind = kind;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDelta#getFullPath()
	 */
	public IPath getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.synchronize.ISyncDelta#getKind()
	 */
	public int getKind() {
		return kind;
	}

}
