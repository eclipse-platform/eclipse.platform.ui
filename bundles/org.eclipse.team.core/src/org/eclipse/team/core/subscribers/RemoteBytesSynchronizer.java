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
package org.eclipse.team.core.subscribers;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.core.Assert;

/**
 * A remote bytes sychronizer is a remote synchronizer that caches the 
 * remote sync bytes using the org.eclipse.core.resources.ISynchronizer.
 * It also has API that differentiates the case of no existing remote for
 * a local resource from that of the remote state never having been queried
 * for that local resource.
 */
public abstract class RemoteBytesSynchronizer extends RemoteSynchronizer {
	
	private static final byte[] NO_REMOTE = new byte[0];
	
	protected QualifiedName syncName;
	
	public RemoteBytesSynchronizer(QualifiedName name) {
		syncName = name;
		getSynchronizer().add(syncName);
	}
	
	/**
	 * Dispose of any cached sync bytes.
	 */
	public void dispose() {
		getSynchronizer().remove(getSyncName());
	}

	protected ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}

	protected QualifiedName getSyncName() {
		return syncName;
	}

	/**
	 * Return the remote sync bytes cached for the given local resource.
	 * A return value of <code>null</code> can mean either that the
	 * remote has never been fetched or that it doesn't exist. The method
	 * <code>isRemoteKnown(IResource)</code> should be used to differentiate
	 * these two cases.
	 */
	public byte[] getSyncBytes(IResource resource) throws TeamException {
		byte[] syncBytes = internalGetSyncBytes(resource);
		if (syncBytes != null && equals(syncBytes, NO_REMOTE)) {
			// If it is known that there is no remote, return null
			return null;
		}
		return syncBytes;
	}

	private byte[] internalGetSyncBytes(IResource resource) throws TeamException {
		try {
			return getSynchronizer().getSyncInfo(getSyncName(), resource);
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}
	
	/**
	 * Set the remote sync bytes for the given resource. The bytes should never be
	 * <code>null</code>. If it is known that the remote does not exist, 
	 * <code>setRemoteDoesNotExist(IResource)</code> should be invoked. If the sync
	 * bytes for the remote are stale and should be removed, <code>removeSyncBytes()</code>
	 * should be called.
	 * @param resource
	 * @param bytes
	 * @return <code>true</code> if the sync bytes changed
	 * @throws TeamException
	 */
	public boolean setSyncBytes(IResource resource, byte[] bytes) throws TeamException {
		Assert.isNotNull(bytes);
		byte[] oldBytes = internalGetSyncBytes(resource);
		if (oldBytes != null && equals(oldBytes, bytes)) return false;
		try {
			getSynchronizer().setSyncInfo(getSyncName(), resource, bytes);
			return true;
		} catch (CoreException e) {
			throw TeamException.asTeamException(e);
		}
	}

	/**
	 * Remove the remote bytes cached for the given local resource. After this
	 * operation <code>isRemoteKnown(resource)</code> will return <code>false</code> 
	 * and <code>getSyncBytes(resource)</code> will return <code>null</code> for the
	 * resource (and potentially it's children depending on the value of the depth parameter.
	 * @return <code>true</code> if there were bytes present which were removed
	 */
	public boolean removeSyncBytes(IResource resource, int depth) throws TeamException {
		if (resource.exists() || resource.isPhantom()) {
			try {
				if (depth != IResource.DEPTH_ZERO || internalGetSyncBytes(resource) != null) {
					getSynchronizer().flushSyncInfo(getSyncName(), resource, depth);
				}
				return true;
			} catch (CoreException e) {
				throw TeamException.asTeamException(e);
			}
		}
		return false;
	}
	
	/**
	 * Return true if the remote resources associated with the given local 
	 * resource has been fetched. This method is useful for those cases when
	 * there are no sync bytes for a remote resource and the client wants to
	 * know if this means that the remote does exist (i.e. this method returns
	 * <code>true</code>) or the remote has not been fetched (i.e. this method returns
	 * <code>false</code>).
	 */
	public boolean isRemoteKnown(IResource resource) throws TeamException {
		return internalGetSyncBytes(resource) != null;
	}
	
	/**
	 * This method should be invoked by a client to indicate that it is known that 
	 * there is no remote resource associated with the local resource. After this method
	 * is invoked, <code>isRemoteKnown(resource)</code> will return <code>true</code> and
	 * <code>getSyncBytes(resource)</code> will return <code>null</code>.
	 * @return <code>true</code> if this changes the remote sync bytes
	 */
	protected boolean setRemoteDoesNotExist(IResource resource) throws TeamException {
		return setSyncBytes(resource, NO_REMOTE);
	}

	private boolean equals(byte[] syncBytes, byte[] oldBytes) {
		if (syncBytes.length != oldBytes.length) return false;
		for (int i = 0; i < oldBytes.length; i++) {
			if (oldBytes[i] != syncBytes[i]) return false;
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.core.subscribers.RemoteSynchronizer#hasRemote(org.eclipse.core.resources.IResource)
	 */
	public boolean hasRemote(IResource resource) throws TeamException {
		return getSyncBytes(resource) != null;
	}

}