package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;
import org.eclipse.team.internal.ccvs.core.util.Assert;


/**
 * Implement the abstract fileSystem
 * @see CVSResource
 */
class CVSFile extends CVSResource implements ICVSFile {

	// We could use a normal HashMap in case the caller does not have instances
	// for all the time it needs the object
	private static Map instancesCache = new HashMap();
	
	// The ioResource is saved in CVSResource and used from there
	// private File file;
	
	/**
	 * Do not use the constructor, as it does not support the caching.
	 * Use createInternalFileFrom instead.
	 */
	private CVSFile(File ioFile) {
		// puts the file into resource
		super(ioFile);
		
		Assert.isTrue(!ioFile.exists() || ioFile.isFile());
	}

	/**
	 * Use this method intead of the constructur. If CACHING == true
	 * the instances of this class are stored in a map and given you
	 * on request.
	 */
	static CVSFile createInternalFileFrom(File newFile) throws CVSException {
		
		CVSFile resultFile;
		
		try {
			newFile = newFile.getCanonicalFile();
		} catch (IOException e) {
			throw new CVSException(Policy.bind("CVSFolder.invalidPath"),e);
		}

		if (!CACHING) {
			return new CVSFile(newFile);
		}
		
		resultFile = (CVSFile) instancesCache.get(newFile.getAbsolutePath()+KEY_EXTENTION);
		
		if (resultFile == null) {
			resultFile = new CVSFile(newFile);
			instancesCache.put(resultFile.ioResource.getAbsolutePath()+KEY_EXTENTION,resultFile);
		}
		
		return resultFile;
	}

	/**
	 * @see ICVSFile#getInputStream()
	 */
	public InputStream getInputStream() throws CVSException {
		
		exceptionIfNotExist();
		
		try {
			return new FileInputStream(ioResource);
		} catch (IOException e) {
			throw wrapException(e);
		}
	}

	/**
	 * @see CVSFile#createFileFromPath(String)
	 */	
	public static ICVSFile createFileFrom(File newFile) throws CVSException {
		
		if (!newFile.getParentFile().exists()) {
			throw new CVSException("You tried to create a file in an non-existing Folder");
		}
		
		try {		
			newFile = newFile.getCanonicalFile();
		} catch (IOException e) {
			throw wrapException(e);
		}
		return createInternalFileFrom(newFile);
	}
	
	/**
	 * @see ICVSFile#getOutputStream()
	 */
	public OutputStream getOutputStream() throws CVSException {
		
		// If the file is read-only we need to delete it before
		// we can write it new
		deleteIfProtected(ioResource);
		
		// No CVSException should happen here, unless
		// the underlying system is not O.K.
		try {
			return new FileOutputStream(ioResource);
		} catch (IOException e) {
			throw wrapException(e);
		}
	}


	/**
	 * @see ICVSFile#getSize()
	 */
	public long getSize() {
		
		return ioResource.length();
	}
	
	/**
	 * @see ICVSResource#delete()
	 */
	public void delete() {
		super.delete();
	}
	
	/**
	 * @see ICVSResource#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}
	
	
	/**
	 * @see ICVSFile#getTimeStamp()
	 */
	public long getTimeStamp() {
		return ioResource.lastModified();
	}


	/**
	 * @see ICVSFile#setTimeStamp(Date)
	 */
	public void setTimeStamp(long msec) {
		ioResource.setLastModified(msec);
	}

	/**
	 * @see ICVSFile#getContent()
	 */
	public String[] getContent() throws CVSException {
		return readFromFile(ioResource);
	}
	
	/**
	 * @see ICVSFile#moveTo(ICVSFile)
	 */
	public void moveTo(ICVSFile file) throws CVSException {
		
		boolean success;
		
		success = ioResource.renameTo(new File(file.getPath()));
		
		if (!success) {
			throw new CVSException("Move from " + ioResource + " to " + file + " was not possible");
		}
	}
	
	/**
	 * @see ICVSResource#clearCache()
	 */
	public void clearCache(boolean deep) throws CVSException {
		// Needs to clear any information stored with this file, as this
		// is called on delete
	}
	
	public void setReadOnly() {
		
		boolean sucess;
		sucess = ioResource.setReadOnly();
		Assert.isTrue(sucess);
		
	}

}


