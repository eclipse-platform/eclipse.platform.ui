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
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * This cache uses session properties to hold the bytes representing the sync
 * info
 */
/*package*/ class SynchronizerSyncInfoCache extends SyncInfoCache {

	public SynchronizerSyncInfoCache() {
		getWorkspaceSynchronizer().add(FOLDER_SYNC_KEY);
		getWorkspaceSynchronizer().add(RESOURCE_SYNC_KEY);
	}
	/**
	 * Return the Eclipse Workspace Synchronizer (from org.eclipse.core.resources)
	 */
	private ISynchronizer getWorkspaceSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}
	
	/**
	 * Convert a byte array that was created using getBytes(Map)
	 * into a Map of ResourceSyncInfo
	 */
	private byte[][] getResourceSyncInfo(byte[] bytes) throws CVSException {
		byte[][] infos = SyncFileWriter.readLines(new ByteArrayInputStream(bytes));
		// check to make sure the info is not stored in the old format 
		if (infos.length != 0) {
			byte[] firstLine = infos[0];
			if (firstLine.length != 0 && (firstLine[0] != (byte)'/' && firstLine[0] != (byte)'D')) {
				Map oldInfos = getResourceSyncInfoMap(bytes);
				infos = new byte[oldInfos.size()][];
				int i = 0;
				for (Iterator iter = oldInfos.values().iterator(); iter.hasNext();) {
					ResourceSyncInfo element = (ResourceSyncInfo) iter.next();
					infos[i++] = element.getBytes();
				}
				// We can't convert the info to the new format because the caller
				// may either not be in a workspace runnable or the resource tree
				// may be closed for modification
			}
		}
		return infos;
	}

	/**
	 * ResourceSyncInfo used to be stored as a Map of ResourceSyncInfo.
	 * We need to be able to retrieve that info the way it was and
	 * convert it to the new way. 
	 * 
	 * Convert a byte array that was created using
	 * getBytes(Map) into a Map of ResourceSyncInfo
	 */
	private Map getResourceSyncInfoMap(byte[] bytes) throws CVSException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(in);
		Map result = new HashMap();
		try {
			int size = dis.readInt();
			for (int i = 0; i < size; i++) {
				ResourceSyncInfo info = new ResourceSyncInfo(dis.readUTF(), null, null);
				result.put(info.getName(), info);
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
		return result;
	}
	
	/*package*/ void flush(IProject project) throws CVSException {
		purgeCache(project, true);
	}
	
	/**
	 * Method flush.
	 * @param folder
	 */
	/*package*/ void flush(IFolder folder) throws CVSException {
		purgeCache(folder, false);
	}
	
	/**
	 * Returns the folder sync info for the container; null if none.
	 * Folder must exist and must not be the workspace root.
	 * The folder sync info for the container MUST ALREADY BE CACHED.
	 *
	 * @param container the container
	 * @return the folder sync info for the folder, or null if none.
	 * @see #cacheFolderSync
	 */
	FolderSyncInfo getCachedFolderSync(IContainer container) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(FOLDER_SYNC_KEY, container);
			if (bytes == null) return null;
			return FolderSyncInfo.getFolderSyncInfo(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Sets the folder sync info for the container; if null, deletes it.
	 * Folder must exist and must not be the workspace root.
	 * The folder sync info for the container need not have previously been
	 * cached.
	 *
	 * @param container the container
	 * @param info the new folder sync info
	 */
	void setCachedFolderSync(IContainer container, FolderSyncInfo info, boolean canModifyWorkspace) throws CVSException {
		try {
			if (info == null) {
				if (container.exists() || container.isPhantom()) {
					getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, container, IResource.DEPTH_ZERO);
				}
			} else {
				getWorkspaceSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, container, info.getBytes());
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#getCachedSyncBytes(org.eclipse.core.resources.IResource)
	 */
	byte[] getCachedSyncBytes(IResource resource) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, resource);
			if (bytes != null && resource.getType() == IResource.FILE) {
				if (ResourceSyncInfo.isAddition(bytes)) {
					// The local file has been deleted but was an addition
					// Therefore, ignoe the sync bytes
					bytes = null;
				} else if (!ResourceSyncInfo.isDeletion(bytes)) {
					// Ensure the bytes indicate an outgoing deletion
					bytes = ResourceSyncInfo.convertToDeletion(bytes);
				}
			}
			return bytes;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setCachedSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	void setCachedSyncBytes(IResource resource, byte[] syncBytes, boolean canModifyWorkspace) throws CVSException {
		byte[] oldBytes = getCachedSyncBytes(resource);
		try {
			if (syncBytes == null) {
				if (oldBytes != null && (resource.exists() || resource.isPhantom())) {
					getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, resource, IResource.DEPTH_ZERO);
				}
			} else {
				// ensure that the sync info is not already set to the same thing.
				// We do this to avoid causing a resource delta when the sync info is 
				// initially loaded (i.e. the synchronizer has it and so does the Entries file
				if (oldBytes == null || !Util.equals(syncBytes, oldBytes)) {
					getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, resource, syncBytes);
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#getDirtyIndicator(org.eclipse.core.resources.IResource)
	 */
	String getDirtyIndicator(IResource resource) throws CVSException {		
		if (resource.getType() == IResource.FILE) {
			// a phantom file is dirty if it was managed before it was deleted			 
			return getCachedSyncBytes(resource) != null ? 
							IS_DIRTY_INDICATOR : 
							NOT_DIRTY_INDICATOR;
		} else {
			return calculateDirtyCountForPhantomFolder((IContainer)resource);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setDirtyIndicator(org.eclipse.core.resources.IResource, java.lang.String)
	 */
	void setDirtyIndicator(IResource resource, String indicator) throws CVSException {
		// We don't cache the dirty count for folders because it would cause
		// resource delta's in the decorator thread and possible deadlock.
	}
		
	/*package*/ void flushDirtyCache(IResource container) throws CVSException {
	}
	
	/*package*/ boolean isSyncInfoLoaded(IContainer parent) throws CVSException {
		return true;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#isResourceSyncInfoCached(org.eclipse.core.resources.IContainer)
	 */
	boolean isResourceSyncInfoCached(IContainer container) throws CVSException {
		// the sync info is always cahced when using the synchronizer
		return true;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setResourceSyncInfoCached(org.eclipse.core.resources.IContainer)
	 */
	void setResourceSyncInfoCached(IContainer container) throws CVSException {
		// do nothing
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#isFolderSyncInfoCached(org.eclipse.core.resources.IContainer)
	 */
	boolean isFolderSyncInfoCached(IContainer container) throws CVSException {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#isDirtyCacheFlushed(org.eclipse.core.resources.IContainer)
	 */
	boolean isDirtyCacheFlushed(IContainer resource) throws CVSException {
		return false;
	}
	
	/*
	 * Calculate the dirty count for the given phantom folder, performing any
	 * necessary calculations on the childen as well
	 */
	private String calculateDirtyCountForPhantomFolder(IContainer parent) throws CVSException {
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(parent);
		if(getCachedFolderSync(parent) == null) {
			return NOT_DIRTY_INDICATOR;
		}
		
		String indicator = NOT_DIRTY_INDICATOR;
		ICVSResource[] children = cvsFolder.members(ICVSFolder.MANAGED_MEMBERS | ICVSFolder.PHANTOM_MEMBERS);
		for (int i = 0; i < children.length; i++) {
			ICVSResource resource = children[i];
			// keep looking into phantom folders until a managed phantom file 
			// is found.
			if (resource.isFolder()) {
				indicator = calculateDirtyCountForPhantomFolder((IContainer)resource.getIResource());
			} else {
				// Any non-existant managed files are dirty (outgoing deletion)
				indicator = IS_DIRTY_INDICATOR;
				break;
			}
		}
		return indicator;
	}
	
	/**
	 * @param root
	 * @param deep
	 */
	public void purgeCache(IContainer root, boolean deep) throws CVSException {
		int depth = deep ? IResource.DEPTH_INFINITE : IResource.DEPTH_ZERO;
		try {
			if (root.exists() || root.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, root, depth);
			}
			if (root.exists() || root.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, root, depth);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		
	}
}
