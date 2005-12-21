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
package org.eclipse.team.core.mapping;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.core.diff.*;

/**
 * A resource diff tree provides access to a tree of {@link IDiffNode} instances
 * that either contain {@link org.eclipse.team.core.mapping.provider.IResourceDiff}
 * nodes or {@link IThreeWayDiff} nodes that contain
 * {@link org.eclipse.team.core.mapping.provider.IResourceDiff} nodes as the local and
 * remote changes. For efficiency reasons, the tree only provides diffs for
 * resources that have changes. Resources that do not contain a change but are
 * returned from the tree will contain children in the set.
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public interface IResourceDiffTree extends IDiffTree {

	/**
	 * Return the diff for the given resource. This method is a convenience
	 * method that uses the path of the resource to access the diff.
	 * 
	 * @param resource a resource
	 * @return the diff associated with the resource or <code>null</code> if
	 *         the resource does not have any changes.
	 */
	IDiffNode getDiff(IResource resource);

	/**
	 * Return the resource associated with the given diff. This method will only
	 * return meaningful results for diffs which were obtained from this tree.
	 * 
	 * @param delta a delta
	 * @return the resource associated with the given diff
	 */
	IResource getResource(IDiffNode diff);

	/**
	 * Visit all diffs in this tree that are covered by the given traversals.
	 * 
	 * @param visitor a diff visitor
	 * @param traversals the set of traversals whose diffs are to be visited
	 * @throws CoreException
	 */
	void accept(IDiffVisitor visitor, ResourceTraversal[] traversals)
			throws CoreException;

	/**
	 * Return all the diffs in the tree that are contained in the given
	 * traversals.
	 * 
	 * @param traversals the traversals
	 * @return all the diffs in the tree that are contained in the given
	 *         traversals
	 */
	IDiffNode[] getDiffs(ResourceTraversal[] traversals);

	/**
	 * Return all the diffs in the tree that are found for
	 * the given resource when traversed to the given depth.
	 * 
	 * @param resource the resource
	 * @param depth the depth
	 * @return all the diffs in the tree that are found for
	 * the given resource when traversed to the given depth
	 */
	IDiffNode[] getDiffs(IResource resource, int depth);
	
	/**
	 * Return the members of the given resource that either have diffs in this
	 * tree of contain descendants that have diffs in this tree.
	 * 
	 * @param resource a resource
	 * @return the members of the given resource that either have diffs in this
	 *         tree of contain descendants that have diffs in this tree
	 */
	IResource[] members(IResource resource);

	/**
	 * Return all resources that contain diffs in this
	 * diff tree.
	 * @return all resources that contain diffs in this
	 * diff tree
	 */
	IResource[] getAffectedResources();

}
