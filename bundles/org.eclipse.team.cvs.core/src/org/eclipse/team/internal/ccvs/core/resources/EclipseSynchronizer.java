package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.ICVSResource;
import org.eclipse.team.internal.ccvs.core.ICVSRunnable;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.BaserevInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.NotifyInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ReentrantLock;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * A synchronizer is responsible for managing synchronization information for local
 * CVS resources.
 * 
 * Special processing has been added for linked folders and their childen so
 * that their CVS meta files are never read or written.
 * 
 * @see ResourceSyncInfo
 * @see FolderSyncInfo
 */
public class EclipseSynchronizer {	
	protected static final String IS_DIRTY_INDICATOR = SyncInfoCache.IS_DIRTY_INDICATOR;
	protected static final String NOT_DIRTY_INDICATOR = SyncInfoCache.NOT_DIRTY_INDICATOR;
	protected static final String RECOMPUTE_INDICATOR = SyncInfoCache.RECOMPUTE_INDICATOR; 
		
	// the cvs eclipse synchronizer is a singleton
	private static EclipseSynchronizer instance;
	
	// track resources that have changed in a given operation
	private ReentrantLock lock = new ReentrantLock();
	
	private Set changedResources = new HashSet();
	private Set changedFolders = new HashSet();
	
	private SessionPropertySyncInfoCache sessionPropertyCache = new SessionPropertySyncInfoCache();
	private SynchronizerSyncInfoCache synchronizerCache = new SynchronizerSyncInfoCache();
	
	/*
	 * Package private contructor to allow specialized subclass for handling folder deletions
	 */
	EclipseSynchronizer() {		
	}
	
	/**
	 * Returns the singleton instance of the synchronizer.
	 */
	public static EclipseSynchronizer getInstance() {		
		if(instance==null) {
			instance = new EclipseSynchronizer();
		}
		return instance;
	}
	
	public SyncInfoCache getSyncInfoCacheFor(IResource resource) {
		if (resource.exists()) {
			return sessionPropertyCache;
		} else {
			return synchronizerCache;
		}
	}

	private boolean isValid(IResource resource) {
		return resource.exists() || resource.isPhantom();
	}
	
