/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers.caches;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.IResourceVariant;

/**
 * A handle that provides access to locally cached resource variants that 
 * represent a resource line-up such as a project version or branch.
 */
public interface IResourceVariantTree {
	
	public abstract IResource[] getRoots();
	
	/**
	 * Returns the members of the local resource that have resource variants in this tree.
	 * The members may or may not exist locally. The resource variants corresponding to the
	 * memebers can be retrieved using <code>getResourceVariant(IResource)</code>.
	 * @param resource the local resource
	 * @return the members of the local resource for which this tree contains resource variants
	 * @throws TeamException
	 */
	public abstract IResource[] members(IResource resource) throws TeamException;
	
	public abstract IResourceVariant getResourceVariant(IResource resource) throws TeamException;
	
	/**
	 * Refreshes the resource variant tree for the specified resources and possibly 
	 * their descendants, depending on the depth.
	 * @param resources the resources whose variants should be refreshed
	 * @param depth the depth of the refresh (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @param monitor a progress monitor
	 * @return the array of resources whose corresponding variants have changed
	 * as a result of the refresh
	 * @throws TeamException
	 */
	public abstract IResource[] refresh(
			IResource[] resources, 
			int depth,
			IProgressMonitor monitor) throws TeamException;
}