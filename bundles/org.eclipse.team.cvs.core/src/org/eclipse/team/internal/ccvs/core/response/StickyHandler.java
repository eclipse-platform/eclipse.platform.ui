package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.api.FolderProperties;
import org.eclipse.team.internal.ccvs.core.resources.api.IManagedFolder;
import org.eclipse.team.internal.ccvs.core.requests.RequestSender;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Response to the Clear-static-directory and the Set-static-directory
 * responses of the server.
 * Out of this responses the folder-structure is generated and the
 * information wether the folder a static is set.
 */
class StickyHandler extends ResponseHandler {
	
	public static final String SET_STICKY = "Set-sticky";
	public static final String CLEAR_STICKY = "Clear-sticky";
	private final boolean setSticky;
		
	/**
	 * Constructor
	 * 
	 * @param setSticky => SetStickyHandler
	           !setSticky => ClearStickyHandler
	 */
	public StickyHandler(boolean setSticky) {
		this.setSticky = setSticky;
	}

	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		if (setSticky) {
			return SET_STICKY;
		} else {
			return CLEAR_STICKY;
		}
	}

	/**
	 * @see IResponseHandler#handle(Connection, PrintStream, IManagedFolder)
	 */
	public void handle(
		Connection connection,
		PrintStream messageOutput,
		IManagedFolder mRoot)
		throws CVSException {
		
		String localDirectory;
		String remoteDirectory;
		String tag;
		
		// Read the info associated with the Updated response
		localDirectory = connection.readLine();
		remoteDirectory = connection.readLine();
		
		if (setSticky) {
			tag = connection.readLine();
			if ("".equals(tag)) {
				tag = null;
			}
		} else {
			tag = null;
		}
		
		// Cut the last slash from the remote directory
		Assert.isTrue(remoteDirectory.endsWith(SERVER_DELIM));
		remoteDirectory = remoteDirectory.substring(0,remoteDirectory.length() - 
														SERVER_DELIM.length());
		
		createFolder(connection,
					 mRoot,
					 localDirectory,
					 remoteDirectory,
					 tag,
					 false,
					 true,
					 false);
	}
}

