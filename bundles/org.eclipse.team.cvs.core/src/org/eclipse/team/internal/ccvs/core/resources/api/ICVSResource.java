package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.team.internal.ccvs.core.CVSException;


/**
 * Represents an abstract file or a folder.
 * 
 * This can be a acctual file in the local system, webDev
 * or ftp remote-files etc.
 * 
 * The interfaces are to be implemented by the user of the
 * cvsclient.core.
 * 
 * The handle to an resource-object (like in java.io) does not nessarily
 * mean that the underlying resource exists. The function exists() 
 * checks that. Many Operations give an CVSFileNotFoundException if the
 * file is not there.
 */

public interface ICVSResource {

	/**
	 * The seperator that is used for giving and
	 * reciving pathnames
	 */
	//MV: This should be in CVSResource, not ICVSResource
	public static final String seperator = File.separator;
	
	/**
	 * Gives the platform dependend Path of the file back.
	 * Should be used for monitoring only.
	 */
	String getPath();
	
	/**
	 * Indicates whether the object is a file or a folder
	 */
	boolean isFolder();
	
	/** 
	 * Delete the resource.
	 * In case of folder, with all the subfolders and files.
	 * 
	 * Deleting a non-existing resourec does nothing.
	 */
	void delete();
	
	/**
	 * Give the folder that contains this resource.
	 * 
	 * The behavior is unspecified as soon as isCVSFolder() = false
	 */
	ICVSFolder getParent();

	/**
	 * Give the name of the file back
	 * e.g. "folder1" for "C:\temp\folder1\"
	 */
	public String getName();
	
	/**
	 * Check if the file exists in the underlying fileSystem
	 */
	boolean exists();
	
	/**
	 * Clears all the information in the cache, if a cache exists.
	 */
	void clearCache(boolean deep) throws CVSException ;
	
}


