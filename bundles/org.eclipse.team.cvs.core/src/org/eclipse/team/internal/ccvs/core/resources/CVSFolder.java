package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.CVSFileNotFoundException;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSResource;
import org.eclipse.team.internal.ccvs.core.resources.api.NotCVSFolderException;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.ListFileFilter;
import org.eclipse.team.internal.ccvs.core.util.Util;


/**
 * Implement the abstract fileSystem
 * @see CVSResource
 */
class CVSFolder extends CVSResource implements ICVSFolder {
	
	// The ioFolder is stored in the ioResource of the 
	// superclass CVSResource
	// -- private File ioFolder;
	
	public static final String CVS_FOLDER_NAME = "CVS";
	public static final boolean PROPERTY_READ_CHACHING = true;
	
	// If we do not extend the key and therefore the key is the same like
	// the absolut pathname we have indirectly an reference to the key in
	// the weak hashmap. Therefore the WeakHashMap does not finalize anything
	private static final String KEY_EXTENTION = "KEY";
	
	// We could use a normal HashMap in case the caller does not have instances
	// for all the time it needs the object
	private static Map instancesCache = new HashMap();
	private HashMap propertiesCache = new HashMap();
	private Boolean cvsFolderCache = null;
	
	/**
	 * NOT to be called (directly). Use createInternalFolder indtead.
	 */
	private CVSFolder(File ioFolder) {
		// puts the file into resource
		super(ioFolder);

		Assert.isTrue(ioFolder == null || !ioFolder.exists() || ioFolder.isDirectory());
	}
	
	/**
	 * @see ICVSFolder#getFolders()
	 */
	public ICVSFolder[] getFolders() throws CVSException {
		
		File[] folderList;
		ICVSFolder[] cvsFolderList;
		
		// Get all folder without the cvs-folder
		folderList = ioResource.listFiles(new FoFilter());
		cvsFolderList = new ICVSFolder[folderList.length];


		for (int i = 0; i<folderList.length; i++) {
			 cvsFolderList[i] = createInternalFolderFrom(folderList[i]);
		}
		return cvsFolderList;
	}


	/**
	 * @see ICVSFolder#getFiles()
	 */
	public ICVSFile[] getFiles() {
		
		File[] fileList;
		ICVSFile[] cvsFileList;
		
		// Get all files
		fileList = ioResource.listFiles(new FiFilter());
		cvsFileList = new ICVSFile[fileList.length];
		
		for (int i = 0; i<fileList.length; i++) {
			 cvsFileList[i] = new CVSFile(fileList[i]);
		}
		return cvsFileList;
	}

	/**
	 * Does list the whole content of the folder
	 * (files and folders)
	 */
	public ICVSResource[] getResources() throws CVSException {
		
		File[] resourceList;
		ICVSResource[] cvsResourceList;
		
		exceptionIfNotExist();
		
		// Get all resources
		resourceList = ioResource.listFiles(new NoCVSFilter());


		cvsResourceList = new ICVSResource[resourceList.length];
		
		for (int i = 0; i<resourceList.length; i++) {
			if (resourceList[i].isDirectory()) {
				cvsResourceList[i] = createInternalFolderFrom(resourceList[i]);
			} else {
				cvsResourceList[i] = new CVSFile(resourceList[i]);
			}
		}
		
		return cvsResourceList;
	}


	/**
	 * @see ICVSFolder#createFolder(String)
	 */
	public ICVSFolder createFolder(String name) throws CVSException {
		return createFolderFrom(new File(ioResource, convertSeparator(name)));
	}

	/**
	 * Acctuall creation of a new folder. (Does not have to exist before)
	 * This is used from outside to create a folder at a location.
	 * 
	 * Here is checked, wether the file we try to create exists
	 * and if we try to create somthing unallowed (e.g. the cvs-folder)
	 */
	public static ICVSFolder createFolderFrom(File newFolder) throws CVSException {
		
		try {
			newFolder = newFolder.getCanonicalFile();
		} catch (IOException e) {
			throw wrapException(e);
		}
		
		if (newFolder.getName().toUpperCase().equals(CVS_FOLDER_NAME)) {
			throw new CVSException("You are not allowed to create the CVS-Folder");
		} else {	
			return createInternalFolderFrom(newFolder);
		}	
	}
	
