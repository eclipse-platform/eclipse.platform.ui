package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * The CVS analog of a directory. CVS folders have access to synchronization information
 * that describes the association between the folder and the remote repository.
 * 
 * @see ICVSResource
 * @see ICVSFile
 */
public interface ICVSFolder extends ICVSResource {
		
	/**
	 * Answers the immediate folder children of this resource. This includes the union 
	 * of children that satisfy the following criteria:
	 * <ul>
	 *   <li> exists but is not managed (not under CVS control)
	 *   <li> does not exist() but is managed (deleted folder)
	 *   <li> exist() and isManaged() (normal registered file)
	 * </ul>
	 */
	ICVSFolder[] getFolders() throws CVSException;
	
	/**
	 * Answers the immediate file children of this resource. This includes the union 
	 * of children that satisfy the following criteria:
	 * <ul>
	 *   <li> exists but is not managed (not under CVS control)
	 *   <li> does not exist() but is managed (deleted file)
	 *   <li> exist() and isManaged() (normal registered file)
	 * </ul>
	 */
	ICVSFile[] getFiles() throws CVSException;
	
	/**
	 * Answers a child folder of this resource with the given name or <code>null</code> if 
	 * the given folder does not have a child with that name.
	 */
	ICVSFolder getFolder(String name) throws CVSException;
	
	/**
	 * Answers a child file of this resource with the given name or <code>null</code> if 
	 * the given folder does not have a child with that name.
	 */
	ICVSFile getFile(String name) throws CVSException;

	/**
	 * Answers if a child resource with the given name exists.
	 */
	boolean childExists(String path);
	
	/**
	 * Return the child resource at the given path relative to
	 * the receiver.
	 */
	ICVSResource getChild(String path) throws CVSException;
	
	/**
	 * Create the folder if it did not exist before. Does only
	 * work if the direct subfolder did exist.
	 * 
	 * @throws CVSException if for some reason it was not possible to create the folder
	 */
	void mkdir() throws CVSException;

	/**
	 * Answers the folder's synchronization information or <code>null</code> if the folder
	 * is not a CVS folder.
	 * <p>
	 * To modify the folder sync info the caller must call <code>setFolderSyncInfo</code> with
	 * new sync information.</p>
	 */
	FolderSyncInfo getFolderSyncInfo() throws CVSException;
	
	/**
	 * Set the folder sync information for this folder. Setting the folder information
	 * to <code>null</code> is not supported. The only mechanism for removing an existing
	 * CVS folder is to delete the resource.
	 */
	void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException;	
	
	/**
	 * Accepts the visitor on all files and all subFolder in the folder. Files are
	 * visited first, then all the folders..
	 */
	public void acceptChildren(ICVSResourceVisitor visitor) throws CVSException;
	
	/**
	 * Answers <code>true</code> if the folder has CVS synchronization information and
	 * <code>false</code> otherwise.
	 */
	public boolean isCVSFolder();
}