package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.team.ccvs.core.CVSProviderPlugin;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.syncinfo.ResourceSyncInfo;

/**
 * Implements the ICVSFolder interface on top of an 
 * instance of the ICVSFolder interface
 * 
 * @see ICVSFolder
 */
public class LocalFolder extends LocalResource implements ICVSFolder {

	public LocalFolder(File ioResource) {
		super(ioResource);		
	}

	/**
	 * 
	 * @see ICVSFolder#getFolders()
	 */
	public ICVSFolder[] getFolders() throws CVSException {
		
		if(!getLocalFile().exists()) {
			return new ICVSFolder[0];
		}
		
		final List folders = new ArrayList();
		
		ResourceSyncInfo[] syncDirs = CVSProviderPlugin.getSynchronizer().members(ioResource);
		for (int i = 0; i < syncDirs.length; i++) {
			if(syncDirs[i].isDirectory()) {
				folders.add((new LocalFolder(new File(ioResource, syncDirs[i].getName()))));
			}			
		}
		
		File[] realDirs = ioResource.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isDirectory()) {
					LocalFolder cvsFolder = new LocalFolder(file);
					if(!cvsFolder.isIgnored() && !folders.contains(cvsFolder)) {
						folders.add(cvsFolder);						
					}
				}
				return false;
			}
		});
								
		return (ICVSFolder[]) folders.toArray(new ICVSFolder[folders.size()]);
	}
	
	/**
	 * @see ICVSFolder#getFiles()
	 */
	public ICVSFile[] getFiles() throws CVSException {
		
		if(!getLocalFile().exists()) {
			return new ICVSFile[0];
		}
		
		final List files = new ArrayList();
		
		ResourceSyncInfo[] syncDirs = CVSProviderPlugin.getSynchronizer().members(ioResource);
		for (int i = 0; i < syncDirs.length; i++) {
			if(!syncDirs[i].isDirectory()) {
				files.add((new LocalFile(new File(ioResource, syncDirs[i].getName()))));
			}			
		}
		
		File[] realDirs = ioResource.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(!file.isDirectory()) {
					LocalFile cvsFile = new LocalFile(file);
					if(!cvsFile.isIgnored() && !files.contains(cvsFile)) {
						files.add(cvsFile);
					}
				}
				return false;
			}
		});								
		return (ICVSFile[]) files.toArray(new ICVSFile[files.size()]);	
	}

	/**
	 * @see ICVSFolder#createFolder(String)
	 */
	public ICVSFolder getFolder(String name) throws CVSException {
		if ((".".equals(name)) || (("." + SEPARATOR).equals(name)))
			return this;
		return new LocalFolder(new File(ioResource, name));
	}

	/**
	 * @see ICVSFolder#createFile(String)
	 */
	public ICVSFile getFile(String name) throws CVSException {
		return new LocalFile(new File(ioResource, name));
	}

	/**
	 * @see ICVSFolder#getChild(String)
	 */
	public ICVSResource getChild(String name) throws CVSException {
		
		boolean isDirectory;
		
		File child = new File(ioResource, name);
		if(child.exists()) {
			isDirectory = child.isDirectory();
		} else {
			LocalFile localFile = new LocalFile(child);
			ResourceSyncInfo info = localFile.getSyncInfo();
			if (info == null)
				throw new CVSFileNotFoundException(Policy.bind("LocalFolder.invalidChild", child.getAbsolutePath()));
			isDirectory = info.isDirectory();			
		}
		
		if(isDirectory) {
			return getFolder(name);
		} else {
			return getFile(name);
		}
	}
	/**
	 * @see ICVSFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		ioResource.mkdir();
	}
		
	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
		
//	/**
//	 * Remove the fileInfo for a specific file. If it was not there
//	 * before nothing happens.
//	 * 
//	 * @param file has to satisfy file.getParent().equals(this)
//	 */
//	void removeFileInfo(IManagedFile file) throws CVSException {
//
//		Assert.isTrue(file.getParent().equals(this));
//		
//		fileInfoContainer.removeFileInfo(file.getName());
//	}
	
	/**
	 * @see ICVSFolder#acceptChildren(ICVSResourceVisitor)
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException {
		
		ICVSResource[] subFiles;
		ICVSResource[] subFolders;
		
		subFiles = getFiles();
		subFolders = getFolders();
		
		for (int i=0; i<subFiles.length; i++) {
			subFiles[i].accept(visitor);
		}
		
		for (int i=0; i<subFolders.length; i++) {
			subFolders[i].accept(visitor);
		}
	}

	/**
	 * @see ICVSResource#accept(ICVSResourceVisitor)
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException {
		visitor.visitFolder(this);
	}

	/**
	 * @see ICVSResource#getRemoteLocation(ICVSFolder)
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException {
		
		String parentLocation;
		
		if (getFolderSyncInfo() != null) {
			return getFolderSyncInfo().getRemoteLocation();
		}			

		if (equals(stopSearching)) {
			return null;
		}
		
		parentLocation = getParent().getRemoteLocation(stopSearching);
		if (parentLocation == null) {
			return null;
		} else {
			return parentLocation + SEPARATOR + getName();
		}
		
	}

	/*
	 * @see ICVSFolder#childExists(String)
	 */
	public boolean childExists(String path) {
		return false;
	}

	/*
	 * @see ICVSFolder#getFolderInfo()
	 */
	public FolderSyncInfo getFolderSyncInfo() throws CVSException {
		return CVSProviderPlugin.getSynchronizer().getFolderSync(ioResource);
	}

	/*
	 * @see ICVSFolder#setFolderInfo(FolderSyncInfo)
	 */
	public void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException {
		CVSProviderPlugin.getSynchronizer().setFolderSync(ioResource, folderInfo);
	}

	/*
	 * @see ICVSFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() {
		try {
			return CVSProviderPlugin.getSynchronizer().getFolderSync(ioResource) != null;
		} catch(CVSException e) {
			return false;
		}
	}

	/*
	 * @see ICVSResource#unmanage()
	 */
	public void unmanage() throws CVSException {
		CVSProviderPlugin.getSynchronizer().deleteFolderSync(ioResource, new NullProgressMonitor());
	}
}