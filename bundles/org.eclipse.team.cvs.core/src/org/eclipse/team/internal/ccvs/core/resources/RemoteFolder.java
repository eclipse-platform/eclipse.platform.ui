package org.eclipse.team.internal.ccvs.core.resources;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.internal.ccvs.core.CVSException;
import org.eclipse.team.internal.ccvs.core.Client;
import org.eclipse.team.internal.ccvs.core.response.IResponseHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.IUpdateMessageListener;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateErrorHandler;
import org.eclipse.team.internal.ccvs.core.response.custom.UpdateMessageHandler;
import org.eclipse.team.ccvs.core.CVSTeamProvider;
import org.eclipse.team.ccvs.core.IRemoteFolder;
import org.eclipse.team.ccvs.core.IRemoteResource;

/**
 * This class provides the implementation of IRemoteFolder
 */
public class RemoteFolder extends RemoteResource implements IRemoteFolder {

	/**
	 * Constructor for RemoteFolder.
	 */
	protected RemoteFolder(RemoteFolder parent, String name, String tag) {
		super(parent, name, tag);
	}

	/**
	 * @see IRemoteFolder#getMembers()
	 */
	public IRemoteResource[] getMembers(IProgressMonitor monitor) throws TeamException {
		return getMembers(tag, monitor);
	}

	/**
	 * @see IRemoteRoot#getMembers()
	 */
	public IRemoteResource[] getMembers(final String tagName, final IProgressMonitor monitor) throws TeamException {
		
		// Create the listener for remote files and folders
		final List errors = new ArrayList();
		final List newRemoteDirectories = new ArrayList();
		final List newRemoteFiles = new ArrayList();
		IUpdateMessageListener listener = new IUpdateMessageListener() {
			public void directoryInformation(IPath path, boolean newDirectory) {
				if (newDirectory && path.segmentCount() == 1)
					newRemoteDirectories.add(path.lastSegment());
//				monitor.subTask(path.lastSegment().toString());
//				monitor.worked(1);
			}
			public void directoryDoesNotExist(IPath path) {
//				monitor.worked(1);
			}
			public void fileInformation(char type, String filename) {
				IPath filePath = new Path(filename);	
				if( filePath.segmentCount() == 1 ) {
					String properFilename = filePath.lastSegment();
					newRemoteFiles.add(properFilename);
//					monitor.subTask(properFilename);
//					monitor.worked(1);
				}
			}
		};
		
		// Build the local options
		List localOptions = new ArrayList();
		localOptions.add("-d");
		if ((tagName != null) && (!tagName.equals("HEAD"))) {
			localOptions.add(Client.TAG_OPTION);
			localOptions.add(tagName);
		}
			
		// Perform a "cvs -n update -d -r tagName folderName" with custom message and error handlers
		try {
			Client.execute(
				Client.UPDATE,
				new String[]{Client.NOCHANGE_OPTION}, 
				(String[])localOptions.toArray(new String[localOptions.size()]), 
				new String[]{"."}, 
				new RemoteManagedFolder(".", getConnection(), getFullPath()),
				monitor,
				CVSTeamProvider.getPrintStream(),
				getConnection(),
				new IResponseHandler[]{new UpdateMessageHandler(listener), new UpdateErrorHandler(listener, errors)});
		} catch (CVSException e) {
			throw CVSTeamProvider.wrapException(e, errors);
		}
		List result = new ArrayList();
		for (int i=0;i<newRemoteDirectories.size();i++)
			result.add(new RemoteFolder(this, (String)newRemoteDirectories.get(i), tagName));
		for (int i=0;i<newRemoteFiles.size();i++)
			result.add(new RemoteFile(this, (String)newRemoteFiles.get(i), tagName));
		return (IRemoteResource[])result.toArray(new IRemoteResource[0]);
	}

	/**
	 * @see IRemoteResource#getType()
	 */
	public int getType() {
		return FOLDER;
	}

}

