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
				if (map.isEmpty()) {
					flushEmptyFolder(parent);
				} else {
					getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, parent, getBytes(map));
				}
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
		FolderSyncInfo folderInfo = getPhantomFolderSyncInfo(folder);
		if (folderInfo != null) {
			if (folder.getFolder(SyncFileWriter.CVS_DIRNAME).exists()) {
				// There is already a CVS subdirectory which indicates that the folder
				// was recreated by an external tool. 
				// Therefore, just forget what we had and use the info from disk.
				flushPhantomInfo(folder);
				return;
			}
			try {
				beginOperation(null);
				setFolderSync(folder, folderInfo);
				Map map = getPhantomResourceSyncInfoMap(folder);
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
			} finally {
				endOperation(null);
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
			if (container.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(FOLDER_SYNC_KEY, container, IResource.DEPTH_ZERO);
			}
			if (container.isPhantom()) {
				getWorkspaceSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, container, IResource.DEPTH_ZERO);
			}
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
			} else {
				FolderSyncInfo info = getFolderSync(container);
				if (info == null) return;
				getWorkspaceSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, container, getBytes(info));
				getWorkspaceSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, container, getBytes(getResourceSyncInfosForChildren(container)));
				changedResources.add(container);
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

}
