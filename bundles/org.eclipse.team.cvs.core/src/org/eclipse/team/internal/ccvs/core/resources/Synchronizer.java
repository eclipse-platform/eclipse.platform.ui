package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.ResourceDeltaVisitor;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;

/**
 * A singleton that provides access to CVS specific information about local CVS 
 * resources.
 */
public class Synchronizer {
	
	private static Synchronizer instance;
	private static Cache cache;
	
	private class SyncFile {
		long timestamp = 0;
		Map config = new HashMap(10);
	}		
	
	private class Cache {
	
		// keys = SyncFile values = hashMap of keys = name values = ResourceSyncInfo
		private Map entriesCache = new HashMap(10);
		
		// keys = File values = FolderSyncInfo
		private Map folderConfigCache = new HashMap(10);
		
		// entry files that are modified in memory but have not been saved
		private Set invalidatedEntryFiles = new HashSet(10);
		private Set invalidatedFolderConfigs = new HashSet(10);
		
		protected void broadcastSyncChange(File file) throws CVSException {	
			IResource resource;
			if(file.isDirectory()) {
				resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(new Path(file.getAbsolutePath()));
			} else {
				resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
			}
			
			if(resource!=null) {			
				try {
					resource.refreshLocal(IResource.DEPTH_ZERO, null);
				} catch(CoreException e) {
					throw new CVSException("Error refreshing file from local contents " + file.getAbsolutePath(), e);
				}
				TeamPlugin.getManager().broadcastResourceStateChanges(new IResource[] {resource});
			}
		}
		
		public FolderSyncInfo getFolderSync(File file, boolean reload) throws CVSException {
			if(!file.isDirectory()) { 
				return null;
			}
			SyncFile folderSync = (SyncFile)folderConfigCache.get(file);
			if(folderSync==null || reload) {
				folderSync = new SyncFile();
				folderSync.config.put(file.getName(), SyncFileUtil.readFolderConfig(file));
				folderConfigCache.put(file, folderSync);
			}
			return (FolderSyncInfo)folderSync.config.get(file.getName());			
		}
		
		public ResourceSyncInfo getResourceSync(File file, boolean reload) throws CVSException {
			File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(file.getParentFile()), SyncFileUtil.ENTRIES);
			SyncFile entriesSync = (SyncFile)entriesCache.get(entriesFile);
			
			// read entries file
			if(entriesSync==null || reload) {
				entriesSync = (SyncFile)entriesCache.get(entriesFile);
				
				entriesSync = new SyncFile();
				ResourceSyncInfo infos[] = SyncFileUtil.readEntriesFile(file.getParentFile());
				for (int i = 0; i < infos.length; i++) {
					entriesSync.config.put(infos[i].getName(), infos[i]);
				}
				entriesCache.put(entriesFile, entriesSync);
			}
			return (ResourceSyncInfo)entriesSync.config.get(file.getName());
		}

		public void setFolderSync(File folder, FolderSyncInfo info) throws CVSException {
						
			// if parent has the sync folder (e.g. CVS) then ensure that the directory
			// entry for this folder is added.
			if(SyncFileUtil.getCVSSubdirectory(folder.getParentFile()).exists()) {
				ResourceSyncInfo resourceInfo = new ResourceSyncInfo(folder.getName(), true);
				setResourceSync(folder, resourceInfo);
			}
			
			// there can only be one sync entry for folders, create a new one
			folderConfigCache.remove(folder);
			SyncFile folderSync = new SyncFile();
			folderSync.config.put(folder.getName(), info);
			folderConfigCache.put(folder, folderSync);
			invalidatedFolderConfigs.add(folder);			
			broadcastSyncChange(folder);
		}

		public void setResourceSync(File file, ResourceSyncInfo info) throws CVSException {
			File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(file.getParentFile()), SyncFileUtil.ENTRIES);
									
