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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * This cache uses session properties to hold the bytes representing the sync
 * info
 */
/*package*/ class SessionPropertySyncInfoCache extends SyncInfoCache {
	
	// key used on a folder to indicate that the resource sync has been cahced for it's children
	private static final QualifiedName RESOURCE_SYNC_CACHED_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync-cached"); //$NON-NLS-1$
	private static final Object RESOURCE_SYNC_CACHED = new Object();
	
	/*package*/ static final String[] NULL_IGNORES = new String[0];
	private static final FolderSyncInfo NULL_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false); //$NON-NLS-1$ //$NON-NLS-2$
	
	/**
	 * If not already cached, loads and caches the folder ignores sync for the container.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 * @return the folder ignore patterns, or an empty array if none
	 */
	/*package*/ String[] cacheFolderIgnores(IContainer container) throws CVSException {
		try {
			// don't try to load if the information is already cached
			String[] ignores = (String[])container.getSessionProperty(IGNORE_SYNC_KEY);
			if (ignores == null) {
				// read folder ignores and remember it
				ignores = SyncFileWriter.readCVSIgnoreEntries(container);
				if (ignores == null) ignores = NULL_IGNORES;
				container.setSessionProperty(IGNORE_SYNC_KEY, ignores);
			}
			return ignores;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}


	/**
	 * If not already cached, loads and caches the folder sync for the container.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 * @return the folder sync info for the folder, or null if none.
	 */
	/*package*/ FolderSyncInfo cacheFolderSync(IContainer container) throws CVSException {
		if (!container.exists()) return null;
		try {
			// don't try to load if the information is already cached
			FolderSyncInfo info = (FolderSyncInfo)container.getSessionProperty(FOLDER_SYNC_KEY);
			if (info == null) {
				// read folder sync info and remember it
				info = SyncFileWriter.readFolderSync(container);
				if (info == null) {
					container.setSessionProperty(FOLDER_SYNC_KEY, NULL_FOLDER_SYNC_INFO);
				} else {
					container.setSessionProperty(FOLDER_SYNC_KEY, info);
				}
			} else if (info == NULL_FOLDER_SYNC_INFO) {
				info = null;
			}
			return info;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}


	/*package*/ boolean isResourceSyncInfoCached(IContainer container) throws CVSException {
		try {
			return container.getSessionProperty(RESOURCE_SYNC_CACHED_KEY) != null;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*package*/ void setResourceSyncInfoCached(IContainer container) throws CVSException {
		try {
			container.setSessionProperty(RESOURCE_SYNC_CACHED_KEY, RESOURCE_SYNC_CACHED);
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
		if (!container.exists()) return null;
		try {
			FolderSyncInfo info = (FolderSyncInfo)container.getSessionProperty(FOLDER_SYNC_KEY);
			if (info == null) {
				// There should be sync info but it was missing. Report the error
				throw new CVSException(Policy.bind("EclipseSynchronizer.folderSyncInfoMissing", container.getFullPath().toString())); //$NON-NLS-1$
			}
			if (info == NULL_FOLDER_SYNC_INFO) return null;
			return info;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/**
	 * Purges the cache recursively for all resources beneath the container.
	 * There must not be any pending uncommitted changes.
	 */
	/*package*/ void purgeCache(IContainer container, boolean deep) throws CVSException {
		if (! container.exists()) return;
		try {
			if (container.getType() != IResource.ROOT) {
				container.setSessionProperty(IGNORE_SYNC_KEY, null);
				container.setSessionProperty(FOLDER_SYNC_KEY, null);
				container.setSessionProperty(RESOURCE_SYNC_CACHED_KEY, null);
			}
			IResource[] members = container.members();
			for (int i = 0; i < members.length; i++) {
				IResource resource = members[i];
				resource.setSessionProperty(RESOURCE_SYNC_KEY, null);
				if (deep && resource.getType() != IResource.FILE) {
					purgeCache((IContainer) resource, deep);
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Sets the array of folder ignore patterns for the container, must not be null.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 * @param ignores the array of ignore patterns
	 */
	/*package*/ void setCachedFolderIgnores(IContainer container, String[] ignores) throws CVSException {
		try {
			container.setSessionProperty(IGNORE_SYNC_KEY, ignores);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}


	/**
	 * Sets the folder sync info for the container; if null, deletes it.
	 * Folder must exist and must not be the workspace root.
	 * The folder sync info for the container need not have previously been cached.
	 *
	 * @param container the container
	 * @param info the new folder sync info
	 */
	/*package*/ void setCachedFolderSync(IContainer container, FolderSyncInfo info) throws CVSException {
		if (!container.exists()) return;
		try {
			if (info == null) info = NULL_FOLDER_SYNC_INFO;
			container.setSessionProperty(FOLDER_SYNC_KEY, info);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/*package*/ void setDirtyIndicator(IResource resource, String indicator) throws CVSException {
		if (resource.getType() == IResource.FILE) {
			internalSetDirtyIndicator((IFile)resource, indicator);
		} else {
			internalSetDirtyIndicator((IContainer)resource, indicator);
		}
	}
	/*package*/ String getDirtyIndicator(IResource resource) throws CVSException {
		if (resource.getType() == IResource.FILE) {
			return internalGetDirtyIndicator((IFile)resource);
		} else {
			return internalGetDirtyIndicator((IContainer)resource);
		}
	}
	private void internalSetDirtyIndicator(IFile file, String indicator) throws CVSException {
		try {
			file.setSessionProperty(IS_DIRTY, indicator);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	private String internalGetDirtyIndicator(IFile file) throws CVSException {
		try {
			return (String)file.getSessionProperty(IS_DIRTY);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	private void internalSetDirtyIndicator(IContainer container, String indicator) throws CVSException {
		try {
			container.setPersistentProperty(IS_DIRTY, indicator);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	private String internalGetDirtyIndicator(IContainer container) throws CVSException {
		try {
			return container.getPersistentProperty(IS_DIRTY);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
		
	/**
	 * Return the dirty count for the given folder. For existing folders, the
	 * dirty count may not have been calculated yet and this method will return
	 * null in that case. For phantom folders, the dirty count is calculated if
	 * it does not exist yet.
	 */
	/*package*/ int getCachedDirtyCount(IContainer container) throws CVSException {
		if (!container.exists()) return -1;
		try {
			Integer dirtyCount = (Integer)container.getSessionProperty(DIRTY_COUNT);
			if (dirtyCount == null) {
				return -1;
			} else {
				return dirtyCount.intValue();
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*package*/ void setCachedDirtyCount(IContainer container, int count) throws CVSException {
		if (!container.exists()) return;
		try {
			container.setSessionProperty(DIRTY_COUNT, new Integer(count));
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Flush all cached info for the container and it's ancestors
	 */
	/*package*/ void flushDirtyCache(IResource resource) throws CVSException {
		if (resource.exists()) {
			try {
				if (resource.getType() == IResource.FILE) {
					resource.setSessionProperty(IS_DIRTY, null);
					resource.setSessionProperty(CLEAN_UPDATE, null);
				} else {
					resource.setSessionProperty(DIRTY_COUNT, null);
					resource.setSessionProperty(DELETED_CHILDREN, null);
					resource.setPersistentProperty(IS_DIRTY, null);
				}
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
		}
	}
	
	/*
	 * Add the deleted child and return true if it didn't exist before
	 */
	/*package*/ boolean addDeletedChild(IContainer container, IFile file) throws CVSException {
		try {
			Set deletedFiles = getDeletedChildren(container);
			if (deletedFiles == null)
				deletedFiles = new HashSet();
			String fileName = file.getName();
			if (deletedFiles.contains(fileName))
				return false;
			deletedFiles.add(fileName);
			setDeletedChildren(container, deletedFiles);
			return true;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/*package*/ boolean removeDeletedChild(IContainer container, IFile file) throws CVSException {
		try {
			Set deletedFiles = getDeletedChildren(container);
			if (deletedFiles == null || deletedFiles.isEmpty())
				return false;
			String fileName = file.getName();
			if (!deletedFiles.contains(fileName))
				return false;
			deletedFiles.remove(fileName);
			if (deletedFiles.isEmpty()) deletedFiles = null;
			setDeletedChildren(container, deletedFiles);
			return true;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	protected void setDeletedChildren(IContainer parent, Set deletedFiles) throws CVSException {
		if (!parent.exists()) return;
		try {
			parent.setSessionProperty(DELETED_CHILDREN, deletedFiles);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	private Set getDeletedChildren(IContainer parent) throws CoreException {
		if (!parent.exists()) return null;
		return (Set)parent.getSessionProperty(DELETED_CHILDREN);
	}
	
	/**
	 * Method updated flags the objetc as having been modfied by the updated
	 * handler. This flag is read during the resource delta to determine whether
	 * the modification made the file dirty or not.
	 *
	 * @param mFile
	 */
	/*package*/ void markFileAsUpdated(IFile file) throws CVSException {
		try {
			file.setSessionProperty(CLEAN_UPDATE, UPDATED_INDICATOR);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/*package*/ boolean contentsChangedByUpdate(IFile file) throws CVSException {
		try {
			Object indicator = file.getSessionProperty(CLEAN_UPDATE);
			boolean updated = false;
			if (indicator == UPDATED_INDICATOR) {
				// the file was changed due to a clean update (i.e. no local mods) so skip it
				file.setSessionProperty(CLEAN_UPDATE, null);
				file.setSessionProperty(IS_DIRTY, NOT_DIRTY_INDICATOR);
				updated = true;
			}
			return updated;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Method isSyncInfoLoaded returns true if all the sync info for the
	 * provided resources is loaded into the internal cache.
	 *
	 * @param resources
	 * @param i
	 * @return boolean
	 */
	/*package*/ boolean isSyncInfoLoaded(IContainer parent) throws CVSException {
		try {
			if (parent.getFolder(new Path(SyncFileWriter.CVS_DIRNAME)).exists()) {
				if (parent.getSessionProperty(RESOURCE_SYNC_KEY) == null)
					return false;
				if (parent.getSessionProperty(FOLDER_SYNC_KEY) == null)
					return false;
				if (parent.getSessionProperty(IGNORE_SYNC_KEY) == null)
					return false;
			}
		} catch (CoreException e) {
			// let future operations surface the error
			return false;
		}
		return true;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#getCachedSyncBytes(org.eclipse.core.resources.IResource)
	 */
	/*package*/ byte[] getCachedSyncBytes(IResource resource) throws CVSException {
		try {
			return (byte[])resource.getSessionProperty(RESOURCE_SYNC_KEY);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setCachedSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	/*package*/ void setCachedSyncBytes(IResource resource, byte[] syncBytes) throws CVSException {
		try {
			resource.setSessionProperty(RESOURCE_SYNC_KEY, syncBytes);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
}
