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
package org.eclipse.team.internal.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;

/**
 * A synchronization scope for a set of resources.
 * 
 */
public class SynchronizationScope extends AbstractSynchronizationScope {

	private ResourceTraversal[] traversals;
	
	public SynchronizationScope(ResourceTraversal[] traversals) {
		this.traversals = traversals;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.ISynchronizationScope#getTraversals()
	 */
	public ResourceTraversal[] getTraversals() {
		return traversals;
	}

	/**
	 * Set the traversal of this scope to a single traversal
	 * of infinite depth on the given resources.
	 * @param roots the new roots of the scope
	 */
	public void setRoots(IResource[] roots) {
		setTraversals(new ResourceTraversal[] {new ResourceTraversal(roots, IResource.DEPTH_INFINITE, IResource.NONE)});
	}

	/**
	 * Set the traversals to the given traversals and notify the
	 * listeners of the change.
	 * @param traversals the new traversals that define the scope
	 */
	protected void setTraversals(ResourceTraversal[] traversals) {
		ResourceTraversal[] oldTraversals = this.traversals;
		this.traversals = traversals;
		fireTraveralsChangedEvent(oldTraversals, traversals);
	}

}