			SyncFile entriesSync = (SyncFile)entriesCache.get(entriesFile);
			if(entriesSync==null) {
				entriesSync = new SyncFile();
				entriesCache.put(entriesFile, entriesSync);
			}			
			entriesSync.config.put(info.getName(), info);						
			invalidatedEntryFiles.add(entriesFile);
			broadcastSyncChange(file);
		}
		
		public void deleteResourceSync(File file) throws CVSException {
			File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(file.getParentFile()), SyncFileUtil.ENTRIES);
			SyncFile entriesSync = (SyncFile)entriesCache.get(entriesFile);
			if(entriesSync!=null) {
				entriesSync.config.remove(file.getName());
				invalidatedEntryFiles.add(entriesFile);
				broadcastSyncChange(file);
			}			
		}
		
		public void save() throws CVSException {
			for (Iterator it = invalidatedEntryFiles.iterator(); it.hasNext();) {
				File entryFile = (File) it.next();
				SyncFile info = (SyncFile)entriesCache.get(entryFile);
								
				// collect all sync infos for this entries file
				List syncInfos = new ArrayList();
				for (Iterator it2 = info.config.values().iterator(); it2.hasNext();) {										
					ResourceSyncInfo element = (ResourceSyncInfo) it2.next();
					syncInfos.add(element);					
				}
				
				if(!entryFile.exists()) {
					try {
						entryFile.getParentFile().mkdirs();
						entryFile.createNewFile();
					} catch(IOException e) {
						throw new CVSException("Error creating " + entryFile.getAbsolutePath(), e);
					}
				}				
				
				SyncFileUtil.writeEntriesFile(entryFile, (ResourceSyncInfo[]) syncInfos.toArray(new ResourceSyncInfo[syncInfos.size()]));
				info.timestamp = entryFile.lastModified();
				
				File[] entrySyncFiles = SyncFileUtil.getEntrySyncFiles(entryFile.getParentFile().getParentFile());
				for (int i = 0; i < entrySyncFiles.length; i++) {
					broadcastSyncChange(entrySyncFiles[i]);					
				}				
			}
			
			for (Iterator it = invalidatedFolderConfigs.iterator(); it.hasNext();) {
				File folder = (File) it.next();
				SyncFile info = (SyncFile)folderConfigCache.get(folder);
				
				// if it was invalidated there should be something to write
				Assert.isNotNull(info);
				
				SyncFileUtil.writeFolderConfig(folder, (FolderSyncInfo)info.config.get(folder.getName()));
				File rootFile = new File(SyncFileUtil.getCVSSubdirectory(folder), SyncFileUtil.ROOT);
				info.timestamp = rootFile.lastModified();
				File[] folderSyncFiles = SyncFileUtil.getFolderSyncFiles(folder);
				for (int i = 0; i < folderSyncFiles.length; i++) {
					broadcastSyncChange(folderSyncFiles[i]);					
				}				
			}
			
			// clear invalidated 
			invalidatedEntryFiles = new HashSet(10);
			invalidatedFolderConfigs = new HashSet(10);
		}
		
		public void clear() {
			entriesCache.clear();
			folderConfigCache.clear();
			
			// Program error if clear is called with pending changes.
			Assert.isTrue(!invalidatedEntryFiles.isEmpty() || !invalidatedFolderConfigs.isEmpty());
						
			invalidatedEntryFiles.clear();
			invalidatedFolderConfigs.clear();
		}
	}
		
	private Synchronizer() {
		cache = new Cache();
	}
	
	public static Synchronizer getInstance() {
		if(instance==null) {
			instance = new Synchronizer();
		}
		return instance;
	}		

	public void save() throws CVSException {
		cache.save();
	}
	
	/**
	 * Reload the sync information from disk starting at this folder.
	 */
	public void reload(ICVSFolder folder, IProgressMonitor monitor) throws CVSException {
		if (folder instanceof LocalFolder) {
			LocalFolder fsFolder = (LocalFolder) folder;
			File file = fsFolder.getLocalFile();
			cache.getFolderSync(file, true);
			
			ICVSFile[] files = folder.getFiles();
			for (int i = 0; i < files.length; i++) {
				ICVSFile iCVSFile = files[i];
				cache.getResourceSync(((LocalFile)iCVSFile).getLocalFile(), true);
				break;
			}
			monitor.worked(1);
			
			ICVSFolder[] folders = folder.getFolders();
			for (int i = 0; i < folders.length; i++) {
				ICVSFolder iCVSFolder = folders[i];
				reload(iCVSFolder, monitor);
			}
		}
	}

	/**
	 * Resource sync info
	 */
	
	public ResourceSyncInfo getSyncInfo(File file) throws CVSException {
		return cache.getResourceSync(file, false);
	}
	
	public void setSyncInfo(File file, ResourceSyncInfo info) throws CVSException {
		cache.setResourceSync(file, info);
	}
	
	public void deleteSyncInfo(File file) throws CVSException {
		cache.deleteResourceSync(file);
	}
	
	/**
	 * Folder sync info
	 */
	
	public FolderSyncInfo getFolderSyncInfo(File folder) throws CVSException {
		if(folder.isDirectory()) {
			return cache.getFolderSync(folder, false);
		} else {
			return null;
		}
	}

	public void setFolderSyncInfo(File folder, FolderSyncInfo info) throws CVSException {
		cache.setFolderSync(folder, info);
	}
	
	/**
	 * Utils
	 */
	
	public ResourceSyncInfo[] members(File file) throws CVSException {
		if(file.isDirectory()) {
			return SyncFileUtil.readEntriesFile(file);
		} else {
			return new ResourceSyncInfo[0];
		}
	}	
}