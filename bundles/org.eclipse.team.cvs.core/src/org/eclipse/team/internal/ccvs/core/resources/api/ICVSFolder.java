package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.ccvs.core.CVSException;


/**
 * Represents an abstract folder.
 * 
 * @see ICVSResource
 */
public interface ICVSFolder extends ICVSResource {
	
	/**
	 * Does list the whole content of the folder
	 * (files and folders)
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	public ICVSResource[] getResources() throws CVSException;

	/**
	 * Gives all the sub-folders of this folder (excluding the
	 * cvs-folder)
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	public ICVSFolder[] getFolders() throws CVSException;

	/**
	 * Gives all the files in this folder
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	public ICVSFile[] getFiles() throws CVSException;

	/**
	 * create a folder as a subfolder of this
	 * accepts "/" or "\" to make it a sub-sub folder of the
	 * current one. Alle folder on the way, that do not exsist
	 * are generated.
	 * 
	 * This is only calling a function in the file-system
	 * 
	 */
	ICVSFolder createFolder(String name) throws CVSException;
	
	/**
	 * Does create a file in the given folder. Does not accept
	 * any subfolders given in that moment. 
	 * If the file does exist, returns the file.
	 * 
	 * This is only calling a function in the file-system
	 * 
	 */
	ICVSFile createFile(String name) throws CVSException;
	
	/**
	 * Return the child resource at the given path relative to
	 * the receiver.
	 * This gets a file child of the current folder. It needs to
	 * contact the fileSystem to figure out whether we have got
	 * an folder or an file.
	 * It is decepated, because it should be possible to create 
	 * non-existend files (and then we need the information whether it
	 * is a file or a folder). 
	 * Use createFile or createFolder intstead. (Check whether it is 
	 * a folder or a file with childIsFolder)
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws CVSFileNotFoundException if childExists(path) = false
	 */
	ICVSResource getChild(String path) throws CVSException;
	
	/**
	 * Checks wether the child of the currentFolder with the 
	 * name exists.
	 */
	boolean childExists(String name);
	
	/**
	 * Checks wether a child is a folder. If the child does not
	 * exist then it automatically does return false.
	 * 
	 * @return false if childExists(name) = false
	 */
	boolean childIsFolder(String name);

	/**
	 * Create the folder if it did not exist before. Does only
	 * work if the direct subfolder did exist.
	 * 
	 * @throws CVSException if for some reason it was not possible to create the folder
	 */
	void mkdir() throws CVSException;
	
	// ---------- Here starts the property handling ----------
	/** 
	 * This method creats the ability to store properties.
	 * It does create the current folder as well, if it did not
	 * exist before.
	 * At the moment this invokes mkdir, so it craetest the current
	 * folder if it was not there before. 
	 * (This is going to change properbly)
	 */
	void makeCVSFolder() throws CVSException;

	/**
	 * The opposite of makeCVSFolder, delets all the properties of
	 * the folder and the ability to store such
	 */
	void unmakeCVSFolder();	

	/**
	 * Checks if properties are accessable and if so,
	 * whether it has at least the following three properties:
	 * root, repolsitory, entries 
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	boolean isCVSFolder() throws CVSFileNotFoundException;
	
	/**
	 * Attace a property to the folder.
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws NoCVSFolderException if isCVSFolder = false
	 */
	void setProperty(String key, String[] content) throws CVSException;

	/**
	 * Delete a property from a folder. If the property did not exist,
	 * nothing happens.
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws NoCVSFolderException if isCVSFolder = false
	 */
	// void unsetProperty(String key) throws CVSException;
	
	/**
	 * Get the property of a folder.
	 * 
	 * @return the contend of the property if the property does exsist, null otherwise
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws NoCVSFolderException if isCVSFolder = false
	 */
	String[] getProperty(String key) throws CVSException;
	
}


