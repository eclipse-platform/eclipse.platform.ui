package org.eclipse.team.internal.ccvs.core.response.custom;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.resources.*;

/**
 * This listener is used by RemoteFolder to listener for E and M messages
 * from the CVS server in order to determine the files and folders contained in a parent folder.
 */
public interface IUpdateMessageListener {
	/**
	 * information that a directory which has been reported using directoryInformation() does not exist
	 */
	public void directoryDoesNotExist(IPath path);
	/**
	 * directory information
	 */
	public void directoryInformation(IPath path, boolean newDirectory);
	/**
	 * file information
	 */
	public void fileInformation(char type, String filename) throws CVSException;
	
	public void fileDoesNotExist(String filename);
	/**
	 * Expect the command to which the listener is associated with to throw a CVSServerException
	 */
	public void expectError();
}
