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
package org.eclipse.team.core.synchronize;

import java.util.Iterator;

import org.eclipse.core.resources.IResource;

/**
 * A dynamic collection of {@link SyncInfo} objects that provides
 * change notification to registered listeners. Batching of change notifications
 * can be accomplished using the <code>beginInput/endInput</code> methods. 
 * <p>
 * This interface is not intended to be implemented by clients. Clients
 * that need an instance of a set can use either a {@link SyncInfoSet} or
 * {@link SyncInfoTree}
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see SyncInfoSet
 * @see SyncInfoTree
 * 
 * @since 3.2
 */
public interface ISyncInfoSet {

	/**
	 * Return an array of <code>SyncInfo</code> for all out-of-sync resources that are contained by the set.
	 * 
	 * @return an array of <code>SyncInfo</code>
	 */
	public SyncInfo[] getSyncInfos();

	/**
	 * Return all out-of-sync resources contained in this set. The default implementation
	 * uses <code>getSyncInfos()</code> to determine the resources contained in the set.
	 * Subclasses may override to optimize.
	 * 
	 * @return all out-of-sync resources contained in the set
	 */
	public IResource[] getResources();

	/**
	 * Return the <code>SyncInfo</code> for the given resource or <code>null</code>
	 * if the resource is not contained in the set.
	 * 
	 * @param resource the resource
	 * @return the <code>SyncInfo</code> for the resource or <code>null</code> if
	 * the resource is in-sync or doesn't have synchronization information in this set.
	 */
	public SyncInfo getSyncInfo(IResource resource);

	/**
	 * Return the number of out-of-sync resources contained in this set.
	 * 
	 * @return the size of the set.
	 */
	public int size();

	/**
	 * Return whether the set is empty.
	 * 
	 * @return <code>true</code> if the set is empty
	 */
	public boolean isEmpty();
	
    /**
     * Return an iterator over all <code>SyncInfo</code>
     * contained in this set.
     * @return an iterator over all <code>SyncInfo</code>
     * contained in this set.
     */
    public Iterator iterator();

	/**
	 * Registers the given listener for sync info set notifications. Has
	 * no effect if an identical listener is already registered.
	 * 
	 * @param listener listener to register
	 */
	public void addSyncSetChangedListener(ISyncInfoSetChangeListener listener);

	/**
	 * Removes the given listener from participant notifications. Has
	 * no effect if listener is not already registered.
	 * 
	 * @param listener listener to remove
	 */
	public void removeSyncSetChangedListener(ISyncInfoSetChangeListener listener);

}