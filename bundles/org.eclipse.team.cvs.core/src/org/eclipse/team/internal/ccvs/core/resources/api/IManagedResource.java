package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * An managedResource is an interface for an resource the cvs-client
 * accepts. It does provide storage of certain properties for every resource.
 * 
 * It also provides a list of resources within every folder, that is 
 * independend of underlign file-system
 */
public interface IManagedResource {
	
	/**
	 * Do we save the properties after every set
	 * of an info ? (AUTO_SAVE=true <=> no caching)
	 */
	public static final boolean AUTO_SAVE = true;

	/**
	 * The seperator that is used for giving and
	 * receiving pathnames
	 */
	public static final String separator = "/";
	
	/**
	 * Gives the path from the root folder to this folder.
	 * 
	 * root.getChild(getRelativePath()).equals(this)
	 * 
	 * @throws CVSException if getClass() != ancestor.getClass()
	 * @throws CVSException if ! absolutePathOf(this).startsWith(absolutePathOf(ancestor))
	 */
	String getRelativePath(IManagedFolder ancestor) throws CVSException;
	
	/**
	 * Indicates whether the object is a file or a folder
	 */
	boolean isFolder();
	
	/** 
	 * Delete the resource.
	 * In case of folder, with all the subfolders and files
	 */
	void delete();
	
	/**
	 * Tells if the underlying resource does exist.
	 * (Maybe it has been delted in between)
	 */
	boolean exists();	
	
	/**
	 * Give the folder that contains this resource.
	 * 
	 * If not isManaged() then the result of the operation is 
	 * unsepecified.
	 */
	IManagedFolder getParent();

	/**
	 * Give the name of the file back
	 * e.g. "folder1" for "C:\temp\folder1\"
	 */
	public String getName();
	
	/**
	 * Answer whether the resource is to be ignored or not
	 */
	public boolean isIgnored() throws CVSException;
	
	/**
	 * 
	 * Answer whether the resource is managed by it's parent. In CVS
	 * terms, this meanes the parent folder has an entry for the given
	 * resource in its CVS/Entries file.
	 * 
	 * @see IManagedFolder#isCVSFolder()
	 * A folder may not have an FolderProperties also it is 
	 * managed. This could only happen if the folder has
	 * been removed locally.
	 * 
	 */
	public boolean isManaged() throws CVSException;

	/**
	 * Unmanage the given resource by purging any CVS information 
	 * associated with the resource.
	 */
	public void unmanage() throws CVSException;
	
	/**
	 * Vistor-Pattern.<br>
	 * 
	 * Accept a vistor to this resource.
	 * To be implemented in file and folder (otherwise
	 * we do not know whether to call visitFolder or 
	 * visitFile)
	 */
	public void accept(IManagedVisitor visitor) throws CVSException;
	
	/**
	 * Get the remote location of a file either by reading it out of the
	 * file-info or by asking the parent-directory for it and appending the
	 * own name (recursivly).It stops recuring when it hits stopSearching.<br>
	 * 
	 * If you want to get the remoteLocation of the currentFolder only then
	 * use it with getRemoteLocation(this).
	 * 
	 * @return null if there was no remote-location until the folder stopSerarching
	 * @throws NullPointerException if stopSearching in not an ancestor of this
	 */
	public String getRemoteLocation(IManagedFolder stopSearching) throws CVSException;
	
	/**
	 * Get if the file has been modified since the last time
	 * saved in the fileEntry. Used on a folder it returns 
	 * whether a file in the tree under this folder has been 
	 * modified. This is an aproximation -- not to be used on 
	 * critical operations.<br>
	 * Use isDirty of the IManagedFile instead.
	 * 
	 * @return true if !isManaged()
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	boolean showDirty() throws CVSException;

	/**
	 * Look, whether the dirty-state of the file has acctally changed.
	 * If so then clear the cache for the dirty status of this element
	 * and all elements above.
	 * 
	 * @param up determins if the parents are acctually called. Setting this to false
	 * 			can have bad consequences. Use it only if you are sure that the parents 
	 * 			allready have a clean cache.
	 */
	void clearDirty(boolean up) throws CVSException;
	
	/**
	 * Get the information if a resource is managed from a 
	 * buffer. showManaged on a not-existing resource may or
	 * may not give the proper result.
	 */
	boolean showManaged() throws CVSException;
	
	/**
	 * clear the buffer whether this resource is managed.
	 */
	void clearManaged() throws CVSException;
}


