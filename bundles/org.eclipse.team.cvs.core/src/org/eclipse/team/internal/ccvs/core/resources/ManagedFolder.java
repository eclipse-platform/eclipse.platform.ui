package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.commands.FileNameMatcher;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.FileProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFile;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedResource;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedVisitor;
import org.eclipse.team.internal.ccvs.core.util.Assert;

/**
 * Implements the IManagedFolder interface on top of an 
 * instance of the ICVSFolder interface
 * 
 * @see IManagedFolder
 */
class ManagedFolder extends ManagedResource implements IManagedFolder {
	
	public static final String PWD_PROPERTY = "Password";
	
	private ICVSFolder cvsFolder;
	private FilePropertiesContainer fileInfoContainer;
	
	/**
	 * Constructor for ManagedFolder
	 */
	ManagedFolder(ICVSFolder cvsFolder) {
		super();
		this.cvsFolder = cvsFolder;
		fileInfoContainer = new FilePropertiesContainer(cvsFolder,AUTO_SAVE);
	}

	/**
	 * @see IManagedFolder#getFolders()
	 */
	public IManagedFolder[] getFolders() throws CVSException {
		
		ICVSFolder[] entrieFolders;
		ICVSFolder[] underlyingFolders;
		ICVSFolder[] allFolders;
		IManagedFolder[] resultFolders;
		
		exceptionIfNotExists();
		
		entrieFolders = fileInfoContainer.getEntriesFolderList();
		underlyingFolders = cvsFolder.getFolders();
		
		// merge the list of the folders
		allFolders = (ICVSFolder[]) merge(entrieFolders, 
						underlyingFolders, 
						new ICVSFolder[0]);
						
		// NIK: we could add a folder, that we are going to ignore
		//      afterwards, it would stay forever in the entries
		//      as to be added ?!?
		allFolders = (ICVSFolder[])removeIgnored(allFolders, new ICVSFolder[0]);
						
		// wrap the cvsFolders to managedFolders
		resultFolders = new IManagedFolder[allFolders.length];
		for (int i=0; i<allFolders.length; i++) {
			resultFolders[i] = createResourceFrom(allFolders[i]);
		}
		
		return resultFolders;
	}
	
	/**
	 * Takes two Array and returns the contend of the two arrays
	 * minus all dublication entries.
	 * A dublication is a entrie x that has an entry y allready in the
	 * result with x.equals(y).
	 * 
	 * e.g.:    tmp = (String[])merge(new String[]{"a","b","c"},
	 * 								  new String[]{"b","c","d"},
	 * 								  new String[0]);
	 * 
	 * result:  tmp.equals(new String[]{"a","b","c","d"})
	 */
	private Object[] merge(Object[] array1, Object[] array2, Object[] resultArray) {
		
		Set mergeSet = new TreeSet();
		
		for (int i=0; i<array1.length; i++) {
			mergeSet.add(array1[i]);
		}

		for (int i=0; i<array2.length; i++) {
			mergeSet.add(array2[i]);
		}
		
		return mergeSet.toArray(resultArray);
	}
		
	/**
	 * @see IManagedFolder#getFiles()
	 */
	public IManagedFile[] getFiles() throws CVSException {
		
		ICVSFile[] entrieFiles;
		ICVSFile[] underlyingFiles;
		ICVSFile[] allFiles;
		IManagedFile[] resultFiles;

		exceptionIfNotExists();
		
		entrieFiles = fileInfoContainer.getEntriesFileList();
		underlyingFiles = cvsFolder.getFiles();
		
		// merge the list of the Files
		allFiles = (ICVSFile[]) merge(entrieFiles, 
						underlyingFiles, 
						new ICVSFile[0]);
		allFiles = (ICVSFile[])removeIgnored(allFiles, new ICVSFile[0]);

						
		// wrap the cvsFiles to managedFiles
		resultFiles = new IManagedFile[allFiles.length];
		for (int i=0; i<allFiles.length; i++) {
			resultFiles[i] = createResourceFrom(allFiles[i]);
		}
		
		return resultFiles;	
	}

