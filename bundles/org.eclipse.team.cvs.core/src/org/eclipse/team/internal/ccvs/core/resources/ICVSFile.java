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
 * The managedFile gives you an FileProperties-Object, that
 * conains CVS-specific information about a file.
 * 
 * It also provides sending and reciving contend to/from an
 * OutputStream/InputStrem.
 * 
 * @see ICVSResource
 */
public interface ICVSFile extends ICVSResource {
	
	/**
	 * Get the size of a file
	 * 
	 * @return 0 if exists() = false
	 */
	long getSize();
	
	/**
	 * Send the fileContend to an InputStream.
	 * A progressmonitor monitors this process.
	 * 
	 * If not exists() the file is created.
	 * 
	 * @throws CVSException if file is contained by an non-existing folder
	 * @throws CVSException if it is not possible to write the file
	 */
	void sendTo(OutputStream outputStream, IProgressMonitor monitor, boolean binary) throws CVSException;
	
	/**
	 * Get the fileContend from a stream and put
	 * it into this file.
	 * 
	 * @throws CVSFileNotFoundException if not exists()
	 */
	void receiveFrom(InputStream inputStream, IProgressMonitor monitor, long size, boolean binary, boolean readOnly) throws CVSException;	

	/**
	 * Get the timpstamp of the file as a date
	 * the format is going to be like: Thu Oct 18 20:21:13 2001
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	String getTimeStamp() throws CVSFileNotFoundException;

	/**
	 * Set the timpstamp of the file as a date
	 * the format needs to be like: Thu Oct 18 20:21:13 2001
	 * 
	 * if the date==null then the current time is used as 
	 * timestamp
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws CVSException if the format of the date is not correct
	 */
	void setTimeStamp(String date) throws CVSException;

	/**
	 * Move the resource to another location. Does overwrite without
	 * promting.
	 * 
	 * @throws CVSException if the move was not successful
	 * @throws ClassCastException if getClass != mFile.getClass
	 */
	void moveTo(ICVSFile mFile) throws CVSException, ClassCastException;
	
	/**
	 * Get if the file has been modified since the last time
	 * saved in the fileEntry. This is the exact but slow 
	 * operation that acctually reads the dirty-state from 
	 * disk.
	 * 
	 * @return true if !isManaged()
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	boolean isDirty() throws CVSException;
}


