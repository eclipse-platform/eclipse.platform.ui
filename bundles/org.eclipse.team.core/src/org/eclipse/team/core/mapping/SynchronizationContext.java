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
package org.eclipse.team.core.mapping;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.core.synchronize.ISyncInfoTree;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.core.variants.IResourceVariant;
import org.eclipse.team.internal.core.TeamPlugin;
import org.eclipse.team.internal.core.delta.DeltaTree;
import org.eclipse.team.internal.core.mapping.SynchronizationCache;

/**
 * Abstract implementation of the {@link ISynchronizationContext} interface.
 * This class can be subclassed by clients.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see ISynchronizationContext
 * @since 3.2
 */
public abstract class SynchronizationContext implements ISynchronizationContext {

	private IResourceMappingScope input;
    private final String type;
    private final SyncInfoTree tree;
    private final DeltaTree deltaTree;
    private SynchronizationCache cache;

    /**
     * Create a synchronization context
     * @param input the input that defines the scope of the synchronization
     * @param type the type of synchronization (ONE_WAY or TWO_WAY)
     * @param tree the sync info tree that contains all out-of-sync resources
     */
    protected SynchronizationContext(IResourceMappingScope input, String type, SyncInfoTree tree, DeltaTree deltaTree) {
    	this.input = input;
		this.type = type;
		this.tree = tree;
		this.deltaTree = deltaTree;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getInput()
	 */
	public IResourceMappingScope getScope() {
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getSyncInfoTree()
	 */
	public ISyncInfoTree getSyncInfoTree() {
		return tree;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getType()
	 */
	public String getType() {
		return type;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#dispose()
	 */
	public void dispose() {
		if (cache != null) {
			cache.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getCache()
	 */
	public synchronized IDeltaCache getCache() {
		if (cache == null) {
			cache = new SynchronizationCache(this);
		}
		return cache;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getSyncDeltaTree()
	 */
	public IDeltaTree getDeltaTree() {
		return deltaTree;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getResource(org.eclipse.team.core.delta.ISyncDelta)
	 */
	public IResource getResource(IDelta delta) {
		IResource resource = null;
		if (delta instanceof IThreeWayDelta) {
			IThreeWayDelta twd = (IThreeWayDelta) delta;
			resource = internalGetResource(twd.getLocalChange());
			if (resource == null)
				resource = internalGetResource(twd.getRemoteChange());
		} else {
			resource = internalGetResource((ITwoWayDelta)delta);
		}
		return resource;	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getDeltas(org.eclipse.core.resources.mapping.ResourceTraversal[])
	 */
	public IDelta[] getDeltas(final ResourceTraversal[] traversals) {
		final Set result = new HashSet();
		try {
			getDeltaTree().accept(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new IDeltaVisitor() {
				public boolean visit(IDelta delta) throws CoreException {
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
		return (IDelta[]) result.toArray(new IDelta[result.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#isFileDelta(org.eclipse.team.core.delta.ISyncDelta)
	 */
	public boolean isFileDelta(IDelta delta) {
		IResource resource = getResource(delta);
		return resource != null && resource.getType() == IResource.FILE;
	}

	private IResource internalGetResource(ITwoWayDelta localChange) {
		if (localChange == null)
			return null;
		Object before = localChange.getBeforeState();
		IResourceVariant variant = null;
		if (before instanceof IResourceVariant) {
			variant = (IResourceVariant) before;
		}
		if (variant == null) {
			Object after = localChange.getAfterState();
			if (after instanceof IResourceVariant) {
				variant = (IResourceVariant) after;
			}
		}
		if (variant != null) {
			return internalGetResource(localChange.getPath(), variant.isContainer());
		}
		return null;
	}

	private IResource internalGetResource(IPath fullPath, boolean container) {
		if (container)
			return ResourcesPlugin.getWorkspace().getRoot().getFolder(fullPath);
		return ResourcesPlugin.getWorkspace().getRoot().getFile(fullPath);
	}
	
	

}
