/*******************************************************************************
 * Copyright (c) 2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/
package org.eclipse.team.internal.ccvs.core.resources;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * Wraps the CVS EclipseSynchronizer with phantoms for folder deletions.
 */
public class EclipsePhantomSynchronizer extends EclipseSynchronizer {

	private static final QualifiedName FOLDER_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-sync"); //$NON-NLS-1$
	private static final QualifiedName RESOURCE_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync"); //$NON-NLS-1$
	
	private Set changedResources = new HashSet();
		
	EclipsePhantomSynchronizer() {
		// Add the sync keys to the workspace synchronizer which is used to handle folder deletions
		getWorkspaceSynchronizer().add(FOLDER_SYNC_KEY);
		getWorkspaceSynchronizer().add(RESOURCE_SYNC_KEY);
		getWorkspaceSynchronizer().add(DIRTY_COUNT);
	}
	
	/**
	 * Gets the folder sync info for the specified folder.
	 * 
	 * @param folder the folder
	 * @return the folder sync info associated with the folder, or null if none.
	 * @see #setFolderSync, #deleteFolderSync
	 */
	public FolderSyncInfo getFolderSync(IContainer container) throws CVSException{
		if (container.isPhantom()) {
			return getPhantomFolderSyncInfo(container);
		}
		return super.getFolderSync(container);
	}
	
