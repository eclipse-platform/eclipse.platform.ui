package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * The CVS analog of a file. CVS files have access to synchronization information
 * that describes their association with the CVS repository. CVS files also provide 
 * mechanisms for sending and receiving content.
 * 
 * @see ICVSResource
 */
public interface ICVSFile extends ICVSResource {
	
	/**
	 * Answers the size of the file. 
	 */
	long getSize();
	
	/**
	 * Transfer the contents of the file to the given output stream. If the file is
	 * not binary the line-endings may be converted.
	 */
	void sendTo(OutputStream outputStream, IProgressMonitor monitor, boolean binary) throws CVSException;
	
	/**
	 * Transfer the contents of an input stream to this file. If the file is
	 * not binary the line-endings may be converted.
	 */
	void receiveFrom(InputStream inputStream, IProgressMonitor monitor, long size, boolean binary, boolean readOnly) throws CVSException;	

	/**
	 * Move the resource to another location. Does overwrite without
	 * promting.
	 */
	void moveTo(String filename) throws CVSException;
	
	/**
	 * Answers the current timestamp for this file. The returned format must be in the
	 * following format:
	 *
	 * E MMM dd HH:mm:ss yyyy
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	String getTimeStamp() throws CVSFileNotFoundException;

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