	/**
	 * @see IManagedFolder#createFolder(String)
	 */
	public IManagedFolder getFolder(String name) throws CVSException {
		return createResourceFrom(cvsFolder.createFolder(name));
	}

	/**
	 * @see IManagedFolder#createFile(String)
	 */
	public IManagedFile getFile(String name) throws CVSException {
		return createResourceFrom(cvsFolder.createFile(name));
	}

	/**
	 * @see IManagedFolder#childExists(String)
	 */
	public boolean childExists(String path) {
		return cvsFolder.childExists(path);
	}

	/**
	 * @see IManagedFolder#getChild(String)
	 */
	public IManagedResource getChild(String name) throws CVSException {
		
		IManagedResource mResource;
		
		mResource = getRealChild(name);
		
		if (mResource == null) {
			mResource = getVirtualChild(name);		
		}
		
		if (mResource == null) {
			throw new CVSFileNotFoundException("used getChild(" + name + ") on a not existing file");
		}
		
		return mResource;

	}
	
	/**
	 * Tries to find the child "path" in the file-system. If the child is not 
	 * there, then it returns null
	 */
	private IManagedResource getRealChild(String name) throws CVSException {

		ICVSResource cvsResource;
		
		if (!cvsFolder.childExists(name)) {
			return null;
		}

		cvsResource = cvsFolder.getChild(name);

		if (cvsResource.isFolder()) {
			return createResourceFrom((ICVSFolder)cvsResource);
		} else {
			return createResourceFrom((ICVSFile)cvsResource);
		}
	}
	
	/**
	 * Tries to find the child "path" in the entries. If it is not there the
	 * method returns null
	 */
	private IManagedResource getVirtualChild(String name) throws CVSException {
		
		IManagedFolder virtualParent;
		
		IManagedFolder[] folders;
		IManagedFile[] files;
		
		// get the direct parent of the child that we want to 
		// find if it does not exist then the virtual child
		// can not exist
		// We "cheat" and say, that the virtualChild is going to 
		// be a file. But as we use it for string-manipulation 
		// only this is allright
		virtualParent = getFile(name).getParent();
		name = getFile(name).getName();
		
		if (!virtualParent.exists()) {
			return null;
		}
		
		folders = virtualParent.getFolders();
		
		for (int i=0; i<folders.length; i++) {
			if (folders[i].getName().equals(name)) {
				return folders[i];
			}
		}
		
		files = virtualParent.getFiles();
		
		for (int i=0; i<files.length; i++) {
			if (files[i].getName().equals(name)) {
				return files[i];
			}
		}
				
		return null;
	}
	
		
	/**
	 * @see IManagedFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		cvsFolder.mkdir();
	}

	/**
	 * @see IManagedFolder#flush(boolean)
	 */
	public void flush(boolean deep) {
		// Does do nothing as AUTO_SAVE == true
		//
		// Otherwise we need somthing like load and save.
	}

	/**
	 * @see IManagedFolder#getFolderInfo()
	 */
	public FolderProperties getFolderInfo() throws CVSException {
			
		FolderProperties folderProperties = new FolderProperties();
		String key;
		String[] data;
		
		if (!exists() || !cvsFolder.isCVSFolder()) {
			return null;
		}

		for (Iterator i = folderProperties.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
			data = cvsFolder.getProperty(key);
			
			if (data == null) {
				// throw new CVSException("The FolderInformation in the folder " + cvsFolder + "  is partrtial");
				continue;
			} else if (data.length == 0) {
				folderProperties.putProperty(key,"");
			} else {
				folderProperties.putProperty(key,data[0]);
			}
		}
	
		return folderProperties;
	}

