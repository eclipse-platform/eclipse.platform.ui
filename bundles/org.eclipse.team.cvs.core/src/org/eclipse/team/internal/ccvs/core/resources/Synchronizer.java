package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.SyncFileUtil;
import sun.rmi.transport.Utils;

/**
 * A singleton that provides access to CVS specific information about local CVS 
 * resources.
 */
public class Synchronizer {
	
	private static Synchronizer instance;
	
	private Synchronizer() {
	}
	
	public static Synchronizer getInstance() {
		if(instance==null) {
			instance = new Synchronizer();
		}
		return instance;
	}		

	public ResourceSyncInfo getSyncInfo(File file) throws CVSException {
		ResourceSyncInfo[] infos = SyncFileUtil.readEntriesFile(file.getParentFile());
		for (int i = 0; i < infos.length; i++) {
			if(infos[i].getName().equals(file.getName())) {
				return infos[i];	
			}		
		}
		return null;
	}
	
	public void setSyncInfo(File file, ResourceSyncInfo info) throws CVSException {
		List infos = new ArrayList();
		infos.addAll(Arrays.asList(SyncFileUtil.readEntriesFile(file.getParentFile())));
		infos.remove(info);
		infos.add(info);
		
		SyncFileUtil.writeEntriesFile(file.getParentFile(), (ResourceSyncInfo[]) infos.toArray(new ResourceSyncInfo[infos.size()]));
	}
	
	public void flushSyncInfo(File file, int depth) throws CVSException {
		
		ResourceSyncInfo[] infos = SyncFileUtil.readEntriesFile(file.getParentFile());
		// If the length of infos is 0, the parent is not a managed folder so we don't 
		// need to do anything unless we actually perform the deep operation.
		if (infos.length > 0) {
			List result = new ArrayList(infos.length - 1);
			
			for (int i = 0; i < infos.length; i++) {
				if (!infos[i].getName().equals(file.getName())) {
					result.add(infos[i]);
				}
			}
			
			SyncFileUtil.writeEntriesFile(file.getParentFile(), (ResourceSyncInfo[]) result.toArray(new ResourceSyncInfo[result.size()]));
		}
	}
	
	public FolderSyncInfo getFolderSyncInfo(File file) throws CVSException {
		if(file.isDirectory()) {
			return SyncFileUtil.readFolderConfig(file);
		} else {
			return null;
		}
	}

	public void setFolderSyncInfo(File file, FolderSyncInfo info) throws CVSException {
		Assert.isTrue(file.isDirectory());
		
		// if parent has the sync folder (e.g. CVS) then ensure that the directory
		// entry for this folder is added.
		if(SyncFileUtil.getCVSSubdirectory(file.getParentFile()).exists()) {
			ResourceSyncInfo resourceInfo = new ResourceSyncInfo(file.getName(), true);
			setSyncInfo(file, resourceInfo);
		}
		SyncFileUtil.writeFolderConfig(file, info);
	}
	
	public void refreshFromLocal(File file, int depth, IProgressMonitor progress) throws CVSException {	
	}
	
	public ResourceSyncInfo[] members(File file) throws CVSException {
		if(file.isDirectory()) {
			return SyncFileUtil.readEntriesFile(file);
		} else {
			return new ResourceSyncInfo[0];
		}
	}	
}