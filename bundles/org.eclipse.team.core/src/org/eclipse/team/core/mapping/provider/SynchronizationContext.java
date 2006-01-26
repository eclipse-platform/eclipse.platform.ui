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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.resources.mapping.ResourceTraversal;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.core.Policy;
import org.eclipse.team.internal.core.mapping.DiffCache;
import org.eclipse.team.internal.core.mapping.ResourceMappingScope;

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

	private IResourceMappingScope scope;
    private final int type;
    private final IResourceDiffTree diffTree;
    private DiffCache cache;

    /**
     * Create a synchronization context.
     * @param scope the input that defines the scope of the synchronization
     * @param type the type of synchronization (ONE_WAY or TWO_WAY)
     * @param diffTree the sync info tree that contains all out-of-sync resources
     */
    protected SynchronizationContext(IResourceMappingScope scope, int type, IResourceDiffTree diffTree) {
    	this.scope = scope;
		this.type = type;
		this.diffTree = diffTree;
    }
	
	/**
	 * {@inheritDoc}
	 */
	public IResourceMappingScope getScope() {
		return scope;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dispose() {
		if (cache != null) {
			cache.dispose();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public synchronized IDiffCache getCache() {
		if (cache == null) {
			cache = new DiffCache(this);
		}
		return cache;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IResourceDiffTree getDiffTree() {
		return diffTree;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void refresh(ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		ScopeGenerator scopeGenerator = getScopeGenerator();
		if (scopeGenerator == null) {
			// The scope generator is missing so just refresh everything
			refresh(scope.getTraversals(), IResource.NONE, Policy.subMonitorFor(monitor, 50));
		} else {
			ResourceTraversal[] traversals = scopeGenerator.refreshScope(getScope(), mappings, Policy.subMonitorFor(monitor, 50));
			if (traversals.length > 0)
				refresh(traversals, IResource.NONE, Policy.subMonitorFor(monitor, 50));
		}
		monitor.done();
	}

	private ScopeGenerator getScopeGenerator() {
		if (scope instanceof ResourceMappingScope) {
			ResourceMappingScope rms = (ResourceMappingScope) scope;
			rms.getGenerator();
		}
		return null;
	}

}
