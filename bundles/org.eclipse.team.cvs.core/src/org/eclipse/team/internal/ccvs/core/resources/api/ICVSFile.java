package org.eclipse.team.internal.ccvs.core.resources.api;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.eclipse.team.internal.ccvs.core.CVSException;

/**
 * Represents an abstract file.
 * 
 * @see ICVSResource
 */

public interface ICVSFile extends ICVSResource {

	/**
	 * Opens the file for reading. Closing the stream
	 * is responsibility of the caller.
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws CVSException if it was not possible to open the pipe for any other reason
	 */
	InputStream getInputStream() throws CVSException;

	/**
	 * Opens the file for writing. Closing the stream
	 * is responsibility of the caller.
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 * @throws CVSException if it was not possible to open the pipe for any other reason
	 */
	OutputStream getOutputStream() throws CVSException;
	
	/**
	 * Get the size of a file
	 * 
	 * @return 0 if exists() = false
	 */
	long getSize();
	
	/**
	 * Get the timpstamp of the file as a date
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	long getTimeStamp() throws CVSFileNotFoundException;

	/**
	 * Set the timpstamp of the file as a date
	 * 
	 * @throws CVSFileNotFoundException if exists() = false
	 */
	void setTimeStamp(long date) throws CVSFileNotFoundException;

	/**
	 * Gives the content of the file as a string-array.
	 */
	String[] getContent() throws CVSException;

	/**
	 * Gives the content of the file as a string-array.
	 * 
	 * @param delim is the end of line (e.g. "\n","\n\r")
	 */
	// void setContent(String[] content, String delim) throws CVSException;

	/**
	 * Move the resource to another location. Does overwrite without
	 * promting.
	 * 
	 * @throws CVSException if the move was not successful
	 */
	void moveTo(ICVSFile file) throws CVSException;
	
	/**
	 * Get a temporary cvs-file (it does not yet delete on 
	 * exit
	 */
	// public ICVSFile createTempFile() throws CVSException;
	
	/**
	 * Set the file to read-Only mode.
	 */
	void setReadOnly();
}


