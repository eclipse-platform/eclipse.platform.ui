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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.core.RepositoryProvider;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.ICVSDecoratorEnablementListener;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * This cache uses session properties to hold the bytes representing the sync
 * info. In addition when the workbench closes or a project is closed, the dirty 
 * state for all cvs managed folders are persisted using the resource's plugin
 * synchronizer.
 */
/*package*/ class SessionPropertySyncInfoCache extends SyncInfoCache 
																		  implements ISaveParticipant, ICVSDecoratorEnablementListener {
	
	// key used on a folder to indicate that the resource sync has been cahced for it's children
	private static final QualifiedName RESOURCE_SYNC_CACHED_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync-cached"); //$NON-NLS-1$
	private static final Object RESOURCE_SYNC_CACHED = new Object();
	
	/*package*/ static final String[] NULL_IGNORES = new String[0];
	private static final FolderSyncInfo NULL_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false); //$NON-NLS-1$ //$NON-NLS-2$
	
	private QualifiedName FOLDER_DIRTY_STATE_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-dirty-state-cached"); //$NON-NLS-1$
	private boolean isDecoratorEnabled = true;
	
	/*package*/ SessionPropertySyncInfoCache() {
		try {
			// this save participant is removed when the plugin is shutdown.			
			ResourcesPlugin.getWorkspace().addSaveParticipant(CVSProviderPlugin.getPlugin(), this);
			CVSProviderPlugin.getPlugin().addDecoratorEnablementListener(this);

			final ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
			synchronizer.add(FOLDER_DIRTY_STATE_KEY);
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
		// don't try to load if the information is already cached
		String[] ignores = (String[])safeGetSessionProperty(container, IGNORE_SYNC_KEY);
		if (ignores == null) {
			// read folder ignores and remember it
			ignores = SyncFileWriter.readCVSIgnoreEntries(container);
			if (ignores == null) ignores = NULL_IGNORES;
			safeSetSessionProperty(container, IGNORE_SYNC_KEY, ignores);
		}
		return ignores;
	}

	/*package*/ boolean isFolderSyncInfoCached(IContainer container) throws CVSException {
		return safeGetSessionProperty(container, FOLDER_SYNC_KEY) != null;
	}

	/*package*/ boolean isResourceSyncInfoCached(IContainer container) throws CVSException {
		return safeGetSessionProperty(container, RESOURCE_SYNC_CACHED_KEY) != null;
	}
	
	/*package*/ void setResourceSyncInfoCached(IContainer container) throws CVSException {
		safeSetSessionProperty(container, RESOURCE_SYNC_CACHED_KEY, RESOURCE_SYNC_CACHED);
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
		FolderSyncInfo info = (FolderSyncInfo)safeGetSessionProperty(container, FOLDER_SYNC_KEY);
		if (info == null) {
			// There should be sync info but it was missing. Report the error
			throw new CVSException(Policy.bind("EclipseSynchronizer.folderSyncInfoMissing", container.getFullPath().toString())); //$NON-NLS-1$
		}
		if (info == NULL_FOLDER_SYNC_INFO) return null;
		return info;
	}

	/**
	 * Purges the cache recursively for all resources beneath the container.
	 * There must not be any pending uncommitted changes.
	 */
	/*package*/ void purgeCache(IContainer container, boolean deep) throws CVSException {
		if (! container.exists()) return;
		try {
			if (container.getType() != IResource.ROOT) {
				safeSetSessionProperty(container, IGNORE_SYNC_KEY, null);
				safeSetSessionProperty(container, FOLDER_SYNC_KEY, null);
				safeSetSessionProperty(container, RESOURCE_SYNC_CACHED_KEY, null);
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
		safeSetSessionProperty(resource, RESOURCE_SYNC_KEY, null);
	}
	
	/**
	 * Sets the array of folder ignore patterns for the container, must not be null.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 * @param ignores the array of ignore patterns
	 */
	/*package*/ void setCachedFolderIgnores(IContainer container, String[] ignores) throws CVSException {
		safeSetSessionProperty(container, IGNORE_SYNC_KEY, ignores);
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
		if (info == null) {
			info = NULL_FOLDER_SYNC_INFO;
		} 
		safeSetSessionProperty(container, FOLDER_SYNC_KEY, info);
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
		safeSetSessionProperty(file, IS_DIRTY, indicator);
	}
	
	private String internalGetDirtyIndicator(IFile file) throws CVSException {
		String di = (String)safeGetSessionProperty(file, IS_DIRTY);
		if(di == null) {
			di = RECOMPUTE_INDICATOR;
		}
		return di;
	}

	private void internalSetDirtyIndicator(IContainer container, String indicator) throws CVSException {
		safeSetSessionProperty(container, IS_DIRTY, indicator);
	}
	
	private String internalGetDirtyIndicator(IContainer container) throws CVSException {
		try {
			String di = (String)safeGetSessionProperty(container, IS_DIRTY);
			
			// if the session property is not available then restore from persisted sync info. At this
			// time the sync info is not flushed because we don't want the workspace to generate
			// a delta.			
			if(di == null) {
				byte [] diBytes = ResourcesPlugin.getWorkspace().getSynchronizer().getSyncInfo(FOLDER_DIRTY_STATE_KEY, container);
				if(diBytes != null) {
					di = new String(diBytes);
					if(di.equals(NOT_DIRTY_INDICATOR)) {
						di = NOT_DIRTY_INDICATOR;
					} else if(di.equals(IS_DIRTY_INDICATOR)) {
						di = IS_DIRTY_INDICATOR;
					} else {
						di = RECOMPUTE_INDICATOR;
					}
				} else {
					di = RECOMPUTE_INDICATOR;
				}
				setDirtyIndicator(container, di);
			}
			return di;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
		
	/*
	 * Flush all cached info for the container and it's ancestors
	 */
	/*package*/ void flushDirtyCache(IResource resource) throws CVSException {
		if (resource.exists()) {
			if (resource.getType() == IResource.FILE) {
				safeSetSessionProperty(resource, IS_DIRTY, RECOMPUTE_INDICATOR);
				safeSetSessionProperty(resource, CLEAN_UPDATE, null);
			} else {
				safeSetSessionProperty(resource, IS_DIRTY, RECOMPUTE_INDICATOR);
			}
		}
	}
	
	/**
	 * Method updated flags the objetc as having been modfied by the updated
	 * handler. This flag is read during the resource delta to determine whether
	 * the modification made the file dirty or not.
	 *
	 * @param mFile
	 */
	/*package*/ void markFileAsUpdated(IFile file) throws CVSException {
		safeSetSessionProperty(file, CLEAN_UPDATE, UPDATED_INDICATOR);
	}

	/*package*/ boolean contentsChangedByUpdate(IFile file, boolean clear) throws CVSException {
		Object indicator = safeGetSessionProperty(file, CLEAN_UPDATE);
		boolean updated = false;
		if (indicator == UPDATED_INDICATOR) {
			// the file was changed due to a clean update (i.e. no local mods) so skip it
			if(clear) {
				safeSetSessionProperty(file, CLEAN_UPDATE, null);
				safeSetSessionProperty(file, IS_DIRTY, NOT_DIRTY_INDICATOR);
			}
			updated = true;
		}
		return updated;
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
		if (parent.getFolder(new Path(SyncFileWriter.CVS_DIRNAME)).exists()) {
			if (safeGetSessionProperty(parent, RESOURCE_SYNC_CACHED_KEY) == null)
				return false;
			if (safeGetSessionProperty(parent, FOLDER_SYNC_KEY) == null)
				return false;
//				if (parent.getSessionProperty(IGNORE_SYNC_KEY) == null)
//					return false;
		}
		return true;
	}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#getCachedSyncBytes(org.eclipse.core.resources.IResource)
	 */
	/*package*/ byte[] getCachedSyncBytes(IResource resource) throws CVSException {
		return (byte[])safeGetSessionProperty(resource, RESOURCE_SYNC_KEY);
	}

	Object safeGetSessionProperty(IResource resource, QualifiedName key) throws CVSException {
		try {
			return resource.getSessionProperty(key);
		} catch (CoreException e) {
			IStatus status = e.getStatus();
			if(status != null) {
				int code = e.getStatus().getCode();
				if(code != IResourceStatus.RESOURCE_NOT_LOCAL ||
					code != IResourceStatus.RESOURCE_NOT_FOUND) {
						// ignore error since a phantom would of been created
						// and we can safely ignore these cases
						return null;
				}
			}
			// some other error we did not expect
			throw CVSException.wrapException(e);
		}
	}
	
	void safeSetSessionProperty(IResource resource, QualifiedName key, Object value) throws CVSException {
			try {
				resource.setSessionProperty(key, value);
			} catch (CoreException e) {
				IStatus status = e.getStatus();
				if(status != null) {
					int code = e.getStatus().getCode();
					if(code == IResourceStatus.RESOURCE_NOT_LOCAL ||
					    code == IResourceStatus.RESOURCE_NOT_FOUND) {
					    	// ignore error since a phantom would of been created
					    	// and we can safely ignore these cases
					}
					// some other error we did not expect
					throw CVSException.wrapException(e);
				}
			}
		}
	
	/**
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#setCachedSyncBytes(org.eclipse.core.resources.IResource, byte[])
	 */
	/*package*/ void setCachedSyncBytes(IResource resource, byte[] syncBytes) throws CVSException {
		safeSetSessionProperty(resource, RESOURCE_SYNC_KEY, syncBytes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.resources.SyncInfoCache#isDirtyCacheFlushed(org.eclipse.core.resources.IContainer)
	 */
	boolean isDirtyCacheFlushed(IContainer resource) throws CVSException {
		if (resource.exists()) {
			return getDirtyIndicator(resource) == RECOMPUTE_INDICATOR;					
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
		
		if(isDecoratorEnabled && (projectSave || fullSave)) {
			// persist all session properties for folders into sync info.
			final ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
		
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
									synchronizer.setSyncInfo(FOLDER_DIRTY_STATE_KEY, resource, di.getBytes());
								}								
							}
							return true;
						}
					});
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see ICVSDecoratorEnablementListener#decoratorEnablementChanged(boolean)
	 */
	public void decoratorEnablementChanged(boolean enabled) {
		// DECORATOR enable this code once PR 32354 is fixed.
		// In addition, try and remove any code paths that are not required if the decorators
		// are turned off.
//		isDecoratorEnabled = enabled;
//		if(!enabled) {
//			flushDirtyStateFromDisk();		
//		}
	}
	
	/* 
	 * Called to clear the folder dirty state from the resource sync tree and stop persisting
	 * these values to disk.
	 */
	private void flushDirtyStateFromDisk() {
		final ISynchronizer synchronizer = ResourcesPlugin.getWorkspace().getSynchronizer();
	
		IProject[] projects;
		projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();

		for (int i = 0; i < projects.length; i++) {
			IProject project = projects[i];
			RepositoryProvider provider = RepositoryProvider.getProvider(
													project,
													CVSProviderPlugin.getTypeId());
													
			try {
				synchronizer.flushSyncInfo(FOLDER_DIRTY_STATE_KEY, project, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				CVSProviderPlugin.log(e.getStatus());
			}
		}
	}
}