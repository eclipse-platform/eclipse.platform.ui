/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
	 * Convert a FolderSyncInfo into a byte array that can be stored
	 * in the workspace synchronizer
	 */
	private byte[] getBytes(FolderSyncInfo info) throws CVSException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		try {
			dos.writeUTF(info.getRoot());
			dos.writeUTF(info.getRepository());
			CVSEntryLineTag tag = info.getTag();
			if (tag == null) {
				dos.writeUTF(""); //$NON-NLS-1$
			} else {
				dos.writeUTF(tag.toString());
			}
			dos.writeBoolean(info.getIsStatic());
			dos.close();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
		return out.toByteArray();
	}

	/**
	 * Convert a byte array that was created using getBytes(FolderSyncInfo)
	 * into a FolderSyncInfo
	 */
	private FolderSyncInfo getFolderSyncInfo(byte[] bytes) throws CVSException {
		ByteArrayInputStream in = new ByteArrayInputStream(bytes);
		DataInputStream dis = new DataInputStream(in);
		String root;
		String repository;
		CVSEntryLineTag tag;
		boolean isStatic;
		try {
			root = dis.readUTF();
			repository = dis.readUTF();
			String tagName = dis.readUTF();
			if (tagName.length() == 0) {
				tag = null;
			} else {
				tag = new CVSEntryLineTag(tagName);
			}
			isStatic = dis.readBoolean();
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
		return new FolderSyncInfo(repository, root, tag, isStatic);
	}
		
	/**
	 * Method getBytes converts an array of bytes into a single byte array
	 * @param infos
	 * @return byte[]
	 */
	private byte[] getBytes(byte[][] infos) throws CVSException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		SyncFileWriter.writeLines(out, infos);
		return out.toByteArray();
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
		try {
			getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, project, IResource.DEPTH_INFINITE);
			getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, project, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Method flush.
	 * @param folder
	 */
	/*package*/ void flush(IFolder folder) throws CVSException {
		try {
			if (folder.exists() || folder.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, folder, IResource.DEPTH_ZERO);
			}
			if (folder.exists() || folder.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, folder, IResource.DEPTH_ZERO);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
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
	/*package*/ FolderSyncInfo getCachedFolderSync(IContainer container) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(FOLDER_SYNC_KEY, container);
			if (bytes == null) return null;
			return getFolderSyncInfo(bytes);
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
	/*package*/ void setCachedFolderSync(IContainer container, FolderSyncInfo info) throws CVSException {
		try {
			if (info == null) {
				if (container.exists() || container.isPhantom()) {
					getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, container, IResource.DEPTH_ZERO);
				}
			} else {
				getWorkspaceSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, container, getBytes(info));
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#getCachedSyncBytes(org.eclipse.core.resources.IResource)
	 */
	/*package*/ byte[] getCachedSyncBytes(IResource resource) throws CVSException {
		try {
			return getWorkspaceSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, resource);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setCachedSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	/*package*/ void setCachedSyncBytes(IResource resource, byte[] syncBytes) throws CVSException {
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
				if (oldBytes == null || !Util.equals(syncBytes, oldBytes))
					getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, resource, syncBytes);
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
}