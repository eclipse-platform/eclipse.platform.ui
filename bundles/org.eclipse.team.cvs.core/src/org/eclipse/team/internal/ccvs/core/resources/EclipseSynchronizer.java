package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSStatus;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ReentrantLock;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.SyncFileWriter;

/**
 * A synchronizer is responsible for managing synchronization information for local
 * CVS resources.
 * 
 * @see ResourceSyncInfo
 * @see FolderSyncInfo
 */
public class EclipseSynchronizer {
	// the resources plugin synchronizer is used to cache and possibly persist. These 
	// are keys for storing the sync info.
	private static final QualifiedName FOLDER_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-sync"); //$NON-NLS-1$
	private static final QualifiedName RESOURCE_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync"); //$NON-NLS-1$
	private static final QualifiedName IGNORE_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-ignore"); //$NON-NLS-1$
	
	private static final String[] NULL_IGNORES = new String[0];
	private static final FolderSyncInfo NULL_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false); //$NON-NLS-1$ //$NON-NLS-2$
	
	private static final IStatus STATUS_OK = new Status(IStatus.OK, CVSProviderPlugin.ID, 0, Policy.bind("ok"), null); //$NON-NLS-1$
	
	// the cvs eclipse synchronizer is a singleton
	private static EclipseSynchronizer instance;
	
	// track resources that have changed in a given operation
	private ReentrantLock lock = new ReentrantLock();
	
	private Set changedResources = new HashSet();
	private Set changedFolders = new HashSet();
	
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
			instance = new EclipsePhantomSynchronizer();
		}
		return instance;
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
		if (folder.getType() == IResource.ROOT || ! folder.exists()) {
			throw new CVSException(IStatus.ERROR, CVSException.UNABLE,
				Policy.bind("EclipseSynchronizer.ErrorSettingFolderSync", folder.getFullPath().toString())); //$NON-NLS-1$
		}
		try {
			beginOperation(null);
			// set folder sync and notify
			setCachedFolderSync(folder, info);
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
		if (folder.getType() == IResource.ROOT || ! folder.exists()) return null;
		try {
			beginOperation(null);
			// cache folder sync and return it
			return cacheFolderSync(folder);
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
		if (folder.getType() == IResource.ROOT || ! folder.exists()) return;
		try {
			beginOperation(null);
			// delete folder sync
			setCachedFolderSync(folder, null);
			changedFolders.add(folder);
			// iterate over all children with sync info and prepare notifications
			cacheResourceSyncForChildren(folder);
			Collection infos = getCachedResourceSyncForChildren(folder);
			for (Iterator it = infos.iterator(); it.hasNext();) {
				ResourceSyncInfo info = (ResourceSyncInfo) it.next();
				IPath path = new Path(info.getName());
				if(info.isDirectory()) {
					changedResources.add(folder.getFolder(path));
				} else {
					changedResources.add(folder.getFile(path));
				}
			}
			// delete resource sync for all children
			deleteCachedResourceSyncForChildren(folder);
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
		if (parent == null || ! parent.exists() || parent.getType() == IResource.ROOT) {
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
		IContainer parent = resource.getParent();
		if (parent == null || ! parent.exists() || parent.getType() == IResource.ROOT) return null;
		try {
			beginOperation(null);
			// cache resource sync for siblings, then return for self
			cacheResourceSyncForChildren(parent);
			return getCachedResourceSync(resource);
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
		if (parent == null || ! parent.exists() || parent.getType() == IResource.ROOT) return;
		try {
			beginOperation(null);
			// cache resource sync for siblings, delete for self, then notify
			cacheResourceSyncForChildren(resource.getParent());
			if (getCachedResourceSync(resource) != null) { // avoid redundant notifications
				setCachedResourceSync(resource, null);
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
		if (folder.getType() == IResource.ROOT || ! folder.exists()) return NULL_IGNORES;
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
			CVSProviderPlugin.broadcastResourceStateChanges((IResource[])possibleIgnores.toArray(new IResource[possibleIgnores.size()]));
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
		if (! folder.exists()) return new IResource[0];
		try {				
			beginOperation(null);
			if (folder.getType() == IResource.ROOT) return folder.members();
			cacheResourceSyncForChildren(folder);
			Collection infos = getCachedResourceSyncForChildren(folder);
			// add all children with or without sync info
			Set childResources = new HashSet();
			for (Iterator it = infos.iterator(); it.hasNext();) {
				ResourceSyncInfo info = (ResourceSyncInfo) it.next();
				IPath path = new Path(info.getName());
				if(info.isDirectory()) {
					childResources.add(folder.getFolder(path));
				} else {
					childResources.add(folder.getFile(path));
				}
			}
			childResources.addAll(Arrays.asList(folder.members()));
			return (IResource[])childResources.toArray(new IResource[childResources.size()]);
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
			IStatus status = STATUS_OK;
			if (lock.getNestingCount() == 1) {
				status = commitCache(monitor);
			}
			if (status != STATUS_OK) {
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
			if (purgeCache) purgeCache(root, deep);
	
			// prepare for the operation again if we cut the last one short
			prepareCache(Policy.subMonitorFor(monitor, 1));
			
			if (status != STATUS_OK) {
				throw new CVSException(status);
			}
		} finally {
			endOperation(Policy.subMonitorFor(monitor, 1));
			monitor.done();
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
				CVSProviderPlugin.broadcastResourceStateChanges((IResource[]) changedPeers.toArray(new IResource[changedPeers.size()]));
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * The folder is about to be deleted (including its CVS subfolder).
	 * Take any appropriate action to remember the CVS information.
	 */
	public void prepareForDeletion(IContainer container) throws CVSException {
	}
	
	/**
	 * Signal to the synchronizer that a folder has been created
	 * 
	 * @param folder the folder to be created
	 */
	public void folderCreated(IFolder folder) throws CVSException {
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
			broadcastResourceStateChanges(new IResource[0]);
			return STATUS_OK;
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
						FolderSyncInfo info = getCachedFolderSync(folder);
						if (info == null) {
							// deleted folder sync info since we loaded it
							SyncFileWriter.deleteFolderSync(folder);
							dirtyParents.remove(folder);
						} else {
							// modified or created new folder sync info since we loaded it
							SyncFileWriter.writeFolderSync(folder, info);
						}
					} catch(CVSException e) {					
						try {
							purgeCache(folder, true /* deep */);
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
						Collection infos = getCachedResourceSyncForChildren(folder);
						SyncFileWriter.writeAllResourceSync(folder,
							(ResourceSyncInfo[]) infos.toArray(new ResourceSyncInfo[infos.size()]));
					} catch(CVSException e) {
						try {
							purgeCache(folder, false /* depth 1 */);
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
			return STATUS_OK;
		} finally {
			monitor.done();
		}
	}
	
	/**
	 * Broadcasts the resource state changes for the given resources to CVS Provider Plugin
	 */
	void broadcastResourceStateChanges(IResource[] resources) {
		if (resources.length > 0) {
			CVSProviderPlugin.broadcastResourceStateChanges(resources);
		}
	}
	
	/**
	 * Purges the cache recursively for all resources beneath the container.
	 * There must not be any pending uncommitted changes.
	 */
	private static void purgeCache(IContainer container, boolean deep) throws CVSException {
		if (! container.exists()) return;
		try {
			if (container.getType() != IResource.ROOT) {
				container.setSessionProperty(RESOURCE_SYNC_KEY, null);
				container.setSessionProperty(IGNORE_SYNC_KEY, null);
				container.setSessionProperty(FOLDER_SYNC_KEY, null);
			}
			if(deep) {
				IResource[] members = container.members();
				for (int i = 0; i < members.length; i++) {
					IResource resource = members[i];
					if (resource.getType() != IResource.FILE) {
						purgeCache((IContainer) resource, deep);
					}
				}
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
	private static void cacheResourceSyncForChildren(IContainer container) throws CVSException {
		try {
			// don't try to load if the information is already cached
			HashMap children = (HashMap)container.getSessionProperty(RESOURCE_SYNC_KEY);
			if (children == null) {
				// load the sync info from disk
				ResourceSyncInfo[] infos = SyncFileWriter.readAllResourceSync(container);
				if (infos != null) {
					children = new HashMap(infos.length);
					for (int i = 0; i < infos.length; i++) {
						ResourceSyncInfo syncInfo = infos[i];					
						children.put(syncInfo.getName(), syncInfo);
					}
				} else {
					children = new HashMap(0);
				}
				container.setSessionProperty(RESOURCE_SYNC_KEY, children);
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
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
	private static ResourceSyncInfo getCachedResourceSync(IResource resource) throws CVSException {
		try {
			IContainer parent = resource.getParent();
			HashMap children = (HashMap)resource.getParent().getSessionProperty(RESOURCE_SYNC_KEY);
			if (children == null) {
				// There should be sync info but it was missing. Report the error
				throw new CVSException(Policy.bind("EclipseSynchronizer.folderSyncInfoMissing", parent.getFullPath().toString())); //$NON-NLS-1$
			}
			return (ResourceSyncInfo) children.get(resource.getName());
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}

	/**
	 * Sets the resource sync info for the resource; if null, deletes it.
	 * Parent must exist and must not be the workspace root.
	 * The resource sync info for the children of the parent container MUST ALREADY BE CACHED.
	 * 
	 * @param resource the resource
	 * @param info the new resource sync info
	 * @see #cacheResourceSyncForChildren
	 */
	private static void setCachedResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		try {
			IContainer parent = resource.getParent();
			HashMap children = (HashMap)parent.getSessionProperty(RESOURCE_SYNC_KEY);
			Assert.isNotNull(children);
			if (info == null) {
				children.remove(resource.getName());
			} else {
				children.put(resource.getName(), info);
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
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
	private static Collection /* of ResourceSyncInfo */ getCachedResourceSyncForChildren(IContainer container) throws CVSException {
		try {
			HashMap children = (HashMap)container.getSessionProperty(RESOURCE_SYNC_KEY);
			if (children == null) {
				// There should be sync info but it was missing. Report the error
				throw new CVSException(Policy.bind("EclipseSynchronizer.folderSyncInfoMissing", container.getFullPath().toString())); //$NON-NLS-1$
			}
			return children.values();
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Deletes the resource sync info for all children of the container.
	 * Container must exist and must not be the workspace root.
	 * The resource sync info for the children of the container need not have previously been cached.
	 * 
	 * @param container the container
	 */
	private static void deleteCachedResourceSyncForChildren(IContainer container) throws CVSException {
		try {
			HashMap children = (HashMap)container.getSessionProperty(RESOURCE_SYNC_KEY);
			if (children != null) {
				children.clear();
			} else {
				children = new HashMap(0);
				container.setSessionProperty(RESOURCE_SYNC_KEY, children);
			}
		} catch(CoreException e) {
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
	private static FolderSyncInfo cacheFolderSync(IContainer container) throws CVSException {
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

	/**
	 * Returns the folder sync info for the container; null if none.
	 * Folder must exist and must not be the workspace root.
	 * The folder sync info for the container MUST ALREADY BE CACHED.
	 * 
	 * @param container the container
	 * @return the folder sync info for the folder, or null if none.
	 * @see #cacheFolderSync
	 */
	private static FolderSyncInfo getCachedFolderSync(IContainer container) throws CVSException {
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
	 * Sets the folder sync info for the container; if null, deletes it.
	 * Folder must exist and must not be the workspace root.
	 * The folder sync info for the container need not have previously been cached.
	 * 
	 * @param container the container
	 * @param info the new folder sync info
	 */
	private static void setCachedFolderSync(IContainer container, FolderSyncInfo info) throws CVSException {
		try {
			if (info == null) info = NULL_FOLDER_SYNC_INFO;
			container.setSessionProperty(FOLDER_SYNC_KEY, info);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * If not already cached, loads and caches the folder ignores sync for the container.
	 * Folder must exist and must not be the workspace root.
	 * 
	 * @param container the container
	 * @return the folder ignore patterns, or an empty array if none
	 */
	private static String[] cacheFolderIgnores(IContainer container) throws CVSException {
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
	 * Sets the array of folder ignore patterns for the container, must not be null.
	 * Folder must exist and must not be the workspace root.
	 * 
	 * @param container the container
	 * @param ignores the array of ignore patterns
	 */
	private static void setCachedFolderIgnores(IContainer container, String[] ignores) throws CVSException {
		try {
			container.setSessionProperty(IGNORE_SYNC_KEY, ignores);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
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
			for (int i = 0; i < children.length; i++) {
				IResource child = children[i];
				if(getCachedResourceSync(child)==null) {
					possibleIgnores.add(child);
				}
				if(child.getType()!=IResource.FILE) {
					accumulateNonManagedChildren((IContainer)child, possibleIgnores);
				}
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
}