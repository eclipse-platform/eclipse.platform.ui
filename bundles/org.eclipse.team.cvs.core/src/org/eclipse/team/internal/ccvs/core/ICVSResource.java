package org.eclipse.team.internal.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.syncinfo.*;

/**
 * The CVS analog of file system files and directories. These are handles to
 * state maintained by a CVS client. That is, the CVS resource does not 
 * actually contain data but rather represents CVS state and behavior. You are
 * free to manipulate handles for CVS resources that do not exist but be aware
 * that some methods require that an actual resource be available.
 * <p>
 * The CVS client has been designed to work on these handles uniquely. As such, the
 * handle could be to a remote resource or a local resource and the client could
 * perform CVS operations ignoring the actual location of the resources.</p>
 * 
 * @see ICVSFolder
 * @see ICVSFile
 */
public interface ICVSResource {
	
	/**
	 * Answers the name of the resource.
	 * 
	 * @return the name of the resource this handle represents. It can never
	 * be <code>null</code>.
	 */
	public String getName();
	
	/**
	 * Answers if this resource has CVS synchronization information associated
	 * with it.
	 * 
	 * @return <code>true</code> if the resource is
	 */
	public boolean isManaged() throws CVSException;

	/**
	 * Unmanage the given resource by purging any CVS synchronization associated with the 
	 * resource. The only way a resource can become managed is by running the 
	 * appropriate CVS commands (e.g. add/commit/update).
	 */
	public void unmanage(IProgressMonitor monitor) throws CVSException;

	/**
	 * Answer whether the resource could be ignored because it is in the one of the 
	 * ignore lists maintained by CVS. Even if a resource is ignored, it can still be
	 * added to a repository, at which time it should never be ignored by the CVS
	 * client.
	 * 
	 * @return <code>true</code> if this resource is listed in one of the ignore
	 * files maintained by CVS and <code>false</code> otherwise.
	 */
	public boolean isIgnored() throws CVSException;
	
	/**
	 * Add the following file to the parent's ignore list
	 */
	public void setIgnored() throws CVSException;
	
	/**
	 * Add the following pattern to the file's parent ignore list
	 */
	public void setIgnoredAs(String pattern) throws CVSException;
			
	/**
	 * Answers if the handle is a file or a folder handle.
	 * 
	 * @return <code>true</code> if this is a folder handle and <code>false</code> if
	 * it is a file handle.
	 */
	public boolean isFolder();
	
	/**
	 * Answers if the resource identified by this handle exists.
	 * 
	 * @return <code>true</code> if the resource represented by this handle
	 * exists and <code>false</code> false otherwise.
	 */
	public boolean exists() throws CVSException;	

	/**
	 * Answers the local relative path from the given ancestor to the receiver.
	 * 
	 * @return the ancestor relative path for this resource.
	 */
	public String getRelativePath(ICVSFolder ancestor) throws CVSException;

	/**
	 * Get the remote location of a resource.
	 * 
	 * @return the remote location.
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException;
	
	/**
	 * Answers the workspace synchronization information for this resource. This would 
	 * typically include information from the <b>Entries</b> file that is used to track
	 * the base revisions of local CVS resources.
	 * 
	 * @return the synchronization information for this resource, or <code>null</code>
	 * if the resource does not have synchronization information available.
	 */
	public ResourceSyncInfo getSyncInfo() throws CVSException;
	
	/**
	 * Called to set the workspace synchronization information for a resource. To
	 * clear sync information call <code>unmanage</code>. The sync info will
	 * become the persisted between workbench sessions.
	 * 
	 * @param info the resource synchronization to associate with this resource.
	 */	
	public void setSyncInfo(ResourceSyncInfo info) throws CVSException;

	/** 
	 * Deletes the resource represented by the handle.
	 */
	public void delete() throws CVSException;
	
	/**
	 * Give the folder that contains this resource. If the resource is not managed 
	 * then the result of the operation is not specified.
	 * 
	 * @return a handle to the parent of this resource.
	 */
	public ICVSFolder getParent();

	/**
	 * Accept a vistor to this resource.
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException;	
}