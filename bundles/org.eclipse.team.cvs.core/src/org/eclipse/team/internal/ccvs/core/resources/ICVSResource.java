package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * The CVS analog of file system files and directories. These are handles to
 * state maintained by a CVS client. That is, the CVS resource does not 
 * actually contain data but rather represents CVS state and behavior. You are
 * free to manipulate handles for CVS resources that do not exist but be aware
 * that some methods require that an actual resource be available.
 * <p>
 * The CVS client has been designed work on these handles uniquely. As such, the
 * handle could be to a remote resource or a local resource and the client could
 * perform CVS operations ignoring the actual location of the resources.</p>
 * 
 * @see 
 */
public interface ICVSResource {
	
	/**
	 * The CVS separator that is used in the client/server protocol for
	 * building paths. This is independant of the system separator
	 */
	public static final String SEPARATOR = "/";
	
	/**
	 * Answers the name of the resource.
	 */
	public String getName();
	
	/**
	 * Answer whether the resource is managed by it's parent. In general CVS terms, 
	 * this means that the parent folder has an entry for the given resource in 
	 * its CVS/Entries file.
	 */
	public boolean isManaged() throws CVSException;

	/**
	 * Unmanage the given resource by purging any CVS information  associated with the 
	 * resource. The only way a resource can become managed is by running the 
	 * appropriate CVS commands (e.g. add/commit/update).
	 */
	public void unmanage() throws CVSException;

	/**
	 * Answer whether the resource is to be ignored or not.
	 */
	public boolean isIgnored() throws CVSException;
	
	/**
	 * Answers if the handle is a file or a folder handle.
	 */
	boolean isFolder();
	
	/**
	 * Answers if the resource identified by this handle exists.
	 */
	boolean exists();	

	/**
	 * Gives the path from the root folder to this folder.
	 */
	String getRelativePath(ICVSFolder ancestor) throws CVSException;

	/**
	 * Get the remote location of a file either by reading it out of the
	 * file-info or by asking the parent-directory for it and appending the
	 * own name (recursivly).It stops recuring when it hits stopSearching.
	 * If you want to get the remoteLocation of the currentFolder only then
	 * use it with getRemoteLocation(this).
	 */
	public String getRemoteLocation(ICVSFolder stopSearching) throws CVSException;
	
	/**
	 * Answers the workspace synchronization information for this resource or
	 * <code>null</code> if the resource does not have any. This would typically
	 * include information from the <b>Entries</b> file that is used to track
	 * the base revision of a local CVS resource.
	 */
	public ResourceSyncInfo getSyncInfo() throws CVSException;
	
	/**
	 * Called to set the workspace synchronization information for a resource. To
	 * clear sync information call <code>unmanage</code>. The sync info will
	 * become the persisted between workbench sessions.
	 */	
	public void setSyncInfo(ResourceSyncInfo info) throws CVSException;

	/** 
	 * Delete the resource deep.
	 */
	void delete();
	
	/**
	 * Give the folder that contains this resource. If the resource is not managed 
	 * then the result of the operation is not specified.
	 */
	ICVSFolder getParent();

	/**
	 * Accept a vistor to this resource.
	 */
	public void accept(ICVSResourceVisitor visitor) throws CVSException;
}