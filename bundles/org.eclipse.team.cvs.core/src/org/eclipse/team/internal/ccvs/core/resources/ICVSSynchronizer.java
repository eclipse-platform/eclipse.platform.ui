package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;

/**
 * A synchronizer is responsible for managing synchronization information for local
 * CVS resources.
 * 
 * @see ResourceSyncInfo
 * @see FolderSyncInfo
 */
public interface ICVSSynchronizer {

	/**
	 * Associates the provided folder sync information with the given folder. The folder
	 * must exist on the file system.
	 * <p>
	 * The workbench and team plugins are notified that the state of this resources has 
	 * changed.</p>
	 * 
	 * @param file the file or folder for which to associate the sync info.
	 * @param info the folder sync to set.
	 * 
 	 * @throws CVSException if there was a problem adding sync info.
	 */
	public void setFolderSync(File folder, FolderSyncInfo info) throws CVSException;
	
	/**
	 * Answers the folder sync information associated with this folder or <code>null</code>
	 * if none is available.
	 * 
	 * @param folder the folder for which to return folder sync info.
 	 * @throws CVSException if there was a problem adding folder sync info.
	 */
	public FolderSyncInfo getFolderSync(File file) throws CVSException;	

	/**
	 * Associates the provided sync information with the given file or folder. The resource
	 * may or may not exist on the file system however the parent folder must be a cvs
	 * folder.
	 * <p>
	 * The workbench and team plugins are notified that the state of this resources has 
	 * changed.</p>
	 * 
	 * @param file the file or folder for which to associate the sync info.
	 * @param info to set. The name in the resource info must match the file or folder name.
	 * 
 	 * @throws CVSException if there was a problem adding sync info.
	 */
	public void setResourceSync(File file, ResourceSyncInfo info) throws CVSException;
	
	/**
	 * Answers the sync information associated with this file of folder or <code>null</code>
	 * if none is available. A resource cannot have sync information if its parent folder
	 * does not exist.
	 * 
	 * @param file the file or folder for which to return sync info.
 	 * @throws CVSException if there was a problem adding sync info or broadcasting
	 * the changes.
	 */
	public ResourceSyncInfo getResourceSync(File file) throws CVSException;
	
	/**
	 * Removes the folder's and all children's folder sync information. This will essentially remove
	 * all CVS knowledge from these resources.
	 */		
	public void deleteFolderSync(File file, IProgressMonitor monitor) throws CVSException;
	
	/**
	 * Removes the resource's sync information.
	 */
	public void deleteResourceSync(File file) throws CVSException;

	/**
	 * Allows the synchronizer to update the workspace with changes made by an 3rd
	 * party tool to the sync info. 
	 */
	public void reload(File file, IProgressMonitor monitor) throws CVSException;
	
	/**
	 * Call to allow the synchronizer to save any pending or buffered changes and dispatch
	 * state change notifications.
	 */
	public void save(File file, IProgressMonitor monitor) throws CVSException;
	
	/**
	 * Answers an array with the sync information for immediate child resources of this folder. Note 
	 * that the returned sync information may be for resources that no longer exist (e.g. in the
	 * case of a pending deletion).
	 * 
	 * @param folder the folder for which to return the children resource sync infos. The folder
	 * must exist.
	 * 
	 * @throws CVSException if an error occurs retrieving the sync info.
	 */
	public ResourceSyncInfo[] members(File folder) throws CVSException;
	
	/**
	 * XXX: Should be removed. Currently only used by tests and instead the tests should be
	 * created for the different types of concrete sync classes.
	 */
	public boolean isEmpty();
}