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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * This cache uses session properties to hold the bytes representing the sync
 * info
 */
/*package*/ class SynchronizerSyncInfoCache extends LowLevelSyncInfoCache {

	public SynchronizerSyncInfoCache() {
		getWorkspaceSynchronizer().add(FOLDER_SYNC_KEY);
		getWorkspaceSynchronizer().add(RESOURCE_SYNC_KEY);
		getWorkspaceSynchronizer().add(DIRTY_COUNT);
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
	
	/**
	 * Flush any info cahced for the folder
	 */
	private void flushPhantomInfo(IContainer container) throws CVSException {
		try {
			if (container.exists() || container.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, container, IResource.DEPTH_ZERO);
			}
			if (container.exists() || container.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, container, IResource.DEPTH_ZERO);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*package*/ void flush(IProject project) throws CVSException {
		try {
			getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, project, IResource.DEPTH_INFINITE);
			getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, project, IResource.DEPTH_INFINITE);
			getWorkspaceSynchronizer().flushSyncInfo(DIRTY_COUNT, project, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Method flush.
	 * @param folder
	 */
	/*package*/ void flush(IFolder folder) throws CVSException {
		flushPhantomInfo(folder);
	}
	
	/**
	 * If not already cached, loads and caches the folder sync for the container.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 * @return the folder sync info for the folder, or null if none.
	 */
	/*package*/ FolderSyncInfo cacheFolderSync(IContainer container) throws CVSException {
		// nothing needs to be done since the synchronizer is persisted
		return getCachedFolderSync(container);
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
	 * If not already cached, loads and caches the resource sync for the children of the container.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 */
	/*package*/ void cacheResourceSyncForChildren(IContainer container) throws CVSException {
		// nothing needs to be done since the synchronizer is persisted
	}

	/**
	 * Returns the resource sync info for all children of the container.
	 * Container must exist and must not be the workspace root.
	 * The resource sync info for the children of the container MUST ALREADY BE CACHED.
	 *
	 * @param container the container
	 * @return a collection of the resource sync info's for all children
	 * @see #cacheResourceSyncForChildren
	 */
	/*package*/ byte[][] getCachedResourceSyncForChildren(IContainer container) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, container);
			if (bytes == null) return null;
			return getResourceSyncInfo(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
		
	/**
	 * Sets the resource sync info for the resource; if null, deletes it. Parent
	 * must exist and must not be the workspace root. The resource sync info for
	 * the children of the parent container MUST ALREADY BE CACHED.
	 *
	 * @param resource the resource
	 * @param info the new resource sync info
	 * @see #cacheResourceSyncForChildren
	 */
	/*package*/ void setCachedResourceSyncForChildren(IContainer container, byte[][] infos) throws CVSException {
		try {
			if (infos == null) {
				if (container.exists() || container.isPhantom()) {
					getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, container, IResource.DEPTH_ZERO);
				}
			} else {
				getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, container, getBytes(infos));
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.LowLevelSyncInfoCache#commitCache(org.eclipse.core.runtime.IProgressMonitor)
	 */
	IStatus commitCache(IProgressMonitor monitor) {
		// Nothing needs to be done since the synchronizer is persisted
		return STATUS_OK;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.LowLevelSyncInfoCache#getDirtyIndicator(org.eclipse.core.resources.IResource)
	 */
	String getDirtyIndicator(IResource resource) throws CVSException {
		if (resource.getType() == IResource.FILE) {
			// if someone is asking about a non-existant file, it's probably dirty
			return IS_DIRTY_INDICATOR;
		} else {
			int dirtyCount = getCachedDirtyCount((IContainer)resource);
			switch (dirtyCount) {
				case -1 :
					return null;
				case 0 :
					return NOT_DIRTY_INDICATOR;
				default :
					return IS_DIRTY_INDICATOR;
			}
		}
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.LowLevelSyncInfoCache#setDirtyIndicator(org.eclipse.core.resources.IResource, java.lang.String)
	 */
	void setDirtyIndicator(IResource resource, String indicator) throws CVSException {
		// The count is used as the indicator
	}
		
	/**
	 * Return the dirty count for the given folder. For existing folders, the
	 * dirty count may not have been calculated yet and this method will return
	 * null in that case. For phantom folders, the dirty count is calculated if
	 * it does not exist yet.
	 */
	/*package*/ int getCachedDirtyCount(IContainer container) throws CVSException {
		// get the count from the synchronizer
		int count = internalGetDirtyCount(container);
		if (count == -1) {
			count = calculateDirtyCountForPhantom(container);
			//setDirtyCount(parent, count);
		}
		return count;
	}

	/**
	 * Set the dirty count for the given container to the given count.
	 * 
	 * @param container
	 * @param count
	 * @throws CVSException
	 */
	/*package*/ void setCachedDirtyCount(IContainer container, int count) throws CVSException {
		try {
			getWorkspaceSynchronizer().setSyncInfo(DIRTY_COUNT, container, getBytes(count));
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Convert an int to a byte array
	 */
	private byte[] getBytes(int count) {
		byte[] result = new byte[4];
		result[0] = (byte)(count & 256);
		result[1] = (byte)(count<<8 & 256);
		result[1] = (byte)(count<<16 & 256);
		result[1] = (byte)(count<<24 & 256);
		return result;
	}

	/*
	 * Convert a byte array to an int
	 */
	private int intFromBytes(byte[] bytes) {
		return bytes[0] + (bytes[1]>>8) + (bytes[2]>>16) + (bytes[3]>>24);
	}
	
	private int internalGetDirtyCount(IContainer parent) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(DIRTY_COUNT, parent);
			if (bytes == null) return -1;
			return intFromBytes(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Calculate the dirty count for the given phantom folder, performing any
	 * necessary calculations on the childen as well
	 */
	private int calculateDirtyCountForPhantom(IContainer parent) throws CVSException {
		ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(parent);
		ICVSResource[] children = cvsFolder.members(ICVSFolder.MANAGED_MEMBERS | ICVSFolder.PHANTOM_MEMBERS);
		int count = 0;
		for (int i = 0; i < children.length; i++) {
			ICVSResource resource = children[i];
			if (resource.isFolder()) {
				int dc = getCachedDirtyCount((IContainer)resource.getIResource());
				if (dc > 0) count++;
			} else {
				// Any non-existant managed files are dirty (outgoing deletion)
				count++;
			}
		}
		return count;
	}
	
	/*package*/ void flushDirtyCache(IResource container) throws CVSException {
//		if (container.exists() || container.isPhantom()) {
//			try {
//				getWorkspaceSynchronizer().flushSyncInfo(DIRTY_COUNT, container, IResource.DEPTH_ZERO);
//			} catch (CoreException e) {
//				throw CVSException.wrapException(e);
//			}
//		}
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer#addDeletedChild(org.eclipse.core.resources.IContainer, org.eclipse.core.resources.IFile)
	 */
	protected boolean addDeletedChild(IContainer container, IFile file) throws CVSException {
//			try {
//				beginOperation(null);
//				int oldCount = internalGetDirtyCount(container);
//				if (oldCount == -1) {
//					// there is no cached count so wait until the first query
//					// or there was no deleted file
//					return false;
//				}
//				int newCount = calculateDirtyCountForPhantom(container);
//				// adjust the parent folder count if the newCount is 1;
//				return oldCount == 0 && newCount == 1;
//			} finally {
//				endOperation(null);
//			}
			return true;
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer#removeDeletedChild(org.eclipse.core.resources.IContainer, org.eclipse.core.resources.IFile)
	 */
	protected boolean removeDeletedChild(IContainer container, IFile file) throws CVSException {
//			try {
//				beginOperation(null);
//				int oldCount = internalGetDirtyCount(container);
//				if (oldCount == -1 || oldCount == 0) {
//					// there is no cached count so wait until the first query
//					// or there was no deleted file
//					return false;
//				}
//				int newCount = calculateDirtyCountForPhantom(container);
//				// adjust the parent folder count if the newCount is 0;
//				return newCount == 0;
//			} finally {
//				endOperation(null);
//			}
			return true;
	}
	
	/*package*/ boolean isSyncInfoLoaded(IContainer parent) throws CVSException {
		return true;
	}
}
