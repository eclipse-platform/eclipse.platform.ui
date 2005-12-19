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
package org.eclipse.team.core.synchronize;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;

/**
 * Provides addition API for accessing the <code>SyncInfo</code> in the set through
 * their resource's hierarchical relationships.
 * <p>
 * Events fired from a <code>ISyncInfoTree</code> will be instances of <code>ISyncInfoTreeChangeEvent</code>.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that need an instance of a set can use {@link SyncInfoTree}
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * @see SyncInfoTree
 * @since 3.2
 */
public interface ISyncInfoTree extends ISyncInfoSet {

	/**
	 * Return whether the given resource has any children in the sync set. The children
	 * could be either out-of-sync resources that are contained by the set or containers
	 * that are ancestors of out-of-sync resources contained by the set.
	 * 
	 * @param resource the resource to check for children.
	 * @return <code>true</code> if the resource has children in the set.
	 */
	public boolean hasMembers(IResource resource);

	/**
	 * Return the <code>SyncInfo</code> for each out-of-sync resource in the subtree rooted at the given resource
	 * to the depth specified. The depth is one of:
	 * <ul>
	 * <li><code>IResource.DEPTH_ZERO</code>: the resource only,
	 * <li><code>IResource.DEPTH_ONE</code>: the resource or its direct children,
	 * <li><code>IResource.DEPTH_INFINITE</code>: the resource and all of it's descendants.
	 * <ul>
	 * If the given resource is out of sync, it will be included in the result.
	 * <p>
	 * The default implementation makes use of <code>getSyncInfo(IResource)</code>,
	 * <code>members(IResource)</code> and <code>getSyncInfos()</code>
	 * to provide the varying depths. Subclasses may override to optimize.
	 * </p>
	 * @param resource the root of the resource subtree
	 * @param depth the depth of the subtree
	 * @return the <code>SyncInfo</code> for any out-of-sync resources
	 */
	public SyncInfo[] getSyncInfos(IResource resource, int depth);

	/**
	 * Return the immediate children of the given resource who are either out-of-sync 
	 * or contain out-of-sync resources.
	 * 
	 * @param resource the parent resource 
	 * @return the children of the resource that are either out-of-sync or are ancestors of
	 * out-of-sync resources contained in the set
	 */
	public IResource[] members(IResource resource);

	/**
	 * Return the sync info contained in this set that are contained
	 * in the given traversals.
	 * @param traversals the traversals
	 * @return the sync info contained in this set that are contained
	 * in the given traversals
	 */
	public SyncInfo[] getSyncInfos(ResourceTraversal[] traversals);

}