package org.eclipse.team.internal.ccvs.core.response;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.PrintStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.connection.Connection;
import org.eclipse.team.internal.ccvs.core.resources.CVSEntryLineTag;
import org.eclipse.team.internal.ccvs.core.resources.FolderSyncInfo;
import org.eclipse.team.internal.ccvs.core.resources.ICVSFolder;
import org.eclipse.team.internal.ccvs.core.util.Util;

/**
 * ResponseHandler is an abstract class implementing the IResponseHandler.
 * 
 * At the moment it does just provide some additional helper-classes.
 */
public abstract class ResponseHandler implements IResponseHandler {
	
	public static final String SERVER_DELIM = "/";
	public static final String DUMMY_TIMESTAMP = "dummy timestamp";
	public static final String RESULT_OF_MERGE = "Result of merge+";

	/**
	 * Call the old method without a monitor. Either this method or
	 * the called method have to be overloaded, otherwise an 
	 * UnsupportedOperationException is thrown.<br>
	 * This is done for convinience to be able to keep the old methods
	 * that do not use a progress-monitor.
	 * 
	 * Handle the given response from the server.
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							ICVSFolder mRoot,
							IProgressMonitor monitor) 
							throws CVSException {
		
		handle(connection,messageOutput,mRoot);
	}
	
	/**
	 * This method throws an UnsupportedOperationException.
	 * To be overloaded
	 */
	public void handle(Connection connection, 
							PrintStream messageOutput,
							ICVSFolder mRoot) 
							throws CVSException {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Set the properties of the local folder, creating the folder if it does
	 * not exist. If the folder does exist, the remote parameter is not used.
	 * Also, the tag parameter is only set if setTag is true. Similarly,
	 * the parameter staticFolder is only set if setStatic is true.
	 * Otherwise these parameters are ignored.
	 */
	protected static void createFolder(Connection connection,
										ICVSFolder mRoot,
										String local,
										String remote,
										String tag,
										boolean staticFolder,
										boolean setTag,
										boolean setStatic) 
										throws CVSException {
		
		FolderSyncInfo info;
		ICVSFolder mFolder;
		String repo;
		String root;
		CVSEntryLineTag newTag = null;
		
		mFolder = mRoot.getFolder(local);
		
		if (mFolder.exists() && mFolder.isCVSFolder()) {
			info = mFolder.getFolderSyncInfo();
			root = info.getRoot();
			repo = info.getRepository();
			newTag = info.getTag();
		} else {
			mFolder.mkdir();
			root = connection.getCVSRoot().getLocation();
			repo = Util.getRelativePath(connection.getRootDirectory(), remote);
		}
		
		boolean isStatic = setStatic ? staticFolder : false;
		
		if (setTag) {
			if(tag==null) {
				newTag = null;
			} else {
				newTag = new CVSEntryLineTag(tag);
			}
		}		
		
		mFolder.setFolderSyncInfo(new FolderSyncInfo(repo, root, newTag, isStatic));
	}
}