	static CVSFolder createInternalFolderFrom(File newFolder) throws CVSException {
		
		CVSFolder resultFolder;
		
		try {
			newFolder = newFolder.getCanonicalFile();
		} catch (IOException e) {
			throw new CVSException(Policy.bind("CVSFolder.invalidPath"),e);
			// Util.logError(Policy.bind("CVSFolder.invalidPath"),e);
		}
		
		resultFolder = (CVSFolder) instancesCache.get(newFolder.getAbsolutePath()+KEY_EXTENTION);
		
		if (resultFolder == null) {
			resultFolder = new CVSFolder(newFolder);
			instancesCache.put(resultFolder.ioResource.getAbsolutePath()+KEY_EXTENTION,resultFolder);
		}
		
		return resultFolder;
	}
	
	/**
	 * @see ICVSFolder#createFile(String)
	 * 
	 */
	public ICVSFile createFile(String name) throws CVSException {
		
		// No converting of the seperators here
		// this function does not work on subfolders anyway
		return CVSFile.createFileFrom(new File(ioResource, name));
		
	}


	/**
	 * @see ICVSFolder#isCVSFolder()
	 */
	public boolean isCVSFolder() throws CVSFileNotFoundException {
		
		if (cvsFolderCache == null) {
			exceptionIfNotExist();
			cvsFolderCache = new Boolean((new File(ioResource, CVS_FOLDER_NAME)).exists());
		}
		
		return cvsFolderCache.booleanValue();
		
	}


	/**
	 * @see ICVSFolder#makeCVSFolder()
	 */
	public void makeCVSFolder() throws CVSException {
		
		exceptionIfNotExist();

		(new File(ioResource, CVS_FOLDER_NAME)).mkdir();
		
		clearCache(false);
		
	}

	/**
	 * Throw an exception if the folder in no cvs-folder
	 */
	private File getCVSFolder() throws NotCVSFolderException, CVSFileNotFoundException {
		
		if (!isCVSFolder()) {
			throw new NotCVSFolderException("You tried to do an cvs-operation on a non cvs-folder");
		}
		
		return new File(ioResource, CVS_FOLDER_NAME);
	}
	
	/**
	 * @see ICVSFolder#setProperty(String, String[])
	 */
	public void setProperty(String key, String[] content) throws CVSException {
		
		File cvsFolder;
		File propertyFile;

		// If we have got a property that is null,
		// then it is acctually an unset property
		if (content == null) {
			unsetProperty(key);
			return;
		}
		
		Assert.isTrue(content.length == 0 || content[0]!=null);
		
		cvsFolder = getCVSFolder();
		propertyFile = new File(cvsFolder, key);

		writeToFile(propertyFile,content);
		
		if (PROPERTY_READ_CHACHING) {		
			propertiesCache.put(key, content);
		}
	}


	/**
	 * @see ICVSFolder#unsetProperty(String)
	 */
	public void unsetProperty(String key) throws CVSException {
		File cvsFolder;

		cvsFolder = getCVSFolder();
		(new File(cvsFolder, key)).delete();
		
		if (PROPERTY_READ_CHACHING) {		
			propertiesCache.put(key, null);
		}
	}


	/**
	 * @see ICVSFolder#getProperty(String)
	 */
	public String[] getProperty(String key)
		throws NotCVSFolderException, CVSException {
		
		String[] property;
		File cvsFolder;
		File propertyFile;
		
		if (PROPERTY_READ_CHACHING && propertiesCache.containsKey(key)) {
			return (String[])propertiesCache.get(key);
		}
			
		cvsFolder = getCVSFolder();
		propertyFile = new File(cvsFolder, key);
			
		// If the property does not exsist we return null
		// this is specified
		if (propertyFile.exists()) {
			property = readFromFile(propertyFile);
		} else {
			property = null;
		} 

		if (PROPERTY_READ_CHACHING) {	
			propertiesCache.put(key, property);
		}
		
		return property;
	}
	
