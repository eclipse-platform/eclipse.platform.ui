package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Policy;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.ResourceSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFile;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Assert;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * Response on the "Updated" or "Merged" token form the server.
 * 
 * Does get information about the file that is updated
 * and the file-content itself and puts it on the fileSystem.
 * 
 * The difference beetween the "Updated" and the "Merged" is, that
 * an "Merged" file is not going to be up-to-date after the operation.
 * 
 * Requiers a exisiting parent-folder.
 */
class Updated extends ResponseHandler {
	
	public static final String UPDATE_NAME = "Updated";
	public static final String MERGE_NAME = "Merged";
	
	private static final String READ_ONLY_FLAG = "u=rw";
	
	private final ModTimeHandler modTimeHandler;
	private final boolean upToDate;
	
	/**
	 * @param upToDate => Updated-Handler
	 * @param !upToDate => Merged-Handler
	 */
	public Updated(ModTimeHandler modTimeHandler, boolean upToDate) {
		this.modTimeHandler = modTimeHandler;
		this.upToDate = upToDate;
	}
	
	public String getName() {
		if (upToDate) {
			return UPDATE_NAME;
		} else {
			return MERGE_NAME;
		}
	}

	/**
	 * Get the realative Path of the resource in comparison to
	 * the homedirectory.
	 * 
	 * This is used to save it while storing the Properties
	 * for a folder (the Repository-file)
	 */	
	public static String getRepository(Connection connection, String resourceName) 
		throws CVSException {
			
		return Util.getRelativePath(connection.getRootDirectory(),
												resourceName);
	}
	
	/**
	 * This method sets the contents and sync info for a local resource.
	 * 
	 * The contents are set before the sync info.
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							ICVSFolder mRoot,
							IProgressMonitor monitor) 
							throws CVSException {
							
		String fileName;
		int size;
		boolean binary;
		boolean readOnly;
		
		ICVSFile mFile;
		ICVSFolder mParent;
		ResourceSyncInfo fileInfo;
		
		InputStream in;
				
		// Read the info associated with the Updated response
		String localDirectory = connection.readLine();
		String repositoryFilename = connection.readLine();
		String entry = connection.readLine();
		String permissions = connection.readLine();
		
		// Read the number of bytes in the file
		String line = connection.readLine();

		try {
			size = Integer.parseInt(line);
		} catch (NumberFormatException e) {
			throw new CVSException(Policy.bind("Updated.numberFormat"));
		}
		
		// Get the local file		
		fileName = repositoryFilename.substring(
						repositoryFilename.lastIndexOf(SERVER_DELIM) + 1);
		mParent = mRoot.getFolder(localDirectory);
		Assert.isTrue(mParent.exists());
		mFile = mParent.getFile(fileName);
		
		in = connection.getResponseStream();

		binary = entry.indexOf("/"+ResourceSyncInfo.BINARY_TAG) != -1;
		readOnly = permissions.indexOf(READ_ONLY_FLAG) == -1;
		
		mFile.receiveFrom(in,monitor,size,binary,readOnly);
		
		// Set the timestamp in the file, set the result in the fileInfo
		String timestamp;
		mFile.setTimeStamp(modTimeHandler.pullLastTime());
		if (upToDate) {
			timestamp = mFile.getTimeStamp();
		} else {
			timestamp = RESULT_OF_MERGE + mFile.getTimeStamp();
		}			
		mFile.setSyncInfo(new ResourceSyncInfo(entry, permissions, timestamp));

		Assert.isTrue(mFile.isModified()!=upToDate);
	}
}