	/**
	 * @see IManagedFolder#setFolderInfo(FolderProperties)
	 */
	public void setFolderInfo(FolderProperties folderProperties) throws CVSException {

		String key;
		
		if (folderProperties == null) {
			removeFolderInfo();
			return;
		}
		
		exceptionIfNotExists();
		cvsFolder.makeCVSFolder();
		
		for (Iterator i = folderProperties.keySet().iterator(); i.hasNext();) {
			key = (String) i.next();
			if (folderProperties.getProperty(key) == null) {
				cvsFolder.setProperty(key,null);
			} else {
				cvsFolder.setProperty(key,
					new String[]{folderProperties.getProperty(key)});
			}
		}
		
		getInternalParent().addFolderEntrie(this);
		
	}
	
	/**
	 * Remove the FileProperties and therefore all the knowlege of the entrie-systen
	 * about this folder.
	 * The properties are deleted and the entries of the parent-folder
	 * are updated.
	 */
	private void removeFolderInfo() throws CVSException {
		
		if (exists() && cvsFolder.isCVSFolder()) {
			cvsFolder.unmakeCVSFolder();
		}
		
		getInternalParent().removeFolderEntrie(this);	
	}
	
	/**
	 * @see IManagedFolder#setProperty(String, String[])
	 */
	public void setProperty(String key, String[] content) throws CVSException {

		// We want to create a cvs-folder on the first time 
		// setting a property 
		cvsFolder.makeCVSFolder();

		cvsFolder.setProperty(key,content);

	}

	/**
	 * @see IManagedFolder#unsetProperty(String)
	 */
	public void unsetProperty(String key) throws CVSException {
		
		// Otherwise we do not want to do anything
		if (cvsFolder.isCVSFolder()) {
			cvsFolder.setProperty(key,null);
		}
	}

	/**
	 * @see IManagedFolder#getProperty(String)
	 */
	public String[] getProperty(String key) throws CVSException {
		return cvsFolder.getProperty(key);
	}
	
	/**
	 * @see IManagedResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
	
	protected boolean isIgnored(String child) throws CVSException {
		// NOTE: This is the wrong place for this
		if (child.equals("CVS"))
			return true;
		FileNameMatcher matcher = null;
		try {
			matcher = FileNameMatcher.getIgnoreMatcherFor(cvsFolder);
		} catch (IOException e) {
			// Log the exception and return files unchanged
			throw wrapException(e);
		}
		if (matcher == null)
			return false;
		return matcher.match(child);
	}
	
	/**
	 * Set the entry and the rest of the fileInfo for the file.
	 */
	void setFileInfo(IManagedFile file, FileProperties fileInfo) throws CVSException {

		Assert.isTrue(file.getParent().equals(this));
		Assert.isTrue(fileInfo == null || file.getName().equals(fileInfo.getName()));

		exceptionIfNotExists();
		cvsFolder.makeCVSFolder();
		
		fileInfoContainer.setFileInfo(file.getName(),fileInfo);
	}
	