	/**
	 * @see EclipseSynchronizer#setFolderSync(IContainer, FolderSyncInfo)
	 */
	public void setFolderSync(IContainer container, FolderSyncInfo info) throws CVSException {
		if (container.isPhantom()) {
			try {
				beginOperation(null);
				changedResources.add(container);
				try {
					getWorkspaceSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, container, getBytes(info));
				} catch (CoreException e) {
					throw CVSException.wrapException(e);
				}
			} finally {
				endOperation(null);
			}
		} else {
			super.setFolderSync(container, info);
		}
	}
	
	/**
	 * Deletes the folder sync for the specified folder and the resource sync
	 * for all of its children.  Does not recurse.
	 * 
	 * @param folder the folder
	 * @see #getFolderSync, #setFolderSync
	 */
	public void deleteFolderSync(IContainer container) throws CVSException {
		if (container.isPhantom()) {
			try {
				beginOperation(null);
				changedResources.add(container);
				flushPhantomInfo(container);
			} finally {
				endOperation(null);
			}
		} else {
			super.deleteFolderSync(container);
		}
	}
	
	/**
	 * Gets the resource sync info for the specified folder.
	 * 
	 * @param resource the resource
	 * @return the resource sync info associated with the resource, or null if none.
	 * @see #setResourceSync, #deleteResourceSync
	 */
	public ResourceSyncInfo getResourceSync(IResource resource) throws CVSException {
		IContainer parent = resource.getParent();
		if (parent != null && parent.isPhantom()) {
			Map map = getPhantomResourceSyncInfoMap(parent);
			return (ResourceSyncInfo)map.get(resource.getName());
		}
		return super.getResourceSync(resource);
	}
	
	/**
	 * @see EclipseSynchronizer#setResourceSync(IResource, ResourceSyncInfo)
	 */
	public void setResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		IContainer parent = resource.getParent();
		if (parent != null && parent.isPhantom()) {
			// Look for the sync info in the workspace synchronizer
			try {
				beginOperation(null);
				Map map = getPhantomResourceSyncInfoMap(parent);
				map.put(resource.getName(), info);
				getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, parent, getBytes(map));
				changedResources.add(resource);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			} finally {
				endOperation(null);
			}
		} else {
			super.setResourceSync(resource, info);
		}
	}
		
	/**
	 * Deletes the resource sync info for the specified resource, if it exists.
	 * 
	 * @param resource the resource
	 * @see #getResourceSync, #setResourceSync
	 */
	public void deleteResourceSync(IResource resource) throws CVSException {
		IContainer parent = resource.getParent();
		if (parent != null && parent.isPhantom()) {
			// Look for the sync info in the workspace synchronizer
			try {
				beginOperation(null);
				Map map = getPhantomResourceSyncInfoMap(parent);
				map.remove(resource.getName());
				getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, parent, getBytes(map));
				changedResources.add(resource);
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			} finally {
				endOperation(null);
			}
		} else {
			super.deleteResourceSync(resource);
		}
	}

	/**
	 * Flush the folder sync and resource sync for a phantom folder that has no childen
	 * @param folder an empty phantom folder
	 */
	private void flushEmptyFolder(IContainer folder) throws CVSException {
		deleteFolderSync(folder);
		deleteResourceSync(folder);
	}
	
	/**
	 * Returns the members of this folder which are either phantom folder
	 * or file deletions
	 *
	 * @param folder the container to list
	 * @return the array of members
	 */
	public IResource[] members(IContainer container) throws CVSException {
		if (container.isPhantom()) {
			Map map = getPhantomResourceSyncInfoMap(container);
			Set childResources = new HashSet();
			for (Iterator it = map.values().iterator(); it.hasNext();) {
				ResourceSyncInfo info = (ResourceSyncInfo) it.next();
				IPath path = new Path(info.getName());
				if(info.isDirectory()) {
					childResources.add(container.getFolder(path));
				} else {
					childResources.add(container.getFile(path));
				}
			}
			return (IResource[])childResources.toArray(new IResource[childResources.size()]);
		} else {
			return super.members(container);
		}
	}
	
	/**
	 * Notify the receiver that a folder has been created.
	 * Any existing phantom sync info will be moved
	 * 
	 * @param folder the folder that has been created
	 */
	public void folderCreated(IFolder folder) throws CVSException {
		try {
			// set the dirty count using what was cached in the phantom it
			beginOperation(null);
			FolderSyncInfo folderInfo = getPhantomFolderSyncInfo(folder);
			if (folderInfo != null) {
				Map map = getPhantomResourceSyncInfoMap(folder);
				if (folder.getFolder(SyncFileWriter.CVS_DIRNAME).exists()) {
					// There is already a CVS subdirectory which indicates that 
					// either the folder was recreated by an external tool or that
					// a folder with CVS information was copied from another location.
					// To know the difference, we need to compare the folder sync info.
					// If they are mapped to the same root and repository then just
					// purge the phantom info. Otherwise, keep the original sync info. 
					
					// flush the phantom info so we can get what is on disk.
					flushPhantomInfo(folder);
					
					// Get the new folder sync info
					FolderSyncInfo newFolderInfo = getFolderSync(folder);
					if (newFolderInfo.getRoot().equals(folderInfo.getRoot()) 
						&& newFolderInfo.getRepository().equals(folderInfo.getRepository())) {
							// The folder is the same so use what is on disk
							return;
					}
					
					// The folder is mapped to a different location.
					// Purge new resource sync before restoring from phantom
					ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
					ICVSResource[] children = cvsFolder.members(ICVSFolder.MANAGED_MEMBERS);
					for (int i = 0; i < children.length; i++) {
						ICVSResource resource = children[i];
						deleteResourceSync(resource.getIResource());
					}
				}
		
				// set the sync info using what was cached in the phantom
				setFolderSync(folder, folderInfo);
				for (Iterator it = map.values().iterator(); it.hasNext();) {
					ResourceSyncInfo info = (ResourceSyncInfo) it.next();
					IPath path = new Path(info.getName());
					IResource childResource;
					if(info.isDirectory()) {
						childResource = folder.getFolder(path);
					} else {
						childResource = folder.getFile(path);
					}
					setResourceSync(childResource, info);
				} 
			}	
		} finally {
			try {
				endOperation(null);
			} finally {
				flushPhantomInfo(folder);
			}
		}
	}
	
	/**
	 * Return the cached folder sync info for the given container or null 
	 * if there is none.
	 */
	private FolderSyncInfo getPhantomFolderSyncInfo(IContainer container) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(FOLDER_SYNC_KEY, container);
			if (bytes == null) return null;
			return getFolderSyncInfo(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	private Map getPhantomResourceSyncInfoMap(IContainer container) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, container);
			if (bytes == null) return new HashMap();
			return getResourceSyncInfoMap(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
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
			internalFlushModificationCache(container);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Return the Eclipse Workspace Synchronizer (from org.eclipse.core.resources)
	 */
	private ISynchronizer getWorkspaceSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}
	
	/**
	 * The folder is about to be deleted so move the folder's CVS information 
	 * to the workspace synchronizer so it will survive the deletion
	 */
	public void prepareForDeletion(IContainer container) throws CVSException {
		try {
			beginOperation(null);
			if (container.getType() == IResource.PROJECT) {
				getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, container, IResource.DEPTH_INFINITE);
				getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, container, IResource.DEPTH_INFINITE);
				getWorkspaceSynchronizer().flushSyncInfo(DIRTY_COUNT, container, IResource.DEPTH_INFINITE);
			} else {
				// Move the folder sync info into phantom space
				FolderSyncInfo info = getFolderSync(container);
				if (info == null) return;
				getWorkspaceSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, container, getBytes(info));
				getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, container, getBytes(getResourceSyncInfosForChildren(container)));
				changedResources.add(container);
				// Move the dirty count into phantom space
				Integer dirtyCount = getDirtyCount(container);
				if (dirtyCount != null) {
					internalSetDirtyCount(container, dirtyCount.intValue());
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Return a map of resource name to ResourceSyncInfo
	 * 
	 * This should only be used on folders that exist in the workspace
	 */
	private Map getResourceSyncInfosForChildren(IContainer parent) throws CVSException {
		ICVSFolder folder = CVSWorkspaceRoot.getCVSFolderFor(parent);
		ICVSResource[] files = folder.members(ICVSFolder.FILE_MEMBERS | ICVSFolder.FOLDER_MEMBERS | ICVSFolder.MANAGED_MEMBERS);
		Map result = new HashMap();
		for (int i = 0; i < files.length; i++) {
			ICVSResource resource = files[i];
			result.put(resource.getName(), resource.getSyncInfo());
		}
		return result;
	}
	
	/**
	 * Convert a byte array that was created using getBytes(FolderSyncInfo)
	 * into a FolderSyncInfo
	 */
	private static FolderSyncInfo getFolderSyncInfo(byte[] bytes) throws CVSException {
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
	 * Convert a FolderSyncInfo into a byte array that can be stored 
	 * in the workspace synchronizer
	 */
	private static byte[] getBytes(FolderSyncInfo info) throws CVSException {
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
	 * Convert a Map of ResourceSyncInfo into a byte array that can be stored 
	 * in the workspace synchronizer
	 */
	private static byte[] getBytes(Map infos) throws CVSException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(out);
		try {
			dos.writeInt(infos.size());
			Iterator iter = infos.values().iterator();
			while (iter.hasNext()) {
				ResourceSyncInfo info = (ResourceSyncInfo)iter.next();
				dos.writeUTF(info.getEntryLine());
			}
		} catch (IOException e) {
			throw CVSException.wrapException(e);
		}
		return out.toByteArray();
	}
	
	/**
	 * Convert a byte array that was created using getBytes(Map)
	 * into a Map of ResourceSyncInfo
	 */
	private static Map getResourceSyncInfoMap(byte[] bytes) throws CVSException {
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
	 * @see EclipseSynchronizer#broadcastResourceStateChanges(IResource[])
	 */
	void broadcastResourceStateChanges(IResource[] resources) {
		// Add the changedResources to the list of broadcasted resources
		if (changedResources.size() > 0) {
			ArrayList allResources = new ArrayList();
			allResources.addAll(Arrays.asList(resources));
			allResources.addAll(changedResources);
			resources = (IResource[]) allResources.toArray(new IResource[allResources.size()]);
			changedResources.clear();
		}
		super.broadcastResourceStateChanges(resources);
	}

	/*
	 * Return the dirty count for the given folder. For existing folders, the
	 * dirty count may not have been calculated yet and this method will return
	 * null in that case. For phantom folders, the dirty count is calculated if
	 * it does not exist yet.
	 */
	protected Integer getDirtyCount(IContainer parent) throws CVSException {
		if (parent.isPhantom()) {
			// get the count from the synchronizer
			int count = internalGetDirtyCount(parent);
			if (count == -1) {
				count = calculateDirtyCountForPhantom(parent);
				//setDirtyCount(parent, count);
			}
			return new Integer(count);
		} else {
			return super.getDirtyCount(parent);
		}
	}
	
	protected int internalGetDirtyCount(IContainer parent) throws CVSException {
		try {
			byte[] bytes = getWorkspaceSynchronizer().getSyncInfo(DIRTY_COUNT, parent);
			if (bytes == null) return -1;
			return intFromBytes(bytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	protected void setDirtyCount(IContainer container, int count) throws CVSException {
		if (container.isPhantom()) {
			internalSetDirtyCount(container, count);
		} else {
			super.setDirtyCount(container, count);
		}
	}

	protected void internalSetDirtyCount(IContainer container, int count) throws CVSException {
		try {
			beginOperation(null);
			getWorkspaceSynchronizer().setSyncInfo(DIRTY_COUNT, container, getBytes(count));
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
		   endOperation(null);
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
				Integer dc = getDirtyCount((IContainer)resource.getIResource());
				if (dc.intValue() > 0) count++;
			} else {
				// Any non-existant managed files are dirty (outgoing deletion)
				count++;
			}
		}
		return count;
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
	
	/*
	 * Flush all cached info for the container and it's ancestors
	 */
	protected void flushModificationCache(IContainer container) throws CVSException {
		internalFlushModificationCache(container);
		super.flushModificationCache(container);
	}
	
	private void internalFlushModificationCache(IContainer container) throws CVSException {
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
		if (container.isPhantom()) {
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
		} else {
			return super.addDeletedChild(container, file);
		}
	}

	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.EclipseSynchronizer#removeDeletedChild(org.eclipse.core.resources.IContainer, org.eclipse.core.resources.IFile)
	 */
	protected boolean removeDeletedChild(IContainer container, IFile file) throws CVSException {
		if (container.isPhantom()) {
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
		} else {
			return super.removeDeletedChild(container, file);
		}
	}
}
