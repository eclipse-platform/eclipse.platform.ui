package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.ccvs.core.*;
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
	InputStream getInputStream() throws CVSException;
	
	/**
	 * Gets an appending output stream for writing to the file.
	 * It is the responsibility of the caller to close the stream when finished.
 	 */
	OutputStream getAppendingOutputStream() throws CVSException;

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
	 * Answers the current timestamp for this file. The returned format must be in the
	 * following format:
	 *
	 * E MMM dd HH:mm:ss yyyy
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	String getTimeStamp();

	/**
	 * Sets the current timestamp for this file. The supplied date must be in the
	 * following format:
	 *
	 * E MMM dd HH:mm:ss yyyy
	 *
	 * If the date is <code>null</code> then the current time is used as 
	 * the timestamp.
	 */
	void setTimeStamp(String date) throws CVSException;
	
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
}