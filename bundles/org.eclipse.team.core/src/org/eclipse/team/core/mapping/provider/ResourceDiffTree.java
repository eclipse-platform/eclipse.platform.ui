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
package org.eclipse.team.core.mapping.provider;

import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.*;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IResourceDiff;
import org.eclipse.team.core.mapping.IResourceDiffTree;
import org.eclipse.team.internal.core.TeamPlugin;

/**
 * Implementation of {@link IResourceDiffTree}.
 * <p>
 * This class is not intended to be subclassed by clients
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @since 3.2
 */
public class ResourceDiffTree extends DiffTree implements IResourceDiffTree {

	/**
	 * Get the resource for the diff node that was obtained from an
	 * {@link IResourceDiffTree}.
	 * @param node the diff node.
	 * @return the resource for the diff node
	 */
	public static IResource getResourceFor(IDiffNode node) {
		if (node instanceof IResourceDiff) {
			IResourceDiff rd = (IResourceDiff) node;
			return rd.getResource();
		}
		if (node instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) node;
			IDiffNode child = twd.getLocalChange();
			if (child != null)
				return getResourceFor(child);
			child = twd.getRemoteChange();
			if (child != null)
				return getResourceFor(child);
		}
		Assert.isLegal(false);
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiffTree#getDiff(org.eclipse.core.resources.IResource)
	 */
	public IDiffNode getDiff(IResource resource) {
		return getDiff(resource.getFullPath());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiffTree#getResource(org.eclipse.team.core.diff.IDiffNode)
	 */
	public IResource getResource(IDiffNode diff) {
		if (diff instanceof IThreeWayDiff) {
			IThreeWayDiff twd = (IThreeWayDiff) diff;
			IResourceDiff localChange = ((IResourceDiff)twd.getLocalChange());
			if (localChange != null)
				return localChange.getResource();
			return ((IResourceDiff)twd.getRemoteChange()).getResource();
		} else {
			return ((IResourceDiff)diff).getResource();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiffTree#accept(org.eclipse.team.core.diff.IDiffVisitor, org.eclipse.core.resources.mapping.ResourceTraversal[])
	 */
	public void accept(IDiffVisitor visitor, ResourceTraversal[] traversals) throws CoreException {
		IDiffNode[] diffs = getDiffs(traversals);
		for (int i = 0; i < diffs.length; i++) {
			IDiffNode node = diffs[i];
			visitor.visit(node);
		}
	}

	public IDiffNode[] getDiffs(final ResourceTraversal[] traversals) {
		final Set result = new HashSet();
		try {
			accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDiffVisitor() {
				public boolean visit(IDiffNode delta) throws CoreException {
					for (int i = 0; i < traversals.length; i++) {
						ResourceTraversal traversal = traversals[i];
						if (traversal.contains(getResource(delta))) {
							result.add(delta);
						}
					}
					return true;
				}
			}, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			TeamPlugin.log(e);
		}
		return (IDiffNode[]) result.toArray(new IDiffNode[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceDiffTree#getDiffs(org.eclipse.core.resources.IResource, int)
	 */
	public IDiffNode[] getDiffs(IResource resource, int depth) {
		return getDiffs(new ResourceTraversal[] { new ResourceTraversal(new IResource[] { resource }, depth, IResource.NONE) } );
	}

	private IResource internalGetResource(IPath fullPath, boolean container) {
		if (container)
			return ResourcesPlugin.getWorkspace().getRoot().getFolder(fullPath);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.diff.IResourceDiffTree#members(org.eclipse.core.resources.IResource)
	 */
	public IResource[] members(IResource resource) {
		List result = new ArrayList();
		IPath[] paths = getChildren(resource.getFullPath());
		for (int i = 0; i < paths.length; i++) {
			IPath path = paths[i];
			IDiffNode node = getDiff(path);
			if (node == null) {
				result.add(internalGetResource(path, true));
			} else {
				result.add(getResource(node));
			}
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.IResourceDiffTree#getAffectedResources()
	 */
	public IResource[] getAffectedResources() {
		List result = new ArrayList();
		IDiffNode[] nodes = getDiffs();
		for (int i = 0; i < nodes.length; i++) {
			IResourceDiff node = (IResourceDiff)nodes[i];
			result.add(node.getResource());
		}
		return (IResource[]) result.toArray(new IResource[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.mapping.provider.DiffTree#add(org.eclipse.team.core.diff.IDiffNode)
	 */
	public void add(IDiffNode delta) {
		Assert.isTrue(delta instanceof IResourceDiff || delta instanceof IThreeWayDiff);
		super.add(delta);
	}
	
	/**
	 * Remove the diff associated with the given resource from
	 * the tree.
	 * @param resource the resource
	 */
	public void remove(IResource resource) {
		remove(resource.getFullPath());
	}	
}
