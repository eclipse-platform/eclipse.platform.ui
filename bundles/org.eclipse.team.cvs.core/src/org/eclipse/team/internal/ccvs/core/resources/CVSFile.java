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

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.api.ICVSFile;


/**
 * Implement the abstract fileSystem
 * @see CVSResource
 */
class CVSFile extends CVSResource implements ICVSFile {
	
	// The ioResource is saved in CVSResource and used from there
	// private File file;
	
	CVSFile(String path) throws CVSException {
		this(new File(path));
	}


	CVSFile(File ioFile) {
		// puts the file into resource
		super(ioFile);
		
		Assert.isTrue(!ioFile.exists() || ioFile.isFile());
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
	 * Acctuall creation of a new file. (Does not have to exist before)
	 * This is used from outside to create a file at a location.
	 * 
	 * All checks, and creaton of files are done here
	 */		
	static ICVSFile createFileFrom(String path) throws CVSException {
		return createFileFrom(new File(path));
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
			// newFile.createNewFile();
		} catch (IOException e) {
			throw wrapException(e);
		}
		return new CVSFile(newFile);
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
	 * @see ICVSFile#setContent(String[], boolean)
	 */
//	public void setContent(String[] content, String delim)
//		throws CVSException {
//		
//		writeToFile(ioResource, content, delim);
//	}
	
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
		// getParent().clearCache(boolean deep);
	}
	
	public void setReadOnly() {
		
		boolean sucess;
		sucess = ioResource.setReadOnly();
		Assert.isTrue(sucess);
		
	}

}


