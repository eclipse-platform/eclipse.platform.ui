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
class StaticHandler extends ResponseHandler {
	
	public static final String SET_STATIC_RESPONSE = "Set-static-directory";
	public static final String CLEAR_STATIC_RESPONSE = "Clear-static-directory";
	private final boolean setStatic;
		
	/**
	 * Constructor
	 * 
	 * @param setStatic => SetStaticHandler
	           !setStatic => ClearStaticHandler
	 */
	public StaticHandler(boolean setStatic) {
		this.setStatic = setStatic;
	}

	/**
	 * @see IResponseHandler#getName()
	 */
	public String getName() {
		if (setStatic) {
			return SET_STATIC_RESPONSE;
		} else {
			return CLEAR_STATIC_RESPONSE;
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
		
		IManagedFolder mFolder;
		FolderProperties folderInfo;
		
		// Read the info associated with the Updated response
		localDirectory = connection.readLine();
		remoteDirectory = connection.readLine();
		
		// Cut the last slash form the 
		Assert.isTrue(remoteDirectory.endsWith(SERVER_DELIM));
		remoteDirectory = remoteDirectory.substring(0,remoteDirectory.length() - 
														SERVER_DELIM.length());
		
		mFolder = mRoot.getFolder(localDirectory);
		mFolder.mkdir();
		// Make sure that we were able to create the folder
		Assert.isTrue(mFolder.exists());

		folderInfo = createFolderInfo(connection,remoteDirectory,setStatic);
		mFolder.setFolderInfo(folderInfo);
		
	}


	/**
	 * Get the String identifying the repository which should be stored with
	 * all folders retrieved from the repository. This string corresponds to the
	 * identifier stored in the "Root" file by most cvs clients
	 */
	public static String getRoot(Connection connection) {	
		return connection.getCVSRoot().getLocation();
	}

	/**
	 * Get the realative Path of the resource in comparison to
	 * the homedirectory.
	 * 
	 * This is used to save it while storing the Properties
	 * for a folder (the Repository-file)
	 */	
	private static String getRepository(Connection connection, String resourceName) 
		throws CVSException {
			
		return Util.getRelativePath(connection.getRootDirectory(),
												resourceName);
	}
	
	/**
	 * Constructs a folderInfo out of the information provided
	 * from connection and the local Path of the folder.
	 * 
	 * Sets all the properties of the FolderProperties.
	 */
	private static FolderProperties createFolderInfo(Connection connection,
												String localFolderPath,
												boolean staticFolder)  
												throws CVSException {
								
		return new FolderProperties(getRoot(connection),
								getRepository(connection,localFolderPath),
								staticFolder);
	}

}

