package org.eclipse.team.ccvs.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.io.InputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.core.TeamException;

 /**
  * This interface represents a file in a repository.
  * Instances of this interface can be used to fetch the contents
  * of the remote file.
  * 
  * In the future, additional information should be available (tags, revisions, etc.)
  * 
  * Clients are not expected to implement this interface.
  */
public interface ICVSRemoteFile extends ICVSRemoteResource {

	/**
	 * Get the log entries of the remote file
	 */
	public ILogEntry[] getLogEntries(IProgressMonitor monitor) throws TeamException;
	
	/**
	 * Get the revision of the remote file (e.g. 1.1)
	 * 
	 * The revision depends on any tagging associated with the remote parent used
	 * to access the file. 
	 */
	public String getRevision() throws TeamException;
}

