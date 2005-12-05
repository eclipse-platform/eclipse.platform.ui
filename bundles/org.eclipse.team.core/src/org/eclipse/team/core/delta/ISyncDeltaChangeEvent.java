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
 * A change event that describes changes that have occurred
 * in an {@link ISyncDeltaTree}.
 * <p>
 * This interface is not intended to be implemented by clients.
 * 
 * @since 3.2
 */
public interface ISyncDeltaChangeEvent {
	
	/**
	 * Returns the tree that has been changed.
	 * @return the tree that has been changed.
	 */
	public ISyncDeltaTree getTree();
	
	public ISyncDelta[] getAdditions();
	
	public IPath[] getRemovals();
	
	public ISyncDelta[] getChanges();
}
