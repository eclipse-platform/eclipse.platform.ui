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

import org.eclipse.team.core.mapping.*;
import org.eclipse.team.internal.core.mapping.DiffCache;

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
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getInput()
	 */
	public IResourceMappingScope getScope() {
		return input;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getType()
	 */
	public int getType() {
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
	public synchronized IDiffCache getCache() {
		if (cache == null) {
			cache = new DiffCache(this);
		}
		return cache;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.mapping.ISynchronizationContext#getSyncDeltaTree()
	 */
	public IResourceDiffTree getDiffTree() {
		return deltaTree;
	}

}
