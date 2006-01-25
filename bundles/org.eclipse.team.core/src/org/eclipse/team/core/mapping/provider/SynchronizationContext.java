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

import java.util.ArrayList;
import java.util.List;

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

	private IResourceMappingScope input;
    private final int type;
    private final IResourceDiffTree deltaTree;
    private DiffCache cache;

    /**
     * Create a synchronization context
     * @param input the input that defines the scope of the synchronization
     * @param type the type of synchronization (ONE_WAY or TWO_WAY)
     * @param tree the sync info tree that contains all out-of-sync resources
     */
    protected SynchronizationContext(IResourceMappingScope input, int type, IResourceDiffTree deltaTree) {
    	this.input = input;
		this.type = type;
		this.deltaTree = deltaTree;
    }
	
	/**
	 * {@inheritDoc}
	 */
	public IResourceMappingScope getScope() {
		return input;
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
		return deltaTree;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void refresh(ResourceMapping[] mappings, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(null, 100);
		ScopeGenerator scopeGenerator = getScopeGenerator();
		if (scopeGenerator != null)
			scopeGenerator.refreshScope(getScope(), mappings, Policy.subMonitorFor(monitor, 50));
		List traversals = new ArrayList();
		for (int i = 0; i < mappings.length; i++) {
			ResourceMapping mapping = mappings[i];
			ResourceTraversal[] mappingTraversals = input.getTraversals(mapping);
			for (int j = 0; j < mappingTraversals.length; j++) {
				ResourceTraversal traversal = mappingTraversals[j];
				traversals.add(traversal);
			}
		}
		if (!traversals.isEmpty())
			refresh((ResourceTraversal[]) traversals.toArray(new ResourceTraversal[traversals.size()]), IResource.NONE, Policy.subMonitorFor(monitor, 50));
		monitor.done();
	}

	private ScopeGenerator getScopeGenerator() {
		if (input instanceof ResourceMappingScope) {
			ResourceMappingScope rms = (ResourceMappingScope) input;
			rms.getGenerator();
		}
		return null;
	}

}
