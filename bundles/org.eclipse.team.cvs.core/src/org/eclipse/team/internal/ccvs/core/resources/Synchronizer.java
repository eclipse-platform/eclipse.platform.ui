package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;

/**
 * A singleton that provides access to CVS synchronization information about local CVS 
 * resources. 
 */
public class Synchronizer {
	
	private static Synchronizer instance;

	private class SyncFile {
		long timestamp = 0;
		Map config = new HashMap(10);
	}		
	
	// keys = SyncFile values = hashMap of keys = name values = ResourceSyncInfo
	private Map entriesCache = new HashMap(10);
		
	// keys = File values = FolderSyncInfo
	private Map folderConfigCache = new HashMap(10);
	
	// entry files that are modified in memory but have not been saved
	private Set invalidatedEntryFiles = new HashSet(10);
	private Set invalidatedFolderConfigs = new HashSet(10);
		
	private Synchronizer() {
	}
	
	public static Synchronizer getInstance() {
		if(instance==null) {
			instance = new Synchronizer();
		}
		return instance;
	}		

	protected void broadcastSyncChange(File file) throws CVSException {	
		IResource resource;
		int depth = IResource.DEPTH_ZERO;
		
		if(!file.exists()) {
			file = file.getParentFile();
			depth = IResource.DEPTH_ONE;
		}
		
		if(file.isDirectory()) {
			resource = ResourcesPlugin.getWorkspace().getRoot().getContainerForLocation(new Path(file.getAbsolutePath()));
		} else {
			resource = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(new Path(file.getAbsolutePath()));
		}
			
		if(resource!=null) {			
			try {
				resource.refreshLocal(depth, null);
			} catch(CoreException e) {
				throw new CVSException("Error refreshing file from local contents " + file.getAbsolutePath(), e);
			}
			TeamPlugin.getManager().broadcastResourceStateChanges(new IResource[] {resource});
		}
	}
	
	public FolderSyncInfo getFolderSync(File file) throws CVSException {
		return getFolderSync(file, false);
	}
	
	public ResourceSyncInfo getResourceSync(File file) throws CVSException {
		return getResourceSync(file, false);		
	}
	
	protected FolderSyncInfo getFolderSync(File file, boolean reload) throws CVSException {
		if(!file.isDirectory()) { 
			return null;
		}
		SyncFile folderSync = (SyncFile)folderConfigCache.get(file);
		if(folderSync==null || reload) {
			folderSync = new SyncFile();
			FolderSyncInfo info = SyncFileUtil.readFolderConfig(file);
			
			// no CVS sub-directory
			if(info==null) {
				return null;
			}
			folderSync.config.put(file.getName(), info);
			folderConfigCache.put(file, folderSync);
		}
		return (FolderSyncInfo)folderSync.config.get(file.getName());			
	}
	
	protected ResourceSyncInfo getResourceSync(File file, boolean reload) throws CVSException {
		File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(file.getParentFile()), SyncFileUtil.ENTRIES);
		SyncFile entriesSync = (SyncFile)entriesCache.get(entriesFile);
		