	/**
	 * Sets the folder sync info for the specified folder.
	 * The folder must exist and must not be the workspace root.
	 * 
	 * @param folder the folder
	 * @param info the folder sync info, must not be null
	 * @see #getFolderSync, #deleteFolderSync
	 */
	public void setFolderSync(IContainer folder, FolderSyncInfo info) throws CVSException {
		Assert.isNotNull(info); // enforce the use of deleteFolderSync
		// ignore folder sync on the root (i.e. CVSROOT/config/TopLevelAdmin=yes but we just ignore it)
		if (folder.getType() == IResource.ROOT) return;
		if (!isValid(folder)) {
			throw new CVSException(IStatus.ERROR, CVSException.UNABLE,
				Policy.bind("EclipseSynchronizer.ErrorSettingFolderSync", folder.getFullPath().toString())); //$NON-NLS-1$
		}
		try {
			beginOperation(null);
			// set folder sync and notify
			getSyncInfoCacheFor(folder).setCachedFolderSync(folder, info);
			changedFolders.add(folder);
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Gets the folder sync info for the specified folder.
	 * 
	 * @param folder the folder
	 * @return the folder sync info associated with the folder, or null if none.
	 * @see #setFolderSync, #deleteFolderSync
	 */
	public FolderSyncInfo getFolderSync(IContainer folder) throws CVSException {
		if (folder.getType() == IResource.ROOT || !isValid(folder)) return null;
		try {
			beginOperation(null);
			cacheFolderSync(folder);
			return getSyncInfoCacheFor(folder).getCachedFolderSync(folder);
		} finally {
			endOperation(null);
		}
	}	

	/**
	 * Deletes the folder sync for the specified folder and the resource sync
	 * for all of its children.  Does not recurse.
	 * 
	 * @param folder the folder
	 * @see #getFolderSync, #setFolderSync
	 */
	public void deleteFolderSync(IContainer folder) throws CVSException {
		if (folder.getType() == IResource.ROOT || !isValid(folder)) return;
		try {
			beginOperation(null);
			// iterate over all children with sync info and prepare notifications
			// this is done first since deleting the folder sync may remove a phantom
			cacheResourceSyncForChildren(folder);
			IResource[] children = folder.members(true);
			for (int i = 0; i < children.length; i++) {
				IResource resource = children[i];
				changedResources.add(resource);
				// delete resource sync for all children
				getSyncInfoCacheFor(resource).setCachedSyncBytes(resource, null);
			}
			// delete folder sync
			getSyncInfoCacheFor(folder).setCachedFolderSync(folder, null);
			changedFolders.add(folder);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			endOperation(null);
		}
	}

	/**
	 * Sets the resource sync info for the specified resource.
	 * The parent folder must exist and must not be the workspace root.
	 * 
	 * @param resource the resource
	 * @param info the resource sync info, must not be null
	 * @see #getResourceSync, #deleteResourceSync
	 */
	public void setResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		Assert.isNotNull(info); // enforce the use of deleteResourceSync
		IContainer parent = resource.getParent();
		if (parent == null || parent.getType() == IResource.ROOT || !isValid(parent)) {
			throw new CVSException(IStatus.ERROR, CVSException.UNABLE,
				Policy.bind("EclipseSynchronizer.ErrorSettingResourceSync", resource.getFullPath().toString())); //$NON-NLS-1$
		}
		try {
			beginOperation(null);
			// cache resource sync for siblings, set for self, then notify
			cacheResourceSyncForChildren(parent);
			setCachedResourceSync(resource, info);
			changedResources.add(resource);		
		} finally {
			endOperation(null);
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
		byte[] info = getSyncBytes(resource);
		if (info == null) return null;
		return new ResourceSyncInfo(info);
	}

	/**
	 * Gets the resource sync info for the specified folder.
	 * 
	 * @param resource the resource
	 * @return the resource sync info associated with the resource, or null if none.
	 * @see #setResourceSync, #deleteResourceSync
	 */
	public byte[] getSyncBytes(IResource resource) throws CVSException {
		IContainer parent = resource.getParent();
		if (parent == null || parent.getType() == IResource.ROOT || !isValid(parent)) return null;
		try {
			beginOperation(null);
			// cache resource sync for siblings, then return for self
			try {
				cacheResourceSyncForChildren(parent);
			} catch (CVSException e) {
				if (e.getStatus().getCode() == IResourceStatus.WORKSPACE_LOCKED) {
					// This can occur if the resource sync is loaded during the POST_CHANGE delta phase.
					// We will resort to loading the sync info for the requested resource from disk
					return getSyncBytesFromDisk(resource);
				} else {
					throw e;
				}
			}
			return getCachedSyncBytes(resource);
		} finally {
			endOperation(null);
		}
	}

	/**
	 * Sets the resource sync info for the specified resource.
	 * The parent folder must exist and must not be the workspace root.
	 * 
	 * @param resource the resource
	 * @param info the resource sync info, must not be null
	 * @see #getResourceSync, #deleteResourceSync
	 */
	public void setSyncBytes(IResource resource, byte[] syncBytes) throws CVSException {
		Assert.isNotNull(syncBytes); // enforce the use of deleteResourceSync
		IContainer parent = resource.getParent();
		if (parent == null || parent.getType() == IResource.ROOT || !isValid(parent)) {
			throw new CVSException(IStatus.ERROR, CVSException.UNABLE,
				Policy.bind("EclipseSynchronizer.ErrorSettingResourceSync", resource.getFullPath().toString())); //$NON-NLS-1$
		}
		try {
			beginOperation(null);
			// cache resource sync for siblings, set for self, then notify
			cacheResourceSyncForChildren(parent);
			setCachedSyncBytes(resource, syncBytes);
			changedResources.add(resource);		
		} finally {
			endOperation(null);
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
		if (parent == null || parent.getType() == IResource.ROOT || !isValid(parent)) return;
		try {
			beginOperation(null);
			// cache resource sync for siblings, delete for self, then notify
			cacheResourceSyncForChildren(parent);
			if (getCachedSyncBytes(resource) != null) { // avoid redundant notifications
				setCachedSyncBytes(resource, null);
				changedResources.add(resource);
			}
		} finally {
			endOperation(null);
		}
	}

	/**
	 * Gets the array of ignore patterns for the specified folder.
	 * 
	 * @param folder the folder
	 * @return the patterns, or an empty array if none
	 * @see #addIgnored
	 */
	public String[] getIgnored(IContainer folder) throws CVSException {
		if (folder.getType() == IResource.ROOT || ! folder.exists()) return SessionPropertySyncInfoCache.NULL_IGNORES;
		try {
			beginOperation(null);
			return cacheFolderIgnores(folder);
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Adds a pattern to the set of ignores for the specified folder.
	 * 
	 * @param folder the folder
	 * @param pattern the pattern
	 */
	public void addIgnored(IContainer folder, String pattern) throws CVSException {
		if (folder.getType() == IResource.ROOT || ! folder.exists()) {
			throw new CVSException(IStatus.ERROR, CVSException.UNABLE,
				Policy.bind("EclipseSynchronizer.ErrorSettingIgnorePattern", folder.getFullPath().toString())); //$NON-NLS-1$
		}
		try {
			beginOperation(null);
			String[] ignores = cacheFolderIgnores(folder);
			if (ignores != null) {
				// verify that the pattern has not already been added
				for (int i = 0; i < ignores.length; i++) {
					if (ignores[i].equals(pattern)) return;
				}
				// add the pattern
				String[] oldIgnores = ignores;
				ignores = new String[oldIgnores.length + 1];
				System.arraycopy(oldIgnores, 0, ignores, 0, oldIgnores.length);
				ignores[oldIgnores.length] = pattern;
			} else {
				ignores = new String[] { pattern };
			}
			setCachedFolderIgnores(folder, ignores);
			SyncFileWriter.writeCVSIgnoreEntries(folder, ignores);
			// broadcast changes to unmanaged children - they are the only candidates for being ignored
			List possibleIgnores = new ArrayList();
			accumulateNonManagedChildren(folder, possibleIgnores);
			CVSProviderPlugin.broadcastSyncInfoChanges((IResource[])possibleIgnores.toArray(new IResource[possibleIgnores.size()]));
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Returns the members of this folder including deleted resources with sync info,
	 * but excluding special resources such as CVS subdirectories.
	 *
	 * @param folder the container to list
	 * @return the array of members
	 */
	public IResource[] members(IContainer folder) throws CVSException {
		if (! isValid(folder)) return new IResource[0];
		try {				
			beginOperation(null);
			if (folder.getType() != IResource.ROOT) {
				// ensure that the sync info is cached so any required phantoms are created
				cacheResourceSyncForChildren(folder);
			}
			return folder.members(true);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Begins a batch of operations.
	 * 
	 * @param monitor the progress monitor, may be null
	 */
	public void beginOperation(IProgressMonitor monitor) throws CVSException {
		lock.acquire();

		if (lock.getNestingCount() == 1) {
			prepareCache(monitor);
		}		
	}
	
	/**
	 * Ends a batch of operations.  Pending changes are committed only when
	 * the number of calls to endOperation() balances those to beginOperation().
	 * <p>
	 * Progress cancellation is ignored while writting the cache to disk. This
	 * is to ensure cache to disk consistency.
	 * </p>
	 * 
	 * @param monitor the progress monitor, may be null
	 * @exception CVSException with a status with code <code>COMMITTING_SYNC_INFO_FAILED</code>
	 * if all the CVS sync information could not be written to disk.
	 */
	public void endOperation(IProgressMonitor monitor) throws CVSException {		
		try {
			IStatus status = SyncInfoCache.STATUS_OK;
			if (lock.getNestingCount() == 1) {
				status = commitCache(monitor);
			}
			if (!status.isOK()) {
				throw new CVSException(status);
			}
		} finally {
			lock.release();
		}
	}
	
	/**
	 * Flushes unwritten sync information to disk.
	 * <p>
	 * Recursively commits unwritten sync information for all resources 
	 * below the root, and optionally purges the cached data from memory
	 * so that the next time it is accessed it will be retrieved from disk.
	 * May flush more sync information than strictly needed, but never less.
	 * </p>
	 * <p>
	 * Will throw a CVS Exception with a status with code = CVSStatus.DELETION_FAILED 
	 * if the flush could not perform CVS folder deletions. In this case, all other
	 * aspects of the operation succeeded.
	 * </p>
	 * 
	 * @param root the root of the subtree to flush
	 * @param purgeCache if true, purges the cache from memory as well
	 * @param deep purge sync from child folders
	 * @param monitor the progress monitor, may be null
	 */
	public void flush(IContainer root, boolean purgeCache, boolean deep, IProgressMonitor monitor) throws CVSException {
		// flush unwritten sync info to disk
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(null, 10);
		try {
			beginOperation(Policy.subMonitorFor(monitor, 1));
			
			IStatus status = commitCache(Policy.subMonitorFor(monitor, 7));
			
			// purge from memory too if we were asked to
			if (purgeCache) sessionPropertyCache.purgeCache(root, deep);
	
			// prepare for the operation again if we cut the last one short
			prepareCache(Policy.subMonitorFor(monitor, 1));
			
			if (!status.isOK()) {
				throw new CVSException(status);
			}
		} finally {
			endOperation(Policy.subMonitorFor(monitor, 1));
			monitor.done();
		}
	}

	private void purgeCache(IResource resource, boolean deep) throws CVSException {
		sessionPropertyCache.purgeResourceSyncCache(resource);
		if (resource.getType() != IResource.FILE) {
			sessionPropertyCache.purgeCache((IContainer)resource, deep);
		}
	}
	
	/**
	 * Called to notify the synchronizer that meta files have changed on disk, outside 
	 * of the workbench. The cache will be flushed for this folder and it's immediate
	 * children and appropriate state change events are broadcasts to state change
	 * listeners.
	 */
	public void syncFilesChanged(IContainer[] roots) throws CVSException {
		try {
			for (int i = 0; i < roots.length; i++) {
				IContainer root = roots[i];
				flush(root, true, false /*don't flush children*/, null);
				List changedPeers = new ArrayList();
				changedPeers.add(root);
				changedPeers.addAll(Arrays.asList(root.members()));
				IResource[] resources = (IResource[]) changedPeers.toArray(new IResource[changedPeers.size()]);
				CVSProviderPlugin.broadcastSyncInfoChanges(resources);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * The folder is about to be deleted (including its CVS subfolder).
	 * Take any appropriate action to remember the CVS information.
	 */
	public void prepareForDeletion(IResource resource) throws CVSException {
		if (!resource.exists()) return;
		try {
			beginOperation(null);
			// Flush the dirty info for the resource and it's ancestors.
			// Although we could be smarter, we need to do this because the
			// deletion may fail.
			adjustDirtyStateRecursively(resource, RECOMPUTE_INDICATOR);
			if (resource.getType() == IResource.FILE) {
				byte[] syncBytes = getSyncBytes(resource);
				if (syncBytes != null) {
					if (!ResourceSyncInfo.isAddition(syncBytes)) {
						syncBytes = convertToDeletion(syncBytes);
						synchronizerCache.setCachedSyncBytes(resource, syncBytes);
					}
					changedResources.add(resource);
				}
			} else {
				IContainer container = (IContainer)resource;
				if (container.getType() == IResource.PROJECT) {
					synchronizerCache.flush((IProject)container);
				} else {
					// Move the folder sync info into phantom space
					FolderSyncInfo info = getFolderSync(container);
					if (info == null) return;
					synchronizerCache.setCachedFolderSync(container, info);
					changedFolders.add(container);
					// move the resource sync as well
					byte[] syncBytes = getSyncBytes(resource);
					synchronizerCache.setCachedSyncBytes(resource, syncBytes);
				}
			}
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Prepare for a move or delete within the move/delete hook by moving the
	 * sync info into phantom space and flushing the session properties cache.
	 * This will allow sync info for deletions to be maintained in the source
	 * location and sync info at the destination to be preserved as well.
	 * 
	 * @param resource
	 * @param monitor
	 * @throws CVSException
	 */
	public void prepareForMoveDelete(IResource resource, IProgressMonitor monitor) throws CVSException {
		// Move sync info to phantom space for the resource and all it's children
		try {
			resource.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {
					try {
						prepareForDeletion(resource);
					} catch (CVSException e) {
						CVSProviderPlugin.log(e);
						throw new CoreException(e.getStatus());
					}
					return true;
				}
			});
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
		// purge the sync info to clear the session properties
		purgeCache(resource, true);
	}
	
	public void created(IResource resource) throws CVSException {
		if (resource.getType() == IResource.FILE) {
			created((IFile)resource);
		} else if (resource.getType() == IResource.FOLDER) {
			created((IFolder)resource);
		}
	}
	
	/**
	 * Notify the receiver that a folder has been created.
	 * Any existing phantom sync info will be moved
	 *
	 * @param folder the folder that has been created
	 */
	public void created(IFolder folder) throws CVSException {
		try {
			// set the dirty count using what was cached in the phantom it
			beginOperation(null);
			FolderSyncInfo folderInfo = synchronizerCache.getCachedFolderSync(folder);
			byte[] syncBytes = synchronizerCache.getCachedSyncBytes(folder);
			if (folderInfo != null && syncBytes != null) {
				if (folder.getFolder(SyncFileWriter.CVS_DIRNAME).exists()) {
					// There is already a CVS subdirectory which indicates that
					// either the folder was recreated by an external tool or that
					// a folder with CVS information was copied from another location.
					// To know the difference, we need to compare the folder sync info.
					// If they are mapped to the same root and repository then just
					// purge the phantom info. Otherwise, keep the original sync info.

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
				setCachedSyncBytes(folder, syncBytes);
			}
		} finally {
			try {
				endOperation(null);
			} finally {
				synchronizerCache.flush(folder);
			}
		}
	}

	/**
	 * Notify the receiver that a file has been created. Any existing phantom
	 * sync info will be moved
	 *
	 * @param file the file that has been created
	 */
	public void created(IFile file) throws CVSException {
		try {
			// set the dirty count using what was cached in the phantom it
			beginOperation(null);
			byte[] syncBytes = synchronizerCache.getCachedSyncBytes(file);
			if (syncBytes == null) return;
			byte[] newBytes = getSyncBytes(file);
			if (newBytes == null) {
				// only move the sync info if there is no new sync info
				setSyncBytes(file, convertFromDeletion(syncBytes));
			}
		} finally {
			try {
				endOperation(null);
			} finally {
				synchronizerCache.setCachedSyncBytes(file, null);
			}
		}
	}
	
	/**
	 * If not already cached, loads and caches the resource sync for the children of the container.
	 * Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 */
	private void cacheResourceSyncForChildren(IContainer container) throws CVSException {
		// don't try to load if the information is already cached
		if (! getSyncInfoCacheFor(container).isResourceSyncInfoCached(container)) {
			// load the sync info from disk
			byte[][] infos;
			// do not load the sync info for resources that are linked
			if (isLinkedResource(container)) {
				infos = null;
			} else {
				infos = SyncFileWriter.readAllResourceSync(container);
			}
			if (infos != null) {
				for (int i = 0; i < infos.length; i++) {
					byte[] syncBytes = infos[i];
					IPath name = new Path(getName(syncBytes));
					IResource resource;
					if (isFolder(syncBytes)) {
						resource = container.getFolder(name);
					} else {
						resource = container.getFile(name);
					}
					getSyncInfoCacheFor(resource).setCachedSyncBytes(resource, syncBytes);
				}
			}
			getSyncInfoCacheFor(container).setResourceSyncInfoCached(container);
		}
	}
	
	/**
	 * If not already cached, loads and caches the folder sync for the
	 * container. Folder must exist and must not be the workspace root.
	 *
	 * @param container the container
	 */
	private void cacheFolderSync(IContainer container) throws CVSException {
		// don't try to load if the information is already cached
		if (! getSyncInfoCacheFor(container).isFolderSyncInfoCached(container)) {
			// load the sync info from disk
			FolderSyncInfo info;
			// do not load the sync info for resources that are linked
			if (isLinkedResource(container)) {
				info = null;
			} else {
				info = SyncFileWriter.readFolderSync(container);
			}
			getSyncInfoCacheFor(container).setCachedFolderSync(container, info);
		}
	}
	
	private boolean isLinkedResource(IResource resource) {
		return CVSWorkspaceRoot.isLinkedResource(resource);
	}

	/**
	 * Load the sync info for the given resource from disk
	 * @param resource
	 * @return byte[]
	 */
	private byte[] getSyncBytesFromDisk(IResource resource) throws CVSException {
		byte[][] infos = SyncFileWriter.readAllResourceSync(resource.getParent());
		if (infos == null) return null;
		for (int i = 0; i < infos.length; i++) {
			byte[] syncBytes = infos[i];
			if (resource.getName().equals(getName(syncBytes))) {
				return syncBytes;
			}
		}
		return null;
	}
	
	/**
	 * Prepares the cache for a series of operations.
	 *
	 * @param monitor the progress monitor, may be null
	 */
	private void prepareCache(IProgressMonitor monitor) throws CVSException {
	}
	
	/**
	 * Commits the cache after a series of operations.
	 * 
	 * Will return STATUS_OK unless there were problems writting sync 
	 * information to disk. If an error occurs a multistatus is returned
	 * with the list of reasons for the failures. Failures are recovered,
	 * and all changed resources are given a chance to be written to disk.
	 * 
	 * @param monitor the progress monitor, may be null
	 */
	private IStatus commitCache(IProgressMonitor monitor) {
		if (changedFolders.isEmpty() && changedResources.isEmpty()) {
			return SyncInfoCache.STATUS_OK;
		}
		List errors = new ArrayList();
		try {
			/*** prepare operation ***/
			// find parents of changed resources
			Set dirtyParents = new HashSet();
			for(Iterator it = changedResources.iterator(); it.hasNext();) {
				IResource resource = (IResource) it.next();
				IContainer folder = resource.getParent();
				dirtyParents.add(folder);
			}
			
			monitor = Policy.monitorFor(monitor);
			int numDirty = dirtyParents.size();
			int numResources = changedFolders.size() + numDirty;
			monitor.beginTask(null, numResources);
			if(monitor.isCanceled()) {
				monitor.subTask(Policy.bind("EclipseSynchronizer.UpdatingSyncEndOperationCancelled")); //$NON-NLS-1$
			} else {
				monitor.subTask(Policy.bind("EclipseSynchronizer.UpdatingSyncEndOperation")); //$NON-NLS-1$
			}
			
			/*** write sync info to disk ***/
			// folder sync info changes
			for(Iterator it = changedFolders.iterator(); it.hasNext();) {
				IContainer folder = (IContainer) it.next();
				if (folder.exists() && folder.getType() != IResource.ROOT) {
					try {
						FolderSyncInfo info = sessionPropertyCache.getCachedFolderSync(folder);
						// Do not write the folder sync for linked resources
						if (info == null) {
							// deleted folder sync info since we loaded it
							// (but don't overwrite the sync info for linked folders
							if (!isLinkedResource(folder))
								SyncFileWriter.deleteFolderSync(folder);
							dirtyParents.remove(folder);
						} else {
							// modified or created new folder sync info since we loaded it
							SyncFileWriter.writeFolderSync(folder, info);
						}
					} catch(CVSException e) {					
						try {
							sessionPropertyCache.purgeCache(folder, true /* deep */);
						} catch(CVSException pe) {
							errors.add(pe.getStatus());
						}
						errors.add(e.getStatus());
					}
				}
				monitor.worked(1);
			}

			// update progress for parents we will skip because they were deleted
			monitor.worked(numDirty - dirtyParents.size());

			// resource sync info changes
			for (Iterator it = dirtyParents.iterator(); it.hasNext();) {
				IContainer folder = (IContainer) it.next();
				if (folder.exists() && folder.getType() != IResource.ROOT) {
					// write sync info for all children in one go
					try {
						List infos = new ArrayList();
						IResource[] children = folder.members(true);
						for (int i = 0; i < children.length; i++) {
							IResource resource = children[i];
							byte[] syncBytes = getSyncBytes(resource);
							if (syncBytes != null) {
								infos.add(syncBytes);
							}
						}
						// do not overwrite the sync info for linked resources
						if (infos.size() > 0 || !isLinkedResource(folder))
							SyncFileWriter.writeAllResourceSync(folder,
								(byte[][]) infos.toArray(new byte[infos.size()][]));
					} catch(CVSException e) {
						try {
							sessionPropertyCache.purgeCache(folder, false /* depth 1 */);
						} catch(CVSException pe) {
							errors.add(pe.getStatus());
						}							
						errors.add(e.getStatus());
					} catch (CoreException e) {
						try {
							sessionPropertyCache.purgeCache(folder, false /* depth 1 */);
						} catch(CVSException pe) {
							errors.add(pe.getStatus());
						}							
						errors.add(e.getStatus());
					}
				}
				monitor.worked(1);
			}
			
			/*** broadcast events ***/
			changedResources.addAll(changedFolders);
			changedResources.addAll(dirtyParents);	
			IResource[] resources = (IResource[]) changedResources.toArray(
				new IResource[changedResources.size()]);
			broadcastResourceStateChanges(resources);
			changedResources.clear();
			changedFolders.clear();
			if ( ! errors.isEmpty()) {
				MultiStatus status = new MultiStatus(CVSProviderPlugin.ID, 
											CVSStatus.COMMITTING_SYNC_INFO_FAILED, 
											Policy.bind("EclipseSynchronizer.ErrorCommitting"), //$NON-NLS-1$
											null);
				for (int i = 0; i < errors.size(); i++) {
					status.merge((IStatus)errors.get(i));
				}
				return status;
			}
			return SyncInfoCache.STATUS_OK;
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Broadcasts the resource state changes for the given resources to CVS Provider Plugin
	 */
	void broadcastResourceStateChanges(IResource[] resources) {
		if (resources.length > 0) {
			CVSProviderPlugin.broadcastSyncInfoChanges(resources);
			
			for (int i = 0; i < resources.length; i++) {
				IResource resource = resources[i];
				try {
					if((resource.getType() == IResource.FILE && !contentsChangedByUpdate((IFile)resource, false /* don't clear */)) ||
					    resource.getType() != IResource.FILE) {
						adjustDirtyStateRecursively(resource, RECOMPUTE_INDICATOR);
					}
				} catch (CVSException e) {
					CVSProviderPlugin.log(e);
				}
			}
		}
	}

	/**
	 * Returns the resource sync info for the resource; null if none.
	 * Parent must exist and must not be the workspace root.
	 * The resource sync info for the children of the parent container MUST ALREADY BE CACHED.
	 * 
	 * @param resource the resource
	 * @return the resource sync info for the resource, or null
	 * @see #cacheResourceSyncForChildren
	 */
	private byte[] getCachedSyncBytes(IResource resource) throws CVSException {
		return getSyncInfoCacheFor(resource).getCachedSyncBytes(resource);
	}

	/**
	 * Returns the resource sync info for the resource; null if none.
	 * Parent must exist and must not be the workspace root.
	 * The resource sync info for the children of the parent container MUST ALREADY BE CACHED.
	 * 
	 * @param resource the resource
	 * @return the resource sync info for the resource, or null
	 * @see #cacheResourceSyncForChildren
	 */
	private void setCachedSyncBytes(IResource resource, byte[] syncBytes) throws CVSException {
		getSyncInfoCacheFor(resource).setCachedSyncBytes(resource, syncBytes);
		changedResources.add(resource);
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
	private void setCachedResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		//todo
		byte[] syncBytes = null;
		if (info != null) syncBytes = info.getBytes();
		getSyncInfoCacheFor(resource).setCachedSyncBytes(resource, syncBytes);
	}
	
	/**
	 * If not already cached, loads and caches the folder ignores sync for the container.
	 * Folder must exist and must not be the workspace root.
	 * 
	 * @param container the container
	 * @return the folder ignore patterns, or an empty array if none
	 */
	private String[] cacheFolderIgnores(IContainer container) throws CVSException {
		return sessionPropertyCache.cacheFolderIgnores(container);
	}
	
	/**
	 * Sets the array of folder ignore patterns for the container, must not be null.
	 * Folder must exist and must not be the workspace root.
	 * 
	 * @param container the container
	 * @param ignores the array of ignore patterns
	 */
	private void setCachedFolderIgnores(IContainer container, String[] ignores) throws CVSException {
		sessionPropertyCache.setCachedFolderIgnores(container, ignores);
	}
	
	/**
	 * Recursively adds to the possibleIgnores list all children of the given 
	 * folder that can be ignored.
	 * 
	 * @param folder the folder to be searched
	 * @param possibleIgnores the list of IResources that can be ignored
	 */
	private void accumulateNonManagedChildren(IContainer folder, List possibleIgnores) throws CVSException {
		try {
			cacheResourceSyncForChildren(folder);
			IResource[] children = folder.members();
			List folders = new ArrayList();
			// deal with all files first and then folders to be otimized for caching scheme
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				if(getCachedSyncBytes(child)==null) {
					possibleIgnores.add(child);
				}
				if(child.getType()!=IResource.FILE) {
					folders.add(child);
				}
			}
			for (Iterator iter = folders.iterator(); iter.hasNext();) {
				IContainer child = (IContainer) iter.next();
				accumulateNonManagedChildren(child, possibleIgnores);
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Add the entry to the CVS/Notify file. We are not initially concerned with efficiency
	 * since edit/unedit are typically issued on a small set of files.
	 * 
	 * XXX If there was a previous notify entry for the resource, it is replaced. This is
	 * probably not the proper behavior (see EclipseFile).
	 * 
	 * @param resource
	 * @param info
	 */
	public void setNotifyInfo(IResource resource, NotifyInfo info) throws CVSException {
		NotifyInfo[] infos = SyncFileWriter.readAllNotifyInfo(resource.getParent());
		if (infos == null) {
			infos = new NotifyInfo[] { info };
		} else {
			Map infoMap = new HashMap();
			for (int i = 0; i < infos.length; i++) {
				NotifyInfo notifyInfo = infos[i];
				infoMap.put(infos[i].getName(), infos[i]);
			}
			infoMap.put(info.getName(), info);
			NotifyInfo[] newInfos = new NotifyInfo[infoMap.size()];
			int i = 0;
			for (Iterator iter = infoMap.values().iterator(); iter.hasNext();) {
				newInfos[i++] = (NotifyInfo) iter.next();
			}
			infos = newInfos;
		}
		SyncFileWriter.writeAllNotifyInfo(resource.getParent(), infos);
	}

	/**
	 * Method getNotifyInfo.
	 * @param resource
	 * @return NotifyInfo
	 */
	public NotifyInfo getNotifyInfo(IResource resource) throws CVSException {
		NotifyInfo[] infos = SyncFileWriter.readAllNotifyInfo(resource.getParent());
		if (infos == null) return null;
		for (int i = 0; i < infos.length; i++) {
			NotifyInfo notifyInfo = infos[i];
			if (notifyInfo.getName().equals(resource.getName())) {
				return notifyInfo;
			}
		}
		return null;
	}

	/**
	 * Method deleteNotifyInfo.
	 * @param resource
	 */
	public void deleteNotifyInfo(IResource resource) throws CVSException {
		NotifyInfo[] infos = SyncFileWriter.readAllNotifyInfo(resource.getParent());
		if (infos == null) return;
		Map infoMap = new HashMap();
		for (int i = 0; i < infos.length; i++) {
			NotifyInfo notifyInfo = infos[i];
			infoMap.put(infos[i].getName(), infos[i]);
		}
		infoMap.remove(resource.getName());
		NotifyInfo[] newInfos = new NotifyInfo[infoMap.size()];
		int i = 0;
		for (Iterator iter = infoMap.values().iterator(); iter.hasNext();) {
			newInfos[i++] = (NotifyInfo) iter.next();
		}
		SyncFileWriter.writeAllNotifyInfo(resource.getParent(), newInfos);
	}
	
	/**
	 * Add the entry to the CVS/Baserev file. We are not initially concerned
	 * with efficiency since edit/unedit are typically issued on a small set of
	 * files.
	 *
	 * XXX If there was a previous notify entry for the resource, it is replaced. This is
	 * probably not the proper behavior (see EclipseFile).
	 *
	 * @param resource
	 * @param info
	 */
	public void setBaserevInfo(IResource resource, BaserevInfo info) throws CVSException {
		BaserevInfo[] infos = SyncFileWriter.readAllBaserevInfo(resource.getParent());
		if (infos == null) {
			infos = new BaserevInfo[] { info };
		} else {
			Map infoMap = new HashMap();
			for (int i = 0; i < infos.length; i++) {
				infoMap.put(infos[i].getName(), infos[i]);
			}
			infoMap.put(info.getName(), info);
			BaserevInfo[] newInfos = new BaserevInfo[infoMap.size()];
			int i = 0;
			for (Iterator iter = infoMap.values().iterator(); iter.hasNext();) {
				newInfos[i++] = (BaserevInfo) iter.next();
			}
			infos = newInfos;
		}
		SyncFileWriter.writeAllBaserevInfo(resource.getParent(), infos);
	}

	/**
	 * Method getBaserevInfo.
	 * @param resource
	 * @return BaserevInfo
	 */
	public BaserevInfo getBaserevInfo(IResource resource) throws CVSException {
		BaserevInfo[] infos = SyncFileWriter.readAllBaserevInfo(resource.getParent());
		if (infos == null) return null;
		for (int i = 0; i < infos.length; i++) {
			BaserevInfo info = infos[i];
			if (info.getName().equals(resource.getName())) {
				return info;
			}
		}
		return null;
	}
			
	/**
	 * Method deleteNotifyInfo.
	 * @param resource
	 */
	public void deleteBaserevInfo(IResource resource) throws CVSException {
		BaserevInfo[] infos = SyncFileWriter.readAllBaserevInfo(resource.getParent());
		if (infos == null) return;
		Map infoMap = new HashMap();
		for (int i = 0; i < infos.length; i++) {
			infoMap.put(infos[i].getName(), infos[i]);
		}
		infoMap.remove(resource.getName());
		BaserevInfo[] newInfos = new BaserevInfo[infoMap.size()];
		int i = 0;
		for (Iterator iter = infoMap.values().iterator(); iter.hasNext();) {
			newInfos[i++] = (BaserevInfo) iter.next();
		}
		SyncFileWriter.writeAllBaserevInfo(resource.getParent(), newInfos);
	}

	public void copyFileToBaseDirectory(final IFile file, IProgressMonitor monitor) throws CVSException {
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				ResourceSyncInfo info = getResourceSync(file);
				// The file must exist remotely and locally
				if (info == null || info.isAdded() || info.isDeleted())
					return;
				SyncFileWriter.writeFileToBaseDirectory(file, monitor);
				changedResources.add(file);
			}
		}, monitor);
	}
	
	public void restoreFileFromBaseDirectory(final IFile file, IProgressMonitor monitor) throws CVSException {
		run(new ICVSRunnable() {
			public void run(IProgressMonitor monitor) throws CVSException {
				ResourceSyncInfo info = getResourceSync(file);
				// The file must exist remotely
				if (info == null || info.isAdded())
					return;
				SyncFileWriter.restoreFileFromBaseDirectory(file, monitor);
				changedResources.add(file);
			}
		}, monitor);
	}
	
	public void deleteFileFromBaseDirectory(final IFile file, IProgressMonitor monitor) throws CVSException {
		ResourceSyncInfo info = getResourceSync(file);
		// The file must exist remotely
		if (info == null || info.isAdded())
			return;
		SyncFileWriter.deleteFileFromBaseDirectory(file, monitor);
	}
	
	/**
	 * Method isSyncInfoLoaded returns true if all the sync info for the
	 * provided resources is loaded into the internal cache.
	 * 
	 * @param resources
	 * @param i
	 * @return boolean
	 */
	public boolean isSyncInfoLoaded(IResource[] resources, int depth) throws CVSException {
		// get the folders involved
		IContainer[] folders = getParentFolders(resources, depth);
		// for all folders that have a CVS folder, ensure the sync info is cached
		for (int i = 0; i < folders.length; i++) {
			IContainer parent = folders[i];
			if (!getSyncInfoCacheFor(parent).isSyncInfoLoaded(parent)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Method ensureSyncInfoLoaded loads all the relevent sync info into the cache
	 * @param resources
	 * @param i
	 * @return Object
	 */
	public void ensureSyncInfoLoaded(IResource[] resources, int depth) throws CVSException {
		// get the folders involved
		IContainer[] folders = getParentFolders(resources, depth);
		// Cache the sync info for all the folders
		for (int i = 0; i < folders.length; i++) {
			IContainer parent = folders[i];
			try {
				beginOperation(null);
				cacheResourceSyncForChildren(parent);
				cacheFolderSync(parent);
				cacheFolderIgnores(parent);
			} finally {
				endOperation(null);
			}
		}
	}

	/*
	 * Collect the projects and parent folders of the resources since 
	 * thats were the sync info is kept.
	 */
	private IContainer[] getParentFolders(IResource[] resources, int depth) throws CVSException {
		final Set folders = new HashSet();
		for (int i = 0; i < resources.length; i++) {
			IResource resource = resources[i];
			folders.add(resource.getProject());
			if (resource.getType() != IResource.PROJECT) {
				folders.add(resource.getParent());
			}
			// use the depth to gather child folders when appropriate
			if (depth != IResource.DEPTH_ZERO) {
				try {
					resource.accept(new IResourceVisitor() {
						public boolean visit(IResource resource) throws CoreException {
							if (resource.getType() == IResource.FOLDER)
								folders.add(resource);
							// let the depth determine who we visit
							return true;
						}
					}, depth, false);
				} catch (CoreException e) {
					throw CVSException.wrapException(e);
				}
			}
		}
		return (IContainer[]) folders.toArray(new IContainer[folders.size()]);
	}
	
	public void run(ICVSRunnable job, IProgressMonitor monitor) throws CVSException {
		monitor = Policy.monitorFor(monitor);
		monitor.beginTask(null, 100);
		try {
			beginOperation(Policy.subMonitorFor(monitor, 5));
			job.run(Policy.subMonitorFor(monitor, 60));
		} finally {
			endOperation(Policy.subMonitorFor(monitor, 35));
			monitor.done();
		}
	}
	
	/**
	 * Method isEdited returns true if a "cvs edit" was performed on the given
	 * file and no commit or unedit has yet been performed.
	 * @param iResource
	 * @return boolean
	 */
	public boolean isEdited(IFile resource) throws CVSException {
		return SyncFileWriter.isEdited(resource);
	}
	
	private void adjustDirtyStateRecursively(IResource resource, String indicator) throws CVSException {
		if (resource.getType() == IResource.ROOT) return;
		try {
			beginOperation(null);
			
			if (indicator == getDirtyIndicator(resource)) {
				return;
			} 					
			
			if (Policy.DEBUG_DIRTY_CACHING) {
				debug(resource, indicator, "adjusting dirty state");
			}

			getSyncInfoCacheFor(resource).setDirtyIndicator(resource, indicator);										

			IContainer parent = resource.getParent();
			if(indicator == NOT_DIRTY_INDICATOR) {
				adjustDirtyStateRecursively(parent, RECOMPUTE_INDICATOR);
			}
			
			if(indicator == RECOMPUTE_INDICATOR) {
				adjustDirtyStateRecursively(parent, RECOMPUTE_INDICATOR);
			} 
			
			if(indicator == IS_DIRTY_INDICATOR) {
				adjustDirtyStateRecursively(parent, indicator);
			} 
		} finally {
			endOperation(null);
		}
	}

	protected String getDirtyIndicator(IResource resource) throws CVSException {
		try {
			beginOperation(null);
			return getSyncInfoCacheFor(resource).getDirtyIndicator(resource);
		} finally {
			endOperation(null);
		}
	}
	
	/*
	 * Mark the given resource as either modified or clean using a persistant
	 * property. Do nothing if the modified state is already what we want.
	 * Return true if the modification state was changed.
	 */
	protected void setDirtyIndicator(IResource resource, boolean modified) throws CVSException {
		try {
			beginOperation(null);
			String indicator = modified ? IS_DIRTY_INDICATOR : NOT_DIRTY_INDICATOR;
			// set the dirty indicator and adjust the parent accordingly			
			adjustDirtyStateRecursively(resource, indicator);
		} finally {
			endOperation(null);
		}
	}
	
	/**
	 * Method updated flags the objetc as having been modfied by the updated
	 * handler. This flag is read during the resource delta to determine whether
	 * the modification made the file dirty or not.
	 *
	 * @param mFile
	 */
	public void markFileAsUpdated(IFile file) throws CVSException {
		sessionPropertyCache.markFileAsUpdated(file);
	}
	
	protected boolean contentsChangedByUpdate(IFile file, boolean clear) throws CVSException {
		if(file.exists()) {
			return sessionPropertyCache.contentsChangedByUpdate(file, clear);
		} else {
			return false;
		}
	}

	/**
	 * Method getName.
	 * @param syncBytes
	 */
	private String getName(byte[] syncBytes) throws CVSException {
		return ResourceSyncInfo.getName(syncBytes);
	}
	
	/**
	 * Method isFolder.
	 * @param syncBytes
	 * @return boolean
	 */
	private boolean isFolder(byte[] syncBytes) {
		return ResourceSyncInfo.isFolder(syncBytes);
	}
		
	/**
	 * Method convertToDeletion.
	 * @param syncBytes
	 * @return byte[]
	 */
	private byte[] convertToDeletion(byte[] syncBytes) throws CVSException {
		return ResourceSyncInfo.convertToDeletion(syncBytes);
	}
	
	/**
	 * Method convertFromDeletion.
	 * @param syncBytes
	 */
	private byte[] convertFromDeletion(byte[] syncBytes) throws CVSException {
		return ResourceSyncInfo.convertFromDeletion(syncBytes);
	}
	
	/**
	 * Method createdByMove clears any session properties on the file so it
	 * appears as an ADDED file.
	 * 
	 * @param destination
	 */
	public void createdByMove(IFile file) throws CVSException {
		deleteResourceSync(file);
	}

	static public void debug(IResource resource, String indicator, String string) {
		String di = EclipseSynchronizer.IS_DIRTY_INDICATOR;
		if(indicator == EclipseSynchronizer.IS_DIRTY_INDICATOR) {
			di = "dirty";
		} else if(indicator == EclipseSynchronizer.NOT_DIRTY_INDICATOR) {
			di = "clean";
		} else {
			di = "needs recomputing";
		} 
		System.out.println("["+string + ":" + di + "]  "  + resource.getFullPath());
	}
	
}
