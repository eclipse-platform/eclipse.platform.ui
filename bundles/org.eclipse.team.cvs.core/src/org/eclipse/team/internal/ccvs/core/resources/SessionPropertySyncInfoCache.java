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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * This cache uses session properties to hold the bytes representing the sync
 * info. In addition when the workbench closes or a project is closed, the dirty 
 * state for all cvs managed folders are persisted using the resource's plugin
 * synchronizer.
 */
/*package*/ class SessionPropertySyncInfoCache extends SyncInfoCache implements ISaveParticipant {
	
	// key used on a folder to indicate that the resource sync has been cahced for it's children
	private static final QualifiedName RESOURCE_SYNC_CACHED_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync-cached"); //$NON-NLS-1$
	private static final QualifiedName FOLDER_SYNC_RESTORED_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-sync-restored"); //$NON-NLS-1$
	
	private static final Object RESOURCE_SYNC_CACHED = new Object();
	private static final Object FOLDER_SYNC_RESTORED = new Object();
	
	/*package*/ static final String[] NULL_IGNORES = new String[0];
	private static final FolderSyncInfo NULL_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false); //$NON-NLS-1$ //$NON-NLS-2$
	
	/*package*/ SessionPropertySyncInfoCache() {
		try {
			// this save participant is removed when the plugin is shutdown.			
			ResourcesPlugin.getWorkspace().addSaveParticipant(CVSProviderPlugin.getPlugin(), this);
		} catch (CoreException e) {
			CVSProviderPlugin.log(e.getStatus());
		}
	}
	
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

	/*package*/ boolean isFolderSyncInfoCached(IContainer container) throws CVSException {
		try {
			return container.getSessionProperty(FOLDER_SYNC_KEY) != null;
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
				purgeResourceSyncCache(resource);
				if (deep && resource.getType() != IResource.FILE) {
					purgeCache((IContainer) resource, deep);
				}
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/* package*/ void purgeResourceSyncCache(IResource resource) throws CVSException {
		try {
			resource.setSessionProperty(RESOURCE_SYNC_KEY, null);
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
			container.setSessionProperty(IS_DIRTY, indicator);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	private String internalGetDirtyIndicator(IContainer container) throws CVSException {
		try {
			String di = (String)container.getSessionProperty(IS_DIRTY);
			
			// if the session property is not available then restore from persisted sync info. At this
			// time the sync info is not flushed because we don't want the workspace to generate
			// a delta. Since the sync info is not flushed another session property is used to remember
			// that the sync info was already converted to a session property and has become stale.			
			if(di == null && container.getSessionProperty(FOLDER_SYNC_RESTORED_KEY) == null) {
				byte [] diBytes = ResourcesPlugin.getWorkspace().getSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, container);
				if(diBytes != null) {
					di = new String(diBytes);
					setDirtyIndicator(container, di);
				}
				container.setSessionProperty(FOLDER_SYNC_RESTORED_KEY, FOLDER_SYNC_RESTORED);
			}
			return di;
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
					resource.setSessionProperty(IS_DIRTY, null);
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
			
			// update dirty state so that decorators don't have to recompute after
			// an update is completed.
			contentsChangedByUpdate(file);
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
				if (parent.getSessionProperty(RESOURCE_SYNC_CACHED_KEY) == null)
					return false;
				if (parent.getSessionProperty(FOLDER_SYNC_KEY) == null)
					return false;
//				if (parent.getSessionProperty(IGNORE_SYNC_KEY) == null)
//					return false;
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

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#isDirtyCacheFlushed(org.eclipse.core.resources.IContainer)
	 */
	boolean isDirtyCacheFlushed(IContainer resource) throws CVSException {
		if (resource.exists()) {
			try {
					return resource.getSessionProperty(IS_DIRTY) == null;					
			} catch (CoreException e) {
				throw CVSException.wrapException(e);
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#doneSaving(org.eclipse.core.resources.ISaveContext)
	 */
	public void doneSaving(ISaveContext context) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#prepareToSave(org.eclipse.core.resources.ISaveContext)
	 */
	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.ISaveParticipant#rollback(org.eclipse.core.resources.ISaveContext)
	 */
	public void rollback(ISaveContext context) {			
	}

	/* Called when the workbench is shutdown or projects are closed. The dirty state
	 * of folders is persisted, using sync info, so that at startup or project open
	 * the folder state can be quickly calculated. This is mainly for improving decorator
	 * performance.
	 * @see org.eclipse.core.resources.ISaveParticipant#saving(org.eclipse.core.resources.ISaveContext)
	 */
	public void saving(ISaveContext context) throws CoreException {
		boolean fullSave = (context.getKind() == ISaveContext.FULL_SAVE);
		boolean projectSave = (context.getKind() == ISaveContext.PROJECT_SAVE);
		
		if(projectSave || fullSave) {
			// persist all session properties for folders into sync info.
			final ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
			synchronizer.add(RESOURCE_SYNC_KEY);
		
			// traverse the workspace looking for CVS managed projects or just the 
			// specific projects being closed
			IProject[] projects;
			if(projectSave) {
				projects = new IProject[1];
				projects[0] = context.getProject();
			} else {
				projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			}
			for (int i = 0; i < projects.length; i++) {
				IProject project = projects[i];
				RepositoryProvider provider = RepositoryProvider.getProvider(
														project,
														CVSProviderPlugin.getTypeId());
														
				// found a project managed by CVS, convert each session property on a
				// folder to a sync object.
				if (provider != null) {
					project.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							if(resource.getType() != IResource.FILE) {
								String di = null;
								try {
									di = getDirtyIndicator(resource);
								} catch (CVSException e) {
									// continue traversal
									CVSProviderPlugin.log(e);
								}
								if(di != null) {
									synchronizer.setSyncInfo(RESOURCE_SYNC_KEY, resource, di.getBytes());
								}								
							}
							return true;
						}
					});
				}
			}
		}
	}
}