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
package org.eclipse.team.internal.ccvs.core.syncinfo;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSRemoteResource;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.RemoteResource;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * A remote resource sychronizer caches the remote sync bytes that can be 
 * used to create remote handles
 */
public class RemoteSynchronizer extends ResourceSynchronizer {
	
	private static final byte[] NO_REMOTE = new byte[0];
	
	public static final String SYNC_KEY_QUALIFIER = "org.eclipse.team.cvs"; //$NON-NLS-1$
	protected QualifiedName syncName;
	protected Set changedResources = new HashSet();
	
	public RemoteSynchronizer(String id) {
		syncName = new QualifiedName(SYNC_KEY_QUALIFIER, id);
		getSynchronizer().add(syncName);
	}
	
	/**
	 * Dispose of any cached remote sync info.
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
	public byte[] getSyncBytes(IResource resource) throws CVSException {
		byte[] syncBytes = internalGetSyncBytes(resource);
		if (syncBytes != null && Util.equals(syncBytes, NO_REMOTE)) {
			// If it is known that there is no remote, return null
			return null;
		}
		return syncBytes;
	}

	private byte[] internalGetSyncBytes(IResource resource) throws CVSException {
		try {
			return getSynchronizer().getSyncInfo(getSyncName(), resource);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	public void setSyncBytes(IResource resource, byte[] bytes) throws CVSException {
		byte[] oldBytes = internalGetSyncBytes(resource);
		if (oldBytes != null && Util.equals(oldBytes, bytes)) return;
		try {
			if (!parentHasSyncBytes(resource) && !Util.equals(bytes, NO_REMOTE)) {
				// Log a warning if there is no sync bytes available for the resource's
				// parent but there is valid sync bytes for the child
				CVSProviderPlugin.log(new CVSException(Policy.bind("ResourceSynchronizer.missingParentBytesOnSet", getSyncName().toString(), resource.getFullPath().toString()))); //$NON-NLS-1$
			}
			getSynchronizer().setSyncInfo(getSyncName(), resource, bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		changedResources.add(resource);
	}

	/**
	 * Indicates whether the parent of the given local resource has sync bytes for its
	 * corresponding remote resource. The parent bytes of a remote resource are required
	 * (by CVS) to create a handle to the remote resource.
	 */
	protected boolean parentHasSyncBytes(IResource resource) throws CVSException {
		if (resource.getType() == IResource.PROJECT) return true;
		return (getSyncBytes(resource.getParent()) != null);
	}

	/**
	 * Remove the remote bytes cached for the given local resource. After this
	 * operation <code>isRemoteKnown(resource)</code> will return <code>false</code> 
	 * and <code>getSyncBytes(resource)</code> will return <code>null</code> for the
	 * resource (and potentially it's children depending on the value of the depth parameter.
	 */
	public void removeSyncBytes(IResource resource, int depth, boolean silent) throws CVSException {
		if (resource.exists() || resource.isPhantom()) {
			try {
				getSynchronizer().flushSyncInfo(getSyncName(), resource, depth);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
			if(silent == false) {
				changedResources.add(resource);
			}
		}
	}
	
	/**
	 * Return true if the remote resources associated with the given local 
	 * resource has been fetched. This method is useful for those cases when
	 * there are no sync bytes for a remote resource and the client wants to
	 * know if this means that the remote does exist (i.e. this method returns
	 * <code>true</code>) or the remote has not been fetched (i.e. this method returns
	 * <code>false</code>).
	 */
	public boolean isRemoteKnown(IResource resource) throws CVSException {
		return internalGetSyncBytes(resource) != null;
	}
	
	/**
	 * This method should be invoked by a client to indicate that it is known that 
	 * there is no remote resource associated with the local resource. After this method
	 * is invoked, <code>isRemoteKnown(resource)</code> will return <code>true</code> and
	 * <code>getSyncBytes(resource)</code> will return <code>null</code>.
	 */
	protected void setRemoteDoesNotExist(IResource resource) throws CVSException {
		setSyncBytes(resource, NO_REMOTE);
	}
	
	/**
	 * Return the sync bytes associated with the remote resource. A return
	 * value of <code>null</code> indicates that the remote resource does not exist.
	 */
	protected byte[] getRemoteSyncBytes(IResource local, ICVSRemoteResource remote) throws CVSException {
		if (remote != null) {
			return ((RemoteResource)remote).getSyncBytes();
		} else {
			return null;
		}
	}
}