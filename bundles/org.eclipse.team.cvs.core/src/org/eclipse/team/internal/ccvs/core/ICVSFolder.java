package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.syncinfo.FolderSyncInfo;

/**
 * The CVS analog of a directory. CVS folders have access to synchronization information
 * that describes the association between the folder and the remote repository.
 * 
 * @see ICVSResource
 * @see ICVSFile
 */
public interface ICVSFolder extends ICVSResource {
		
	/**
	 * Answers and array of <code>ICVSResource</code> elements that are immediate 
	 * children of this remote resource, in no particular order. The server may be contacted.
	 * 
 	 * @param monitor a progress monitor to indicate the duration of the operation, or
	 * <code>null</code> if progress reporting is not required.
	 * 
	 * @return array of immediate children of this remote resource. 
	 */
	public ICVSResource[] fetchChildren(IProgressMonitor monitor) throws CVSException;
	
	/**
	 * [Note: temporary method that should only be used by commands.]
	 * Answers the immediate folder children of this resource that are known
	 * at the time of invocation. The server is never contacted. 
	 * 
	 * This includes the union 
	 * of children that satisfy the following criteria:
	 * <ul>
	 *   <li> exists but is not managed (not under CVS control)
	 *   <li> does not exist() but is managed (deleted folder)
	 *   <li> exist() and isManaged() (normal registered file)
	 * </ul>
	 * If the folder does not exist then a zero length array is returned.
	 */
	public ICVSFolder[] getFolders() throws CVSException;
	
	/**
	 * [Note: temporary method that should only be used by commands.]
	 * Answers the immediate file children of this resource that are known
	 * at the time of invocation. The server is never contacted. 
	 * <ul>
	 *   <li> exists but is not managed (not under CVS control)
	 *   <li> does not exist() but is managed (deleted file)
	 *   <li> exist() and isManaged() (normal registered file)
	 * </ul>
	 */
	public ICVSFile[] getFiles() throws CVSException;
	
	/**
	 * Answers a child folder of this resource with the given name or <code>null</code> if 
	 * the given folder does not have a child with that name.
	 */
	public ICVSFolder getFolder(String name) throws CVSException;
	
	/**
	 * Answers a child file of this resource with the given name or <code>null</code> if 
	 * the given folder does not have a child with that name.
	 */
	public ICVSFile getFile(String name) throws CVSException;

	/**
	 * Return the child resource at the given path relative to
	 * the receiver.
	 */
	public ICVSResource getChild(String path) throws CVSException;
	
	/**
	 * Create the folder if it did not exist before. Does only
	 * work if the direct subfolder did exist.
	 * 
	 * @throws CVSException if for some reason it was not possible to create the folder
	 */
	public void mkdir() throws CVSException;

	/**
	 * Answers the folder's synchronization information or <code>null</code> if the folder
	 * is not a CVS folder.
	 * <p>
	 * To modify the folder sync info the caller must call <code>setFolderSyncInfo</code> with
	 * new sync information.</p>
	 */
	public FolderSyncInfo getFolderSyncInfo() throws CVSException;
	
	/**
	 * Set the folder sync information for this folder. Setting the folder information
	 * to <code>null</code> is not supported. The only mechanism for removing an existing
	 * CVS folder is to delete the resource.
	 */
	public void setFolderSyncInfo(FolderSyncInfo folderInfo) throws CVSException;	
	
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
	
	/**
	 * Runs the given action as an atomic cvs local workspace operation 
	 * rooted at this cvs folder.
	 * <p>
	 * After running a method that modifies cvs resource state in the 
	 * local workspace, registered listeners receive after-the-fact 
	 * notification in the form of a resource state change event. In addition,
	 * any resource state information persistance is batched.
	 * This method allows clients to call a number of
	 * methods that modify resources and only have resource
	 * change event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such
	 * call, this method simply runs the action.
	 * </p>
	 *
	 * @param action the action to perform
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CVSException if the operation failed.
	 */
	public void run(ICVSRunnable job, IProgressMonitor monitor) throws CVSException;
}