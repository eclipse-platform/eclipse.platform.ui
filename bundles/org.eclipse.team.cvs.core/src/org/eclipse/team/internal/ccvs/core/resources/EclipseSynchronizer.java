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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
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
	
	private static final FolderSyncInfo EMPTY_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false); //$NON-NLS-1$ //$NON-NLS-2$
	
	// the cvs eclipse synchronizer is a singleton
	private static EclipseSynchronizer instance;
	
	// track resources that have changed in a given operation
	private int nestingCount = 0;
	private Set changedResources = new HashSet();
	private Set changedFolders = new HashSet();
	
	private EclipseSynchronizer() {		
	}
	
	public static EclipseSynchronizer getInstance() {		
		return instance;
	}

	public void setFolderSync(IContainer folder, FolderSyncInfo info) throws CVSException {
		Assert.isNotNull(info);
		try {
			beginOperation(null);
			setCachedFolderSync(folder, info);
			changedFolders.add(folder);
		} finally {
			endOperation(null);
		}
	}
	
	public FolderSyncInfo getFolderSync(IContainer folder) throws CVSException {
		if (folder.getType() == IResource.ROOT) return null;					
		FolderSyncInfo info = getCachedFolderSync(folder);
		if (info == null && folder.exists()) {
			// read folder sync info and remember it
			// -- if none found then remember that fact for later
			info = SyncFileWriter.readFolderSync(CVSWorkspaceRoot.getCVSFolderFor(folder));
			if (info == null) info = EMPTY_FOLDER_SYNC_INFO;
			setCachedFolderSync(folder, info);
		}
		if (info == EMPTY_FOLDER_SYNC_INFO) info = null;
		return info;
	}	

	public void setResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		Assert.isNotNull(info);
		try {
			beginOperation(null);
			ensureChildResourceSyncLoaded(resource.getParent());
			setCachedResourceSync(resource, info);
			changedResources.add(resource);		
		} finally {
			endOperation(null);
		}
	}
	
	public ResourceSyncInfo getResourceSync(IResource resource) throws CVSException {
		if (resource.getType() == IResource.ROOT) return null;
		ensureChildResourceSyncLoaded(resource.getParent());
		return getCachedResourceSync(resource);
	}

	public void deleteFolderSync(IContainer folder, IProgressMonitor monitor) throws CVSException {
		try {
			beginOperation(null);
			// if the folder doesn't exist anymore the folder sync will be gone
			if(folder.exists()) {
				FolderSyncInfo info = getCachedFolderSync(folder);
				if (info != null && info != EMPTY_FOLDER_SYNC_INFO) {
					// remember that we deleted the folder sync info
					setCachedFolderSync(folder, EMPTY_FOLDER_SYNC_INFO);
					changedFolders.add(folder);
				}
			}
		} finally {
			endOperation(null);
		}
	}
	
	public void deleteResourceSync(IResource resource, IProgressMonitor monitor) throws CVSException {
		try {
			beginOperation(null);
			IContainer parent = resource.getParent();
			if(parent.exists()) {
				ensureChildResourceSyncLoaded(resource.getParent());
				setCachedResourceSync(resource, null);
				changedResources.add(resource);
			}
		} finally {
			endOperation(null);
		}
	}

	public String[] getIgnored(IContainer resource) throws CVSException {
		if(resource.getType()==IResource.ROOT) return null;
		String[] ignores = getCachedFolderIgnores(resource);
		if(ignores==null) {
			ignores = SyncFileWriter.readCVSIgnoreEntries(CVSWorkspaceRoot.getCVSFolderFor(resource));
			setCachedFolderIgnores(resource, ignores);
		}
		return ignores;
	}
	
	public void setIgnored(IContainer resource, String pattern) throws CVSException {
		try {
			SyncFileWriter.addCVSIgnoreEntry(CVSWorkspaceRoot.getCVSFolderFor(resource), pattern);
			String[] ignores = getIgnored(resource);
			String[] newIgnores = new String[ignores.length+1];
			System.arraycopy(ignores, 0, newIgnores, 0, ignores.length);
			newIgnores[ignores.length] = pattern;
			setCachedFolderIgnores(resource, newIgnores);
			// broadcast changes to unmanaged children - they are the only candidates for being ignored
			IResource[] children = resource.members();
			List possibleIgnores = new ArrayList();
			for (int i = 0; i < children.length; i++) {
				if(getCachedResourceSync(children[i])==null) {
					possibleIgnores.add(children[i]);
				}
			}
			TeamPlugin.getManager().broadcastResourceStateChanges((IResource[])possibleIgnores.toArray(new IResource[possibleIgnores.size()]));
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
		
	public IResource[] members(IContainer folder) throws CVSException {
		try {
			ensureChildResourceSyncLoaded(folder);
			HashMap children = (HashMap)folder.getSessionProperty(RESOURCE_SYNC_KEY);			
			Set childResources = new HashSet(children.keySet() /*IResource*/);
			childResources.addAll(Arrays.asList(folder.members()));
			return (IResource[])childResources.toArray(new IResource[childResources.size()]);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	static public void startup() {
		Assert.isTrue(instance==null);
		instance = new EclipseSynchronizer();	
	}
	
	static public void shutdown() {
	}
	
	public void beginOperation(IProgressMonitor monitor) throws CVSException {
		nestingCount += 1;
		if (nestingCount == 1) {
			// any work here?
		}		
	}
	
	public void endOperation(IProgressMonitor monitor) throws CVSException {
		if (nestingCount == 1) {	
			if (! changedFolders.isEmpty() || ! changedResources.isEmpty()) {
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
					monitor.subTask(Policy.bind("EclipseSynchronizer_updatingSyncEndOperation")); //$NON-NLS-1$
					
					/*** write sync info to disk ***/
					// folder sync info changes
					for(Iterator it = changedFolders.iterator(); it.hasNext();) {
						IContainer folder = (IContainer) it.next();
						if(folder.exists()) {
							FolderSyncInfo info = getCachedFolderSync(folder);
							if (info == EMPTY_FOLDER_SYNC_INFO) {
								// deleted folder sync info since we loaded it
								deleteSync(folder, dirtyParents, true);
							} else if (info == null) {
								// attempted to delete folder sync info for a previously unmanaged folder
								// no-op
							} else {
								// modified or created new folder sync info since we loaded it
								ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
								SyncFileWriter.writeFolderSync(cvsFolder, info);
							}
						}
						monitor.worked(1);
					}

					// update progress for parents we will skip because they were deleted
					monitor.worked(dirtyParents.size() - numDirty);

					// resource sync info changes
					for (Iterator it = dirtyParents.iterator(); it.hasNext();) {
						IContainer folder = (IContainer) it.next();
						if (folder.exists()) {
							// write sync info for all children in one go
							Assert.isTrue(isChildResourceSyncLoaded(folder));
							ResourceSyncInfo[] infos = getCachedResourceSyncForChildren(folder);
							ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
							SyncFileWriter.writeAllResourceSync(cvsFolder, infos);
						}
						monitor.worked(1);
					}
					
					/*** broadcast events ***/
					changedResources.addAll(changedFolders);				
					IResource[] resources = (IResource[]) changedResources.toArray(
						new IResource[changedResources.size()]);
					TeamPlugin.getManager().broadcastResourceStateChanges(resources);
					changedResources.clear();
					changedFolders.clear();									
				} finally {
					monitor.done();
				}
			}
		}
		nestingCount -= 1;
		Assert.isTrue(nestingCount>= 0);
	}
	
	
	/*
	 * Returns the cached resource sync info, or null if none found.
	 */
	private static ResourceSyncInfo getCachedResourceSync(IResource resource) throws CVSException {
		try {
			IContainer parent = resource.getParent();
			if(parent.exists()) {
				HashMap children = (HashMap)resource.getParent().getSessionProperty(RESOURCE_SYNC_KEY);
				if(children!=null) {
					return (ResourceSyncInfo)children.get(resource);
				}
			}
			return null;
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Returns the cached resource sync info for all children.
	 */
	private static ResourceSyncInfo[] getCachedResourceSyncForChildren(IContainer container) throws CVSException {
		try {
			if(container.exists()) {
				HashMap children = (HashMap)container.getSessionProperty(RESOURCE_SYNC_KEY);
				if(children!=null) {
					Collection syncInfo = children.values();
					return (ResourceSyncInfo[])syncInfo.toArray(new ResourceSyncInfo[syncInfo.size()]);			
				}
			}
			return new ResourceSyncInfo[0];
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Sets the cached resource sync info, use null to delete it.
	 */
	private static void setCachedResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		try {
			IContainer parent = resource.getParent();
			HashMap children = (HashMap)parent.getSessionProperty(RESOURCE_SYNC_KEY);
			if(children!=null) {
				if(info==null) {
					children.remove(resource);
				} else {
					// replace or add new resource sync
					children.put(resource, info);
				}
				parent.setSessionProperty(RESOURCE_SYNC_KEY, children);
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Returns the cached sync info for a folder, null if none found, or
	 * special placeholder EMPTY_FOLDER_SYNC_INFO for deleted sync info.
	 */
	private static FolderSyncInfo getCachedFolderSync(IContainer folder) throws CVSException {
		try {
			if(!folder.exists()) return null;
			return (FolderSyncInfo)folder.getSessionProperty(FOLDER_SYNC_KEY);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Sets the cached sync info for a folder, use null to flush, or special
	 * EMPTY_FOLDER_SYNC_INFO placeholder for deleted sync info.
	 */
	private static void setCachedFolderSync(IContainer folder, FolderSyncInfo info) throws CVSException {
		try {
			folder.setSessionProperty(FOLDER_SYNC_KEY, info);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}	
	
	private static String[] getCachedFolderIgnores(IContainer folder) throws CVSException {
		try {
			if(!folder.exists()) return null;
			return (String[])folder.getSessionProperty(IGNORE_SYNC_KEY);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	private static void setCachedFolderIgnores(IContainer folder, String[] ignores) throws CVSException {
		try {
			folder.setSessionProperty(IGNORE_SYNC_KEY, ignores);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Reads and caches the ResourceSyncInfos for this folder if not already cached.
	 */
	private static void ensureChildResourceSyncLoaded(IContainer folder) throws CVSException {
		try {
			// don't try to load if the information is already cached
			if (isChildResourceSyncLoaded(folder)) return;
			ResourceSyncInfo[] infos = SyncFileWriter.readAllResourceSync(CVSWorkspaceRoot.getCVSFolderFor(folder));
			HashMap children;
			if (infos != null) {
				children = new HashMap(infos.length);
				for (int i = 0; i < infos.length; i++) {
					ResourceSyncInfo syncInfo = infos[i];
					IResource peer;
					IPath path = new Path(syncInfo.getName());
					if (syncInfo.isDirectory()) {
						peer = folder.getFolder(path);
					} else {
						peer = folder.getFile(path);
					}
					children.put(peer, syncInfo);
				}
			} else {
				children = new HashMap(0);
			}
			folder.setSessionProperty(RESOURCE_SYNC_KEY, children);
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}		
	}

	private static boolean isChildResourceSyncLoaded(IContainer folder) throws CVSException {
		try {
			// root folder has no entries therefore info is always loaded
			if (folder.getType() == IResource.ROOT || !folder.exists()) return true;
			HashMap children = (HashMap)folder.getSessionProperty(RESOURCE_SYNC_KEY);
			return children!=null;
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Recursively deletes sync info from disk and cache.
	 * Also removes containers from set.
	 * 
	 * @param root the container to start at
	 * @param set the set from which to remove
	 * @param flushResourceSync must be 'true'
	 */
	private static void deleteSync(IContainer root, Set set, boolean flushResourceSync) throws CVSException {
		try {
			// delete sync info from set, disk and cache
			set.remove(root);
			if (root.exists()) {
				SyncFileWriter.getCVSSubdirectory(CVSWorkspaceRoot.getCVSFolderFor(root)).delete();
			}
			flushContainerSync(root, flushResourceSync);
			
			// recurse
			IResource[] children = root.members();
			for (int i = 0; i < children.length; i++) {
				IResource resource = children[i];
				if (resource.getType() != IResource.FILE) {
					deleteSync((IContainer) resource, set, false);
				}
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/**
	 * Recursively flushes sync info from cache.
	 * NOTE: made public to support test cases until refreshLocal added
	 * 
	 * @param root the container to start at
	 * @param flushResourceSync must be 'true'
	 */
	public static void flushSync(IContainer root, boolean flushResourceSync) throws CVSException {
		try {
			// delete sync info from cache
			flushContainerSync(root, flushResourceSync);
			
			// recurse
			IResource[] children = root.members();
			for (int i = 0; i < children.length; i++) {
				if(children[i].getType()!=IResource.FILE) {;
					flushSync((IContainer) children[i], false);					
				}
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	private static void flushContainerSync(IContainer container, boolean flushResourceSync) throws CVSException {
		try {
			if(container.exists()) {
				if (flushResourceSync) {
					setCachedResourceSync(container, null);
				}
				container.setSessionProperty(RESOURCE_SYNC_KEY, null);
				container.setSessionProperty(IGNORE_SYNC_KEY, null);
				container.setSessionProperty(FOLDER_SYNC_KEY, null);
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}	
}