/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.core.subscribers.caches;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.core.TeamException;

/**
 * The purpose of a <code>ResourceVariantTree</code> is to support the caching of
 * the synchronization bytes for the resource variants that represent
 * a resource line-up of interest such as a version, baseline or branch. The
 * cache stores bytes in order to minimize the memory footprint of the tree. It is the
 * reponsibility of the client of this API to cache enough bytes to meaningfully identify
 * a resource variant (and possibly create an {@link IResourceVariant} handle from them).
 * <p>
 * The bytes for
 * a resource variant are accessed using the local handle that corresponds to the 
 * resource variant (using the <code>getSyncInfo</code> method). 
 * The potential children of a resource variant are also accessed
 * by using the local handle that corresponds to the resource variant 
 * (using the <code>members</code> method).
 * TODO: Does the isRemoteKnown/setRemoteDoesNotExist make sense?
 */
public abstract class ResourceVariantTree {

	/**
	 * Dispose of any cached sync bytes when this cache is no longer needed.
	 */
	public abstract void dispose();
	
	/**
	 * Return the bytes for the variant corresponding the given local resource.
	 * A return value of <code>null</code> can mean either that the
	 * variant has never been fetched or that it doesn't exist. The method
	 * <code>isVariantKnown(IResource)</code> should be used to differentiate
	 * these two cases.
	 * @param resource the local resource
	 * @return the bytes that represent the resource's variant
	 * @throws TeamException
	 */
	public abstract byte[] getBytes(IResource resource) throws TeamException;
	
	/**
	 * Set the bytes for the variant corresponding the given local resource. 
	 * The bytes should never be
	 * <code>null</code>. If it is known that the remote does not exist, 
	 * <code>setVariantDoesNotExist(IResource)</code> should be used instead. If the sync
	 * bytes for the remote are stale and should be removed, <code>removeBytes()</code>
	 * should be called.
	 * @param resource the local resource
	 * @param bytes the bytes that represent the resource's variant
	 * @return <code>true</code> if the bytes changed
	 * @throws TeamException
	 */
	public abstract boolean setBytes(IResource resource, byte[] bytes) throws TeamException;
	
	/**
	 * Remove the bytes from the tree for the resource variants corresponding to the 
	 * local resources that are descendants of the giben locla resource to the given depth.
	 * After the bytes are removed, the operation <code>isVariantKnown(resource)</code> 
	 * will return <code>false</code> 
	 * and <code>getBytes(resource)</code> will return <code>null</code> for the
	 * affected resources.
	 * @param resource the local resource
	 * @parem depth the depth of the operation (one of <code>IResource.DEPTH_ZERO</code>,
	 * <code>IResource.DEPTH_ONE</code>, or <code>IResource.DEPTH_INFINITE</code>)
	 * @return <code>true</code> if there were bytes present which were removed
	 * @throws TeamException
	 */
	public abstract boolean removeBytes(IResource resource, int depth) throws TeamException;
	
	/**
	 * Return <code>true</code> if the variant associated with the given local 
	 * resource has been cached. This method is useful for those cases when
	 * there are no bytes for a resource variant and the client wants to
	 * know if this means that the remote does exist (i.e. this method returns
	 * <code>true</code>) or the remote has not been fetched (i.e. this method returns
	 * <code>false</code>).
	 * @param resource the local resource
	 * @return <code>true</code> if the variant associated with the given local 
	 * resource has been cached.
	 * @throws TeamException
	 */
	public abstract boolean isVariantKnown(IResource resource) throws TeamException;
	
	/**
	 * This method should be invoked by a client to indicate that it is known that 
	 * there is no variant associated with the local resource. After this method
	 * is invoked, <code>isVariantKnown(resource)</code> will return <code>true</code> and
	 * <code>getBytes(resource)</code> will return <code>null</code>.
	 * @param resource the local resource
	 * @return <code>true</code> if this changes the bytes for the variant
	 */
	public abstract boolean setVariantDoesNotExist(IResource resource) throws TeamException;
	
	/**
	 * Return the children of the given resource that have resource variants in this tree.
	 * @param resource the parent resource
	 * @return the members who have resource variants in this tree.
	 */
	public abstract IResource[] members(IResource resource) throws TeamException;
	
	/**
	 * Helper method to compare two byte arrays for equality
	 * @param syncBytes1 the first byte array or <code>null</code>
	 * @param syncBytes2 the second byte array or <code>null</code>
	 * @return whetehr the two arrays are equal (i.e. same content)
	 */
	protected boolean equals(byte[] syncBytes1, byte[] syncBytes2) {
		if (syncBytes1 == null) {
			return syncBytes2 == null;
		} else if (syncBytes2 == null) {
			return false;
		}
		if (syncBytes1.length != syncBytes2.length) return false;
		for (int i = 0; i < syncBytes1.length; i++) {
			if (syncBytes1[i] != syncBytes2[i]) return false;
		}
		return true;
	}
}