		// read entries file
		if(entriesSync==null || reload) {
			entriesSync = new SyncFile();
			ResourceSyncInfo infos[] = SyncFileUtil.readEntriesFile(file.getParentFile());
			for (int i = 0; i < infos.length; i++) {
				entriesSync.config.put(infos[i].getName(), infos[i]);
			}
			entriesCache.put(entriesFile, entriesSync);
		}
		return (ResourceSyncInfo)entriesSync.config.get(file.getName());
	}

	protected ResourceSyncInfo[] getResourceSyncForFolder(File folder) throws CVSException {
		File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(folder), SyncFileUtil.ENTRIES);
		SyncFile entriesSync = (SyncFile)entriesCache.get(entriesFile);		
		if(entriesSync==null) {
			getResourceSync(new File(folder, "dummy"), true);
			entriesSync = (SyncFile)entriesCache.get(entriesFile);
		}
		Collection entries = entriesSync.config.values();
		return (ResourceSyncInfo[])entries.toArray(new ResourceSyncInfo[entries.size()]);
	}
	
	public void setFolderSync(File folder, FolderSyncInfo info) throws CVSException {
		
		Assert.isNotNull(info);
					
		// if parent has the sync folder (e.g. CVS) then ensure that the directory
		// entry for this folder is added.
		if(getFolderSync(folder.getParentFile())!=null) {
			ResourceSyncInfo resourceInfo = new ResourceSyncInfo(folder.getName());
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
		
		Assert.isNotNull(info);
		
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
	
	public void deleteFolderSync(File folder) throws CVSException {
		clearFolder(folder);
		broadcastSyncChange(folder);
	}
	
	protected void clearFolder(File folder) throws CVSException {
		
		// remove resource sync entries file from the cache
		File entriesFile = new File(SyncFileUtil.getCVSSubdirectory(folder), SyncFileUtil.ENTRIES);
		entriesCache.remove(entriesFile);
		
		// remove from parent
		deleteResourceSync(folder);
		
		// remove folder sync
		folderConfigCache.remove(folder);
	}
	
	public void deleteFolderSyncDeep(File folder) throws CVSException {
		Assert.isTrue(folder.isDirectory());
		
		File[] childDirs = folder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		});
		
		for (int i = 0; i < childDirs.length; i++) {
			File file = childDirs[i];
			deleteFolderSyncDeep(file);			
		}
		
		clearFolder(folder);
	}
	
	public void save() throws CVSException {
		for (Iterator it = invalidatedEntryFiles.iterator(); it.hasNext();) {
			File entryFile = (File) it.next();
			SyncFile info = (SyncFile)entriesCache.get(entryFile);
			
			// entry file may of been deleted from cache by a client calling
			// deleteFolderSync (e.g. pruning on update).
			if(info!=null) {
							
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
		}
		
		for (Iterator it = invalidatedFolderConfigs.iterator(); it.hasNext();) {
			File folder = (File) it.next();
			SyncFile info = (SyncFile)folderConfigCache.get(folder);
			
			// folder config may of been deleted from cache by a client calling
			// deleteFolderSync (e.g. pruning on update).
			if(info!=null) {	
				SyncFileUtil.writeFolderConfig(folder, (FolderSyncInfo)info.config.get(folder.getName()));
				File rootFile = new File(SyncFileUtil.getCVSSubdirectory(folder), SyncFileUtil.ROOT);
				info.timestamp = rootFile.lastModified();
				File[] folderSyncFiles = SyncFileUtil.getFolderSyncFiles(folder);
				for (int i = 0; i < folderSyncFiles.length; i++) {
					broadcastSyncChange(folderSyncFiles[i]);					
				}				
			}
		}
		
		// clear invalidated 
		invalidatedEntryFiles = new HashSet(10);
		invalidatedFolderConfigs = new HashSet(10);
	}
	
	public void clearAll() {
		entriesCache.clear();
		folderConfigCache.clear();
		
		// Program error if clear is called with pending changes.
		Assert.isTrue(!invalidatedEntryFiles.isEmpty() || !invalidatedFolderConfigs.isEmpty());
					
		invalidatedEntryFiles.clear();
		invalidatedFolderConfigs.clear();
	}
	
	/**
	 * Reload the sync information from disk starting at this folder.
	 */
	public void reload(ICVSFolder folder, IProgressMonitor monitor) throws CVSException {
		if (folder instanceof LocalFolder) {
			LocalFolder fsFolder = (LocalFolder) folder;
			File file = fsFolder.getLocalFile();
			getFolderSync(file, true);
			
			ICVSFile[] files = folder.getFiles();
			for (int i = 0; i < files.length; i++) {
				ICVSFile iCVSFile = files[i];
				getResourceSync(((LocalFile)iCVSFile).getLocalFile(), true);
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
	
	public void clear(ICVSFolder folder, IProgressMonitor monitor) throws CVSException {
		if (folder instanceof LocalFolder) {
			LocalFolder fsFolder = (LocalFolder) folder;
			deleteFolderSync(fsFolder.getLocalFile());
			
			monitor.worked(1);
			
			ICVSFolder[] folders = folder.getFolders();
			for (int i = 0; i < folders.length; i++) {
				clear(folders[i], monitor);
			}
		}	
	}

	/**
	 * Utils
	 */
	
	public ResourceSyncInfo[] members(File folder) throws CVSException {
		if(folder.isDirectory()) {
			return getResourceSyncForFolder(folder);
		} else {
			return new ResourceSyncInfo[0];
		}
	}	
}