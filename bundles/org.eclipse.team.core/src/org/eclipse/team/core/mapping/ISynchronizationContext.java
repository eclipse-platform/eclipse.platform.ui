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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.delta.*;
import org.eclipse.team.core.synchronize.*;
import org.eclipse.team.core.variants.IResourceVariant;

/**
 * Allows a model provider to build a view of their model that includes
 * synchronization information with a remote location (usually a repository).
 * <p>
 * The scope of the context is defined when the context is created. The creator
 * of the scope may affect changes on the scope which will result in property
 * change events from the scope and may result in sync-info change events from
 * the sync-info tree. Clients should note that it is possible that a change in
 * the scope will result in new out-of-sync resources being covered by the scope
 * but not result in a sync-info change event from the sync-info tree. This can
 * occur because the set may already have contained the out-of-sync resource
 * with the understanding that the client would have ignored it. Consequently,
 * clients should listen to both sources in order to guarantee that they update
 * any dependent state appropriately.
 * <p>
 * This interface is not intended to be implemented by clients. They should subclass
 * {@link SynchronizationContext} or one of its subclasses instead.
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is a guarantee neither that this API will
 * work nor that it will remain the same. Please do not use this API without
 * consulting with the Platform/Team team.
 * </p>
 * 
 * @see SynchronizationContext
 * @see MergeContext
 * 
 * @since 3.2
 */
public interface ISynchronizationContext {

	/**
	 * Synchronization type constant that indicates that
	 * context is a two-way synchronization.
	 */
	public final static String TWO_WAY = "two-way"; //$NON-NLS-1$
	
	/**
	 * Synchronization type constant that indicates that
	 * context is a three-way synchronization.
	 */
	public final static String THREE_WAY = "three-way"; //$NON-NLS-1$

	/**
	 * Return the input that defined the scope of this synchronization context.
	 * The input determines the set of resources to which the context applies.
	 * Changes in the input may result in changes to the sync-info available in
	 * the tree of this context.
	 * 
	 * @return the input that defined the scope of this synchronization context.
	 */
	IResourceMappingScope getScope();

	/**
	 * Return a tree that contains <code>SyncInfo</code> nodes for resources
	 * that are out-of-sync. The tree will contain sync-info for any out-of-sync
	 * resources that are within the scope of this context. The tree
	 * may include additional out-of-sync resources, which should be ignored by
	 * the client. Clients can test for inclusion using the method 
	 * {@link IResourceMappingScope#contains(IResource)}.
	 * 
	 * @return a tree that contains a <code>SyncInfo</code> node for any
	 *         resources that are out-of-sync.
	 */
	public ISyncInfoTree getSyncInfoTree();
	
	/**
	 * Return a tree that contains {@link ISyncDelta} nodes for resources
	 * that are out-of-sync. The tree will only contain deltas for out-of-sync
	 * resources that are within the scope of this context. The tree
	 * may include additional out-of-sync resources, which should be ignored by
	 * the client. Clients can test for inclusion using the method 
	 * {@link IResourceMappingScope#contains(IResource)}.
	 * <p>
	 * The {@link ISyncDeltaTree} and the {@link ITwoWayDelta} are a generic
	 * delta data structure. The tree returned from this method will have
	 * {@link IResourceVariant} objects as the before and after states
	 * of any {@link ITwoWayDelta} instances that are obtained directly
	 * or indirectly in the set.
	 * 
	 * EXPERIMENTAL: Do not use yet!!!
	 * 
	 * @return a tree that contains a <code>SyncInfo</code> node for any
	 *         resources that are out-of-sync.
	 *         
	 */
	public ISyncDeltaTree getSyncDeltaTree();

	/**
	 * Return the synchronization type. A type of <code>TWO_WAY</code>
	 * indicates that the synchronization information (i.e.
	 * <code>SyncInfo</code>) associated with the context will also be
	 * two-way (i.e. there is only a remote but no base involved in the
	 * comparison used to determine the synchronization state of resources. A
	 * type of <code>THREE_WAY</code> indicates that the synchronization
	 * information will be three-way and include the local, base (or ancestor)
	 * and remote.
	 * 
	 * @return the type of merge to take place
	 * 
	 * @see org.eclipse.team.core.synchronize.SyncInfo
	 */
	public String getType();
	
	/**
	 * Return the cache associated with this synchronization context.
	 * The cache is maintained for the lifetime of this context and is
	 * disposed when the the context is disposed. It can be used by
	 * clients to cache model state related to the context so that it can
	 * be maintained for the life of the operation to which the context
	 * applies.
	 * @return the cache associated with this synchronization context
	 */
     public ISynchronizationCache getCache();
    
	/**
	 * Dispose of the synchronization context and the cache of the context. This
	 * method should be invoked by clients when the context is no longer needed.
	 */
	public void dispose();
	
    /**
	 * Refresh the context in order to update the sync-info to include the
	 * latest remote state. any changes will be reported through the change
	 * listeners registered with the sync-info tree of this context. Changes to
	 * the set may be triggered by a call to this method or by a refresh
	 * triggered by some other source.
	 * 
	 * @see SyncInfoSet#addSyncSetChangedListener(ISyncInfoSetChangeListener)
	 * @see org.eclipse.team.core.synchronize.ISyncInfoTreeChangeEvent
	 * 
	 * @param traversals the resource traversals which indicate which resources
	 *            are to be refreshed
	 * @param flags additional refresh behavior. For instance, if
	 *            <code>RemoteResourceMappingContext.FILE_CONTENTS_REQUIRED</code>
	 *            is one of the flags, this indicates that the client will be
	 *            accessing the contents of the files covered by the traversals.
	 *            <code>NONE</code> should be used when no additional behavior
	 *            is required
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *            reporting is not desired
	 * @throws CoreException if the refresh fails. Reasons include:
	 *             <ul>
	 *             <li>The server could not be contacted for some reason (e.g.
	 *             the context in which the operation is being called must be
	 *             short running). The status code will be
	 *             SERVER_CONTACT_PROHIBITED. </li>
	 *             </ul>
	 */
    public void refresh(ResourceTraversal[] traversals, int flags, IProgressMonitor monitor) throws CoreException;

	/**
	 * Convenience method for return the set of deltas that
	 * are covered by the given traversals.
	 * @param traversals the traversals to search
	 * @return the deltas that are found in the given traversals
	 */
	public ISyncDelta[] getDeltas(ResourceTraversal[] traversals);

	/**
	 * Convenience method for returning the resource associated
	 * with a given delta.
	 * @param delta a delta
	 * @return the resource associated with that delta
	 */
	IResource getResource(ISyncDelta delta);

	/**
	 * Convenience method that returns whether the resource
	 * associated with the given delta is a file.
	 * @param delta the delta
	 * @return whether the resource
	 * associated with the given delta is a file
	 */
	boolean isFileDelta(ISyncDelta delta);
}
