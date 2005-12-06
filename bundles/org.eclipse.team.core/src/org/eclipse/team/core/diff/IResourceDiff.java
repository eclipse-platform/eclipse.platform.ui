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

import org.eclipse.team.core.variants.IResourceVariant;

/**
 * A resource diff represents the changes between two resources.
 * The diff can be used to describe the change between an ancestor and
 * remote, an ancestor and local or between the local and a remote 
 * for two-way comparisons.
 * <p>
 * This interface is not intended to be implemented by clients.
 * Clients that need to create deltas should instead use or subclass
 * {@link ResourceDiff}
 * </p>
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see IDiffTree
 * 
 * @since 3.2
 */
public interface IResourceDiff extends ITwoWayDiff {

	/**
	 * Return a handle to the resource variant representing the
	 * "before" state used to calculate this diff.
	 * A <code>null</code> is returned if the resource did
	 * not exist in the before state.
	 * @return a handle to the resource variant representing the
	 * "before" state used to calculate this diff
	 */
	public IResourceVariant getBeforeState();
	
	/**
	 * Return a handle to the resource variant representing the
	 * "after" state used to calculate this diff.
	 * A <code>null</code> is returned if the resource does
	 * not exist in the after state.
	 * @return a handle to the resource variant representing the
	 * "after" state used to calculate this diff
	 */
	public IResourceVariant getAfterState();
}
