package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.ISynchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.ccvs.core.CVSTag;
import org.eclipse.team.ccvs.core.ICVSFile;
import org.eclipse.team.ccvs.core.ICVSFolder;
import org.eclipse.team.ccvs.core.ICVSResource;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.core.sync.ISyncProvider;
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
 * [Notes:
 *  1. how can we expire cache elements and purge to safe memory?
 *  2. how can we safeguard against overwritting meta files changes made outside of Eclipse? I'm
 *     not sure we should force setting file contents in EclipseFile handles?
 *  4. how do we reload
 * ]
 * 
 * @see ResourceSyncInfo
 * @see FolderSyncInfo
 */
public class EclipseSynchronizer {
	// the resources plugin synchronizer is used to cache and possibly persist. These 
	// are keys for storing the sync info.
	private static final QualifiedName FOLDER_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-sync");
	private static final QualifiedName RESOURCE_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "resource-sync");
	private static final QualifiedName RESOURCE_SYNC_LOADED_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-has-loaded-childsync");
	private static final QualifiedName IGNORE_SYNC_KEY = new QualifiedName(CVSProviderPlugin.ID, "folder-ignore");
	
	private static final byte[] EMPTY_BYTES = new byte[0];
	private static final FolderSyncInfo EMPTY_FOLDER_SYNC_INFO = new FolderSyncInfo("", "", null, false);
	
	// the cvs eclipse synchronizer is a singleton
	private static EclipseSynchronizer instance;
	
	// track resources that have changed in a given operation
	private int nestingCount = 0;
	private Set changedResources = new HashSet();
	private Set changedFolders = new HashSet();
	
	private static final boolean USE_PHANTOMS = false;
	
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
			info = SyncFileWriter.readFolderConfig(CVSWorkspaceRoot.getCVSFolderFor(folder));
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
			FolderSyncInfo info = getCachedFolderSync(folder);
			if (info != null && info != EMPTY_FOLDER_SYNC_INFO) {
				// remember that we deleted the folder sync info
				setCachedFolderSync(folder, EMPTY_FOLDER_SYNC_INFO);
				changedFolders.add(folder);
			}
		} finally {
			endOperation(null);
		}
	}
	
	public void deleteResourceSync(IResource resource, IProgressMonitor monitor) throws CVSException {
		try {
			beginOperation(null);
			ensureChildResourceSyncLoaded(resource.getParent());
			setCachedResourceSync(resource, null);
			changedResources.add(resource);
		} finally {
			endOperation(null);
		}
	}

	public String[] getIgnored(IResource resource) throws CVSException {
		IContainer parent = resource.getParent();
		if(parent==null || parent.getType()==IResource.ROOT) return null;
		String[] ignores = getCachedFolderIgnores(parent);
		if(ignores==null) {
			ICVSFile ignoreFile = CVSWorkspaceRoot.getCVSFileFor(parent.getFile(new Path(SyncFileWriter.IGNORE_FILE)));
			if(ignoreFile.exists()) {
				ignores = SyncFileWriter.readLines(ignoreFile);
				setCachedFolderIgnores(parent, ignores);
			}
		}
		return ignores;
	}
	
	public void setIgnored(IResource resource, String pattern) throws CVSException {
		SyncFileWriter.addCvsIgnoreEntry(CVSWorkspaceRoot.getCVSResourceFor(resource), pattern);
		TeamPlugin.getManager().broadcastResourceStateChanges(new IResource[] {resource});
	}
		
	public IResource[] members(IContainer folder) throws CVSException {
		try {
			// initialize cache if needed, this will create phantoms
			ensureChildResourceSyncLoaded(folder);
			IResource[] children = folder.members(true);
			List list = new ArrayList(children.length);
			for (int i = 0; i < children.length; ++i) {
				IResource child = children[i];
				// return phantoms for files only, until we can handle phantoms for files.
				if(child.isPhantom()) {
					// it's a phantom because it has CVS sync info and not another plugins sync
					if(child.getType()==IResource.FILE && getCachedResourceSync(child) != null) {
						list.add(child); 
					}
				} else {
					list.add(child);
				}
			}
			return (IResource[]) list.toArray(new IResource[list.size()]);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	static public void startup() {
		Assert.isTrue(instance==null);
		instance = new EclipseSynchronizer();	
		getSynchronizer().add(RESOURCE_SYNC_KEY);
		getSynchronizer().add(RESOURCE_SYNC_LOADED_KEY);
		getSynchronizer().add(FOLDER_SYNC_KEY);
		getSynchronizer().add(IGNORE_SYNC_KEY);
		try {
			flushAll(ResourcesPlugin.getWorkspace().getRoot(), false /*don't purge from disk*/);
		} catch(CVSException e) {
			//	// severe problem, it would mean that we are working with stale sync info
			CVSProviderPlugin.log(e.getStatus());
		}					
	}
	
	static public void shutdown() {
		// so that the workspace won't persist cached sync info
		getSynchronizer().remove(RESOURCE_SYNC_KEY);
		getSynchronizer().remove(RESOURCE_SYNC_LOADED_KEY);
		getSynchronizer().remove(FOLDER_SYNC_KEY);
		getSynchronizer().remove(IGNORE_SYNC_KEY);
	}
	
	public void beginOperation(IProgressMonitor monitor) throws CVSException {
		nestingCount += 1;
		if (nestingCount == 1) {
			// any work here?
			
			// uncomment this line to bypass cache for testing
			//flushAll(ResourcesPlugin.getWorkspace().getRoot(), false /*don't purge from disk*/);			
		}		
	}
	
	public void endOperation(IProgressMonitor monitor) throws CVSException {
		if (nestingCount == 1) {	
			if (! changedFolders.isEmpty() || ! changedResources.isEmpty()) {
				try {
					monitor = Policy.monitorFor(monitor);
					int numResources = changedFolders.size() + changedResources.size();
					monitor.beginTask(null, numResources);
					monitor.subTask("Updating CVS synchronization information...");
					/* write sync info to disk */
					List deletedFolderSync = new ArrayList();

					// folder sync info changes
					for(Iterator it = changedFolders.iterator(); it.hasNext();) {
						IContainer folder = (IContainer) it.next();
						FolderSyncInfo info = getCachedFolderSync(folder);
						ICVSFolder cvsFolder = CVSWorkspaceRoot.getCVSFolderFor(folder);
						if (info == EMPTY_FOLDER_SYNC_INFO) {
							// deleted folder sync info since we loaded it, postpone change until later
							deletedFolderSync.add(folder);
						} else if (info == null) {
							// attempted to delete folder sync info for a previously unmanaged folder
							// no-op
						} else {
							// modified or created new folder sync info since we loaded it
							SyncFileWriter.writeFolderConfig(cvsFolder, info);
						}
						monitor.worked(1);
					}

					// resource sync info changes
					Set parentsWithDirtyEntries = new HashSet();
					for(Iterator it = changedResources.iterator(); it.hasNext();) {
						IResource resource = (IResource) it.next();
						ResourceSyncInfo info = getCachedResourceSync(resource);
						ICVSResource cvsResource = CVSWorkspaceRoot.getCVSResourceFor(resource);
						Assert.isTrue(isChildResourceSyncLoaded(resource.getParent()));
						// if the parent doesn't exists there is no point writing out the sync info
						// to disk.
						if(cvsResource.getParent().exists()) {
							parentsWithDirtyEntries.add(cvsResource.getParent());
							if (info == null) {
								// deleted resource sync info since we loaded it
								SyncFileWriter.deleteSync(cvsResource);
							} else {
								// modified or created new resource sync info since we loaded it
								SyncFileWriter.writeResourceSync(cvsResource, info);
							}
							monitor.worked(1);
						}
					}
					
					// folder sync info deletes
					for(Iterator it = deletedFolderSync.iterator(); it.hasNext();) {
						IContainer folder = (IContainer) it.next();
						// flush everything that was cached from the CVS subdirectory
						flushAll(folder, true /*purge from disk*/);
					}
					
					// merge entry file to ensure that the Entries.log file doesn't 
					for(Iterator it = parentsWithDirtyEntries.iterator(); it.hasNext();) {
						SyncFileWriter.mergeEntriesLogFiles((ICVSFolder)it.next());
					}
					
					// broadcast events
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
	
	private static ISynchronizer getSynchronizer() {
		return ResourcesPlugin.getWorkspace().getSynchronizer();
	}
	
	/*
	 * Returns the cached resource sync info, or null if none found.
	 */
	private ResourceSyncInfo getCachedResourceSync(IResource resource) throws CVSException {
		try {
			byte[] bytes = getSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, resource);
			if(bytes == null) return null;
			return new ResourceSyncInfo(new String(bytes), null, null);
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Sets the cached resource sync info, use null to delete it.
	 */
	private void setCachedResourceSync(IResource resource, ResourceSyncInfo info) throws CVSException {
		try {
			if(info==null) {
				getSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, resource, null); // faster than flush
			} else {
				getSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, resource, info.getEntryLine(true).getBytes());
			}
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Returns the cached sync info for a folder, null if none found, or
	 * special placeholder EMPTY_FOLDER_SYNC_INFO for deleted sync info.
	 */
	private FolderSyncInfo getCachedFolderSync(IContainer folder) throws CVSException {
		try {
			byte[] bytes = getSynchronizer().getSyncInfo(FOLDER_SYNC_KEY, folder);
			if (bytes == null) return null;
			if (bytes.length == 0) return EMPTY_FOLDER_SYNC_INFO; // return placeholder for deleted sync info
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
			String repo = is.readUTF();
			String root = is.readUTF();
			String tag = is.readUTF();
			CVSTag cvsTag = null;
			boolean isStatic = is.readBoolean();
			if(!tag.equals("null")) {
				cvsTag = new CVSEntryLineTag(tag);
			}
			return new FolderSyncInfo(repo, root, cvsTag, isStatic);
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} catch(IOException e) {
			throw CVSException.wrapException(e);
		}	
	}
	
	/*
	 * Sets the cached sync info for a folder, use null to flush, or special
	 * EMPTY_FOLDER_SYNC_INFO placeholder for deleted sync info.
	 */
	private void setCachedFolderSync(IContainer folder, FolderSyncInfo info) throws CVSException {
		try {
			if (info == null) {
				getSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, folder, null); // faster than flush
			} else if (info == EMPTY_FOLDER_SYNC_INFO ) {
				// memorize placeholder for deleted sync info
				getSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, folder, EMPTY_BYTES);
			} else {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream os = new DataOutputStream(bos);
				os.writeUTF(info.getRepository());
				os.writeUTF(info.getRoot());
				CVSEntryLineTag tag = info.getTag();
				if(tag==null) {
					os.writeUTF("null");
				} else {
					os.writeUTF(info.getTag().toEntryLineFormat(false));
				}
				os.writeBoolean(info.getIsStatic());				
				getSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, folder, bos.toByteArray());
				os.close();
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} catch(IOException e) {
			throw CVSException.wrapException(e);
		}
	}	
	
	private String[] getCachedFolderIgnores(IContainer folder) throws CVSException {
		try {
			byte[] bytes = getSynchronizer().getSyncInfo(IGNORE_SYNC_KEY, folder);
			if (bytes == null) return null;
			DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));
			int count = is.readInt();
			String[] ignoreList = new String[count];
			for(int i = 0; i < count; ++i) {
				ignoreList[i] = is.readUTF();
			}
			return ignoreList;
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} catch(IOException e) {
			throw CVSException.wrapException(e);
		}				
	}
	
	private void setCachedFolderIgnores(IContainer folder, String[] ignores) throws CVSException {
		try {
			if (ignores == null) {
				getSynchronizer().setSyncInfo(IGNORE_SYNC_KEY, folder, null); // faster than flush
			} else {
				// a zero-length array indicates there were no ignores found
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				DataOutputStream os = new DataOutputStream(bos);
				os.writeInt(ignores.length);
				for(int i = 0; i < ignores.length; ++i) {
					os.writeUTF(ignores[i]);
				}
				getSynchronizer().setSyncInfo(IGNORE_SYNC_KEY, folder, bos.toByteArray());
				os.close();
			}
		} catch (CoreException e) {
			throw CVSException.wrapException(e);
		} catch(IOException e) {
			throw CVSException.wrapException(e);
		}
	}
	
	/*
	 * Reads and caches the ResourceSyncInfos for this folder if not already cached.
	 */
	private void ensureChildResourceSyncLoaded(IContainer folder) throws CVSException {
		// don't try to load if the information is already cached
		if (isChildResourceSyncLoaded(folder)) return;
		ResourceSyncInfo[] infos = SyncFileWriter.readEntriesFile(CVSWorkspaceRoot.getCVSFolderFor(folder));
		if (infos != null) {
			for (int i = 0; i < infos.length; i++) {
				ResourceSyncInfo syncInfo = infos[i];
				IResource peer;
				IPath path = new Path(syncInfo.getName());
				if (syncInfo.isDirectory()) {
					peer = folder.getFolder(path);
				} else {
					peer = folder.getFile(path);
				}
				// may create a phantom if the sibling resource does not exist.
				setCachedResourceSync(peer, syncInfo);
			}
		}
		setChildResourceSyncLoaded(folder, true);
	}

	private boolean isChildResourceSyncLoaded(IContainer folder) throws CVSException {
		try {
			// root folder has no entries therefore info is always loaded
			if (folder.getType() == IResource.ROOT || ! folder.exists()) return true;
			return getSynchronizer().getSyncInfo(RESOURCE_SYNC_LOADED_KEY, folder) != null;
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}		
	}
	
	private void setChildResourceSyncLoaded(IContainer folder, boolean isLoaded) throws CVSException {
		try {
			getSynchronizer().setSyncInfo(RESOURCE_SYNC_LOADED_KEY, folder, isLoaded ? EMPTY_BYTES : null);
		} catch(CoreException e) {
			throw CVSException.wrapException(e);
		}		

	}

	private void flushChildResourceSync(IContainer folder, int depth) throws CVSException {
		// flushSyncInfo fails with an exception if the folder does not exist
		if (! folder.exists()) return;
		try {
			byte[] folderSyncBytes = getSynchronizer().getSyncInfo(RESOURCE_SYNC_KEY, folder);
			getSynchronizer().flushSyncInfo(RESOURCE_SYNC_KEY, folder, depth);
			getSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, folder, folderSyncBytes);
		} catch(CoreException e) {
			throw CVSException.wrapException(folder, "Error flushing a folder's children sync", e);
		}
	}
	
	static public void flushAll(final IContainer root, final boolean purgeFromDisk) throws CVSException {
		if (! (root.exists() || root.isPhantom())) return;
		try {
			// purge sync information from children
			root.accept(new IResourceVisitor() {
				public boolean visit(IResource resource) throws CoreException {					
					if(! root.equals(resource)) {
						// don't clear resource sync on root since it might still be managed
						// by its parent
						getSynchronizer().setSyncInfo(RESOURCE_SYNC_KEY, resource, null);
					}
					if(resource.getType()!=IResource.FILE) {
						IContainer folder = (IContainer) resource;
						getSynchronizer().setSyncInfo(RESOURCE_SYNC_LOADED_KEY, folder, null);
						getSynchronizer().setSyncInfo(FOLDER_SYNC_KEY, folder, null);
						getSynchronizer().setSyncInfo(IGNORE_SYNC_KEY, folder, null);					
						if(purgeFromDisk && root.exists()) {
							SyncFileWriter.deleteCVSSubDirectory(folder);
						}
					}
					return true;					
				}

			}, IResource.DEPTH_INFINITE, true /*include phantoms*/);
		} catch(CoreException e) {
			throw CVSException.wrapException(root, "Problems occured flushing synchronizer cache", e);
		}
	}
}