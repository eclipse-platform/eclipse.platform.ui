package org.eclipse.team.ccvs.core;

import org.eclipse.core.runtime.IAdaptable;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Instances of ILogEntry represent an entry for a CVS file that results
 * from the cvs log command.
 * 
 * Clients are not expected to implement this interface
 */
public interface ILogEntry extends IAdaptable {

	/**
	 * Get the revision for the entry
	 */
	public String getRevision();
	
	/**
	 * Get the author of the revision
	 */
	public String getAuthor();
	
	/**
	 * Get the date the revision was committed
	 */
	public String getDate();
	
	/**
	 * Get the comment for the revision
	 */
	public String getComment();
	
	/**
	 * Get the state
	 */
	public String getState();
	
	/**
	 * Get the tags associated with the revision
	 */
	public String[] getTags();
	
	/**
	 * Get the remote file for this entry
	 */
	public ICVSRemoteFile getRemoteFile();
}

