package org.eclipse.team.internal.ccvs.core.client.listeners;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */
 
import org.eclipse.core.runtime.IPath;

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
	public void fileInformation(int type, String filename);
	
	public void fileDoesNotExist(String filename);
}