	/**
	 * The oposite of makeCVSFolder,
	 * does delete the whoole CVS folder
	 */
	public void unmakeCVSFolder() {
		
		File[] fileList;
		
		try {
			if (!isCVSFolder()) {
				return;
			}
			
			fileList = getCVSFolder().listFiles();
			for (int i = 0; i < fileList.length; i++) {
				fileList[i].delete();
			}
			getCVSFolder().delete();
			clearCache(false);
		} catch (CVSException e) {
			Assert.isTrue(false);
		}	
		
	}
	
	/**
	 * @see ICVSResource#delete()
	 */
	public void delete() {
		
		// If there is nothing to delete return
		if (!ioResource.exists()) {
			return;
		}
		
		ICVSResource[] resourceList;
		
		try {
			resourceList = getResources();
		} catch (CVSException e) {
			// If the file has been deletet in between we
			// stop executing
			return;
		}
		
		for (int i = 0; i < resourceList.length; i++) {
			resourceList[i].delete();
		}
		
		unmakeCVSFolder();
		super.delete();
		
		try {
			clearCache(false);
		} catch (CVSException e) {
			Assert.isTrue(false);
		}
	}


	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return true;
	}
	
	/**
	 * @see ICVSFolder#getChild(IPath)
	 */
	public ICVSResource getChild(String path) throws CVSException {
		File file;
		
		file = new File(ioResource, convertSeparator(path));
		
		if (!childExists(path)) {
			throw new CVSFileNotFoundException(getPath() + "/" + path + " does not exist");
		}
		
		if (file.isDirectory()) {
			return createInternalFolderFrom(file);
		} else {
			return new CVSFile(file);
		}
	}


	/**
	 * @see ICVSFolder#childExists(String)
	 */
	public boolean childExists(String name) {
		return (new File(ioResource,name)).exists();
	}


	/**
	 * @see ICVSFolder#childIsFolder(String)
	 */
	public boolean childIsFolder(String name) {
		return (new File(ioResource,name)).isDirectory();
	}


	/**
	 * @see ICVSFolder#mkdir()
	 */
	public void mkdir() throws CVSException {
		
		boolean success;
		
		success = ioResource.mkdir();
		if (!success && !exists()) {
			throw new CVSException("Folder-Creation failed: " + getName());
		}
	}

	/**
	 * @see ICVSResource#clearCache()
	 */
	public void clearCache(boolean deep) throws CVSException {
		
		ICVSResource[] resources;
		
		// Do that first, maybe we have got wrong entries
		// cached
		propertiesCache = new HashMap();
		cvsFolderCache = null;
		
		if (!deep) {
			return;
		}
		
		resources = getResources();
		
		for (int i = 0; i < resources.length; i++) {
			resources[i].clearCache(true);
		}
		
	}

}


/**
 * Does filter that you get files back (and no folders)
 */
class FiFilter implements FileFilter {
	public boolean accept(File file) {
		return file.isFile();
	}
}


/**
 * Does filter that you get folders back (and no files)
 * Does not give the folder called 
 * cvs (no matter wether lowcase or upcase back)
 */
class FoFilter extends ListFileFilter {
	public FoFilter() {
		// get all folders, that are not the cvs-folder
		super(new String[]{CVSFolder.CVS_FOLDER_NAME},true,false,true);
	}
}


/**
 * Gives you every Resouce but the CVS-Folder back
 */
class NoCVSFilter extends ListFileFilter {
	public NoCVSFilter() {
		// get all that is not the cvs-folder
		super(new String[]{CVSFolder.CVS_FOLDER_NAME},true);
	}
}