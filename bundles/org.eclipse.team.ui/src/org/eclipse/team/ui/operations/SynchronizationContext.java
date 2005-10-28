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
package org.eclipse.team.ui.operations;

import org.eclipse.team.core.synchronize.ISyncInfoTree;
import org.eclipse.team.core.synchronize.SyncInfoTree;
import org.eclipse.team.internal.ui.mapping.ISynchronizationCache;
import org.eclipse.team.internal.ui.mapping.SynchronizationCache;
import org.eclipse.team.ui.mapping.IResourceMappingScope;
import org.eclipse.team.ui.mapping.ISynchronizationContext;

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
    private SynchronizationCache cache;

    /**
     * Create a synchronization context
     * @param input the input that defines the scope of the synchronization
     * @param type the type of synchronization (ONE_WAY or TWO_WAY)
     * @param tree the sync info tree that contains all out-of-sync resources
     */
    protected SynchronizationContext(IResourceMappingScope input, String type, SyncInfoTree tree) {
    	this.input = input;
		this.type = type;
		this.tree = tree;
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
	public synchronized ISynchronizationCache getCache() {
		if (cache == null) {
			cache = new SynchronizationCache();
		}
		return cache;
	}

}