	/**
	 * Get the fileInfo for a specific file.
	 * 
	 * @param file has to satisfy file.getParent().equals(this)
	 * @return null if isManaged() = false
	 */
	FileProperties getFileInfo(IManagedFile file) throws CVSException {

		Assert.isTrue(file.getParent().equals(this));
		
		return fileInfoContainer.getFileInfo(file.getName());
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
	 * Add an folder to the entries of this folder. If the folder 
	 * was in the list allready, it just stays there.
	 * 
	 * @param folder must satisfy: folder.getParent().equals(this)
	 * @throws CVSException if AUTO_SAVE & !folder.exists()
	 */
	void addFolderEntrie(IManagedFolder folder) throws CVSException {
		
		Assert.isTrue(folder.getParent().equals(this));
		
		// This could be the project-folder wich is not a CVSFolder
		// and we do not need to updated the entries
		if (cvsFolder.isCVSFolder()) {
			fileInfoContainer.addFolder(folder.getName());
		}
	}
	
	/**
	 * Remove an folder from the entrie of this folder. If the folder
	 * has not been there before it is not in it after the operation.
	 * 
	 * @param folder must satisfy: folder.getParent().equals(this)
	 * @throws CVSException if AUTO_SAVE & !folder.exists()
	 */
	void removeFolderEntrie(IManagedFolder folder) throws CVSException {
		
		Assert.isTrue(folder.getParent().equals(this));

		// This could be the project-folder wich is not a CVSFolder
		// and we do not need to updated the entries
		if (cvsFolder.isCVSFolder()) {		
			fileInfoContainer.removeFolder(folder.getName());
		}
	}
	
	/**
	 * Is the folder in the entries
	 * 
	 * @return false if !folder.getParent().equals(this)
	 * @throws CVSException if AUTO_SAVE & !folder.exists()
	 */
	boolean containsFolderEntrie(IManagedFolder folder) throws CVSException {

		if (!folder.getParent().equals(this) || !cvsFolder.isCVSFolder()) {
			return false;
		} else {
			return fileInfoContainer.containsFolder(folder.getName());
		}
	}

	/**
	 * @see IManagedResource#isManaged()
	 */
	public boolean isManaged() throws CVSException {
		
		// To be implemented after ManagedFolder
		// we need a method, that tells us wether a folder
		// or a file is in the entries-property.
		
		return getInternalParent().containsFolderEntrie(this);
	}
	
	/**
	 * @see ManagedResource#getResource()
	 */
	public ICVSResource getCVSResource() {
		return cvsFolder;
	}
	

	/**
	 * @see IManagedFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() throws CVSException {
		return exists() && cvsFolder.isCVSFolder();
	}


	/**
	 * @see IManagedFolder#acceptChildren(IManagedVisitor)
	 */
	public void acceptChildren(IManagedVisitor visitor) throws CVSException {
		
		IManagedResource[] subFiles;
		IManagedResource[] subFolders;
		
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
	 * @see IManagedResource#accept(IManagedVisitor)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException {
		visitor.visitFolder(this);
	}

	/**
	 * @see IManagedResource#getRemoteLocation(IManagedFolder)
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException {
		
		String parentLocation;
		
		if (getFolderInfo() != null) {
			return getFolderInfo().getRemoteLocation();
		}			

		if (equals(stopSearching)) {
			return null;
		}
		
		parentLocation = getParent().getRemoteLocation(stopSearching);
		if (parentLocation == null) {
			return null;
		} else {
			return parentLocation + separator + getName();
		}
		
	}

	/**
	 * @see IManagedResource#unmanage()
	 * @deprecated uses unmakeCVSFolder intstead of setFolderInfo(null)
	 */
	public void unmanage() throws CVSException {
		accept(new IManagedVisitor() {
			public void visitFile(IManagedFile file) throws CVSException {}
			public void visitFolder(IManagedFolder folder) throws CVSException {
				folder.acceptChildren(this);
				folder.setFolderInfo(null);
			}
		});
	}

	/**
	 * Remove the ignored resources from the provided list of resources.
	 * The type variable is used to determine the type of the elements in
	 * the resulting array.
	 */
	private ICVSResource[] removeIgnored(ICVSResource[] resources, ICVSResource[] type) throws CVSException {
		FileNameMatcher matcher = null;
		try {
			matcher = FileNameMatcher.getIgnoreMatcherFor(cvsFolder);
		} catch (IOException e) {
			// Log the exception and return files unchanged
			throw wrapException(e);
		}
		if (matcher == null)
			return resources;
		List result = new ArrayList(resources.length);
		for (int i=0;i<resources.length;i++) {
			if (!matcher.match(resources[i].getName()))
				result.add(resources[i]);
		}
		if (result.size() == resources.length)
			return resources;
		return (ICVSResource[])result.toArray(type);	
	}

}

