package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * The CVS analog of a file. CVS files have access to synchronization information
 * that describes their association with the CVS repository. CVS files also provide 
 * mechanisms for sending and receiving content.
 * 
 * @see ICVSResource
 */
public interface ICVSFile extends ICVSResource {
	
	// Constants used to indicate the type of updated response from the server
	public static final int UPDATED = 1;
	public static final int MERGED = 2;
	public static final int UPDATE_EXISTING = 3;
	public static final int CREATED = 4;
	
	/**
	 * Answers the size of the file. 
	 */
	long getSize();
	
 	/**
	 * Gets an input stream for reading from the file.
	 * It is the responsibility of the caller to close the stream when finished.
 	 */
	InputStream getContents() throws CVSException;
	
	/**
	 * Set the contents of the file to the contents of the provided input stream
	 * 
	 * @param responseType the type of reponse that was received from the server
	 * 
	 *    UPDATED - could be a new file or an existing file
	 *    MERGED - merging remote changes with local changes. Failure could result in loss of local changes
	 *    CREATED - contents for a file that doesn't exist locally
	 *    UPDATE_EXISTING - Replacing a local file with no local changes with remote changes.
	 */
	public void setContents(InputStream stream, int responseType, boolean keepLocalHistory, IProgressMonitor monitor) throws CVSException;

	/**
	 * Sets the file's read-only permission.
	 */
	void setReadOnly(boolean readOnly) throws CVSException;
	
	/**
	 * Answers if the file is read-only.
	 */
	boolean isReadOnly() throws CVSException;
	
	/**
	 * Move the resource to another location. Does overwrite without
	 * promting.
	 */
	void copyTo(String filename) throws CVSException;
	
	/**
	 * Answers the current timestamp for this file with second precision.
	 */
	Date getTimeStamp();

	/**
	 * If the date is <code>null</code> then the current time is used.
	 */
	void setTimeStamp(Date date) throws CVSException;
	
	/**
	 * Answers <code>true</code> if the file differs from its base. If the file has no
	 * base, it is not dirty
	 */
	boolean isDirty() throws CVSException;
	
	/**
	 * Answers <code>true</code> if the file has changed since it was last updated
	 * from the repository, if the file does not exist, or is not managed. And <code>false</code> 
	 * if it has not changed.
	 */
	boolean isModified() throws CVSException;
	
	/**
	 * Answers the revision history for this file. This is similar to the
	 * output of the log command.
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws TeamException;